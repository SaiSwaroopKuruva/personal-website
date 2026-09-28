"use client";

import { useMemo, useState } from "react";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { ErrorState } from "@/components/ui/error-state";
import { useStockOhlc } from "@/hooks/use-stocks";
import { cn, formatDate, formatInr } from "@/lib/utils";
import type { Candle } from "@/types/stock";

const CHART_WIDTH = 600;
const PRICE_HEIGHT = 240;
const VOLUME_HEIGHT = 56;

type ChartMode = "line" | "candlestick";

const RANGES = ["1M", "3M", "6M", "1Y", "3Y", "5Y", "MAX"] as const;
type Range = (typeof RANGES)[number];

function rangeToFromDate(range: Range): string {
  const from = new Date();
  switch (range) {
    case "1M":
      from.setMonth(from.getMonth() - 1);
      break;
    case "3M":
      from.setMonth(from.getMonth() - 3);
      break;
    case "6M":
      from.setMonth(from.getMonth() - 6);
      break;
    case "1Y":
      from.setFullYear(from.getFullYear() - 1);
      break;
    case "3Y":
      from.setFullYear(from.getFullYear() - 3);
      break;
    case "5Y":
      from.setFullYear(from.getFullYear() - 5);
      break;
    case "MAX":
      from.setFullYear(from.getFullYear() - 20);
      break;
  }
  return from.toISOString().slice(0, 10);
}

/** Reusable stock price chart (Part 30/31/42) - hand-rolled SVG, no charting dependency (mirrors NavChart's approach).
 * Supports line and candlestick modes, a time-range selector, OHLC/volume tooltip, loading/empty/error states. */
