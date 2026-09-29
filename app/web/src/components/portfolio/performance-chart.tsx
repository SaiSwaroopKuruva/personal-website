"use client";

import { useMemo, useState } from "react";
import { formatDate, formatInr } from "@/lib/utils";
import type { PerformancePoint } from "@/types/portfolio";

const CHART_WIDTH = 600;

interface PerformanceChartProps {
  points: PerformancePoint[];
  height?: number;
}

/** Two-series line chart (market value vs. invested capital) using the same inline-SVG approach as NavChart - no chart library dependency. */
export function PerformanceChart({ points, height = 240 }: PerformanceChartProps) {
  const [hoverIndex, setHoverIndex] = useState<number | null>(null);

  const { marketPath, investedPath, coords, min, max } = useMemo(() => {
    if (points.length === 0) {
      return { marketPath: "", investedPath: "", coords: [] as { x: number; y: number }[], min: 0, max: 0 };
    }
    const marketValues = points.map((p) => Number(p.marketValue));
    const investedValues = points.map((p) => Number(p.investedValue));
    const allValues = [...marketValues, ...investedValues];
    const minValue = Math.min(...allValues);
    const maxValue = Math.max(...allValues);
    const range = maxValue - minValue || 1;
    const stepX = points.length > 1 ? CHART_WIDTH / (points.length - 1) : 0;

    const toY = (value: number) => height - ((value - minValue) / range) * (height - 20) - 10;
    const pointCoords = marketValues.map((value, i) => ({ x: i * stepX, y: toY(value) }));
    const buildPath = (values: number[]) =>
      values.map((v, i) => `${i === 0 ? "M" : "L"}${(i * stepX).toFixed(2)},${toY(v).toFixed(2)}`).join(" ");

    return {
      marketPath: buildPath(marketValues),
      investedPath: buildPath(investedValues),
      coords: pointCoords,
      min: minValue,
      max: maxValue,
    };
  }, [points, height]);

  if (points.length === 0) {
    return (
      <div className="flex h-[240px] items-center justify-center rounded-lg border border-dashed border-border text-sm text-muted-foreground">
        No performance history available yet
      </div>
    );
  }

  const hovered = hoverIndex !== null ? points[hoverIndex] : null;
  const hoveredCoord = hoverIndex !== null ? coords[hoverIndex] : null;

  return (
    <div className="space-y-2">
      <div className="flex items-center gap-4 text-xs text-muted-foreground">
        <span className="flex items-center gap-1">
          <span className="h-2 w-2 rounded-full bg-primary" /> Market value
        </span>
        <span className="flex items-center gap-1">
          <span className="h-2 w-2 rounded-full bg-muted-foreground/50" /> Invested capital
        </span>
      </div>
      <div className="relative">
        <svg
          viewBox={`0 0 ${CHART_WIDTH} ${height}`}
          preserveAspectRatio="none"
          className="h-[240px] w-full"
          role="img"
          aria-label={`Portfolio performance from ${formatDate(points[0].date)} to ${formatDate(points[points.length - 1].date)}, ranging from ${formatInr(min)} to ${formatInr(max)}`}
          onMouseMove={(e) => {
            const rect = e.currentTarget.getBoundingClientRect();
            const relativeX = ((e.clientX - rect.left) / rect.width) * CHART_WIDTH;
            const index = coords.reduce((closest, c, i) => (Math.abs(c.x - relativeX) < Math.abs(coords[closest].x - relativeX) ? i : closest), 0);
            setHoverIndex(index);
          }}
          onMouseLeave={() => setHoverIndex(null)}
        >
          <path d={investedPath} fill="none" strokeDasharray="4 4" stroke="currentColor" strokeWidth={2} className="text-muted-foreground/50" />
          <path d={marketPath} fill="none" stroke="currentColor" strokeWidth={2} className="text-primary" />
          {hoveredCoord && (
            <line x1={hoveredCoord.x} y1={0} x2={hoveredCoord.x} y2={height} stroke="currentColor" strokeWidth={1} className="text-muted-foreground/40" />
          )}
          {hoveredCoord && <circle cx={hoveredCoord.x} cy={hoveredCoord.y} r={4} className="fill-primary" />}
        </svg>
        {hovered && (
          <div className="pointer-events-none absolute left-0 top-0 rounded-md border border-border bg-popover px-2 py-1 text-xs shadow-md">
            <p className="font-medium">{formatInr(hovered.marketValue)}</p>
            <p className="text-muted-foreground">{formatDate(hovered.date)}</p>
            {!hovered.complete ? <p className="text-amber-600 dark:text-amber-400">Partial valuation</p> : null}
          </div>
        )}
      </div>
      <div className="flex justify-between text-xs text-muted-foreground">
        <span>{formatDate(points[0].date)}</span>
        <span>{formatDate(points[points.length - 1].date)}</span>
      </div>
    </div>
  );
}