export function PriceChart({ symbol }: { symbol: string }) {
  const [range, setRange] = useState<Range>("1Y");
  const [mode, setMode] = useState<ChartMode>("line");
  const [hoverIndex, setHoverIndex] = useState<number | null>(null);

  const { data, isLoading, isError, refetch } = useStockOhlc(symbol, { from: rangeToFromDate(range) });
  const candles = useMemo(() => (data?.candles ?? []).filter((c) => c.close !== null), [data]);

  const scale = useMemo(() => {
    if (candles.length === 0) {
      return null;
    }
    const highs = candles.map((c) => Number(c.high ?? c.close));
    const lows = candles.map((c) => Number(c.low ?? c.close));
    const volumes = candles.map((c) => c.volume ?? 0);
    const min = Math.min(...lows);
    const max = Math.max(...highs);
    const priceRange = max - min || 1;
    const maxVolume = Math.max(...volumes, 1);
    const stepX = candles.length > 1 ? CHART_WIDTH / (candles.length - 1) : 0;
    const bandWidth = candles.length > 1 ? Math.min(CHART_WIDTH / candles.length, 14) : 8;

    const priceY = (value: number) => PRICE_HEIGHT - ((value - min) / priceRange) * (PRICE_HEIGHT - 20) - 10;
    const xFor = (index: number) => index * stepX;

    return { min, max, maxVolume, stepX, bandWidth, priceY, xFor };
  }, [candles]);

  if (isLoading) {
    return <Skeleton className="h-[320px] w-full" />;
  }
  if (isError) {
    return <ErrorState title="Could not load the price chart" onRetry={() => refetch()} />;
  }
  if (!scale || candles.length === 0) {
    return (
      <div className="flex h-[240px] items-center justify-center rounded-lg border border-dashed border-border text-sm text-muted-foreground">
        No price history available for this period
      </div>
    );
  }

  const linePath = candles
    .map((c, i) => `${i === 0 ? "M" : "L"}${scale.xFor(i).toFixed(2)},${scale.priceY(Number(c.close)).toFixed(2)}`)
    .join(" ");
  const hovered: Candle | null = hoverIndex !== null ? candles[hoverIndex] : null;

  return (
    <div className="space-y-3">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div className="flex flex-wrap gap-1">
          {RANGES.map((r) => (
            <Button key={r} size="sm" variant={r === range ? "secondary" : "ghost"} onClick={() => setRange(r)}>
              {r}
            </Button>
          ))}
        </div>
        <div className="flex gap-1">
          <Button size="sm" variant={mode === "line" ? "secondary" : "ghost"} onClick={() => setMode("line")}>
            Line
          </Button>
          <Button size="sm" variant={mode === "candlestick" ? "secondary" : "ghost"} onClick={() => setMode("candlestick")}>
            Candlestick
          </Button>
        </div>
      </div>

      <div className="relative">
        <svg
          viewBox={`0 0 ${CHART_WIDTH} ${PRICE_HEIGHT + VOLUME_HEIGHT}`}
          preserveAspectRatio="none"
          className="h-[300px] w-full"
          role="img"
          aria-label={`${symbol} price chart from ${formatDate(candles[0].timestamp)} to ${formatDate(candles[candles.length - 1].timestamp)}, ranging from ${formatInr(scale.min, { decimals: 2 })} to ${formatInr(scale.max, { decimals: 2 })}`}
          onMouseMove={(e) => {
            const rect = e.currentTarget.getBoundingClientRect();
            const relativeX = ((e.clientX - rect.left) / rect.width) * CHART_WIDTH;
            const index = candles.reduce(
              (closest, _c, i) => (Math.abs(scale.xFor(i) - relativeX) < Math.abs(scale.xFor(closest) - relativeX) ? i : closest),
              0
            );
            setHoverIndex(index);
          }}
          onMouseLeave={() => setHoverIndex(null)}
        >
          {mode === "line" ? (
            <path d={linePath} fill="none" stroke="currentColor" strokeWidth={2} className="text-primary" />
          ) : (
            candles.map((c, i) => {
              const open = Number(c.open ?? c.close);
              const close = Number(c.close);
              const high = Number(c.high ?? c.close);
              const low = Number(c.low ?? c.close);
              const isUp = close >= open;
              const x = scale.xFor(i);
              const bodyTop = scale.priceY(Math.max(open, close));
              const bodyBottom = scale.priceY(Math.min(open, close));
              return (
                <g key={c.timestamp} className={isUp ? "text-positive" : "text-negative"}>
                  <line x1={x} y1={scale.priceY(high)} x2={x} y2={scale.priceY(low)} stroke="currentColor" strokeWidth={1} />
                  <rect
                    x={x - scale.bandWidth / 2}
                    y={bodyTop}
                    width={scale.bandWidth}
                    height={Math.max(bodyBottom - bodyTop, 1)}
                    fill="currentColor"
                  />
                </g>
              );
            })
          )}

          {/* Volume bars */}
          {candles.map((c, i) => {
            const x = scale.xFor(i);
            const barHeight = ((c.volume ?? 0) / scale.maxVolume) * (VOLUME_HEIGHT - 4);
            return (
              <rect
                key={"vol-" + c.timestamp}
                x={x - scale.bandWidth / 2}
                y={PRICE_HEIGHT + (VOLUME_HEIGHT - barHeight)}
                width={scale.bandWidth}
                height={barHeight}
                className="fill-muted-foreground/30"
              />
            );
          })}

          {hoverIndex !== null && (
            <line
              x1={scale.xFor(hoverIndex)}
              y1={0}
              x2={scale.xFor(hoverIndex)}
              y2={PRICE_HEIGHT + VOLUME_HEIGHT}
              stroke="currentColor"
              strokeWidth={1}
              className="text-muted-foreground/40"
            />
          )}
        </svg>

        {hovered && (
          <div className="pointer-events-none absolute left-0 top-0 space-y-0.5 rounded-md border border-border bg-popover px-2 py-1 text-xs shadow-md">
            <p className="font-medium">{formatDate(hovered.timestamp)}</p>
            <p className={cn("tabular-nums", Number(hovered.close) >= Number(hovered.open ?? hovered.close) ? "text-positive" : "text-negative")}>
              O {formatInr(hovered.open, { decimals: 2 })} · H {formatInr(hovered.high, { decimals: 2 })} · L {formatInr(hovered.low, { decimals: 2 })} · C{" "}
              {formatInr(hovered.close, { decimals: 2 })}
            </p>
            {hovered.volume !== null && <p className="text-muted-foreground">Vol {new Intl.NumberFormat("en-IN").format(hovered.volume)}</p>}
          </div>
        )}
      </div>

      <p className="text-[11px] text-muted-foreground">
        Historical price movement shown for reference only; it does not predict future performance.
      </p>
    </div>
  );
}
