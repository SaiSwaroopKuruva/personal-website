"use client";

import { useMemo, useState } from "react";
import { formatDate, formatInr } from "@/lib/utils";
import type { NetWorthHistoryPoint } from "@/types/net-worth";

const CHART_WIDTH = 600;

export function NetWorthHistoryChart({ points, height = 220 }: { points: NetWorthHistoryPoint[]; height?: number }) {
  const [hoverIndex, setHoverIndex] = useState<number | null>(null);

  const { path, coords, min, max } = useMemo(() => {
    if (points.length === 0) {
      return { path: "", coords: [] as { x: number; y: number }[], min: 0, max: 0 };
    }
    const values = points.map((p) => Number(p.netWorth));
    const minValue = Math.min(...values);
    const maxValue = Math.max(...values);
    const range = maxValue - minValue || 1;
    const stepX = points.length > 1 ? CHART_WIDTH / (points.length - 1) : 0;
    const pointCoords = values.map((v, i) => ({ x: i * stepX, y: height - ((v - minValue) / range) * (height - 20) - 10 }));
    const linePath = pointCoords.map((c, i) => `${i === 0 ? "M" : "L"}${c.x.toFixed(2)},${c.y.toFixed(2)}`).join(" ");
    return { path: linePath, coords: pointCoords, min: minValue, max: maxValue };
  }, [points, height]);

  if (points.length === 0) {
    return (
      <div className="flex h-[220px] items-center justify-center rounded-lg border border-dashed border-border text-sm text-muted-foreground">
        No net-worth history available yet
      </div>
    );
  }

  const hovered = hoverIndex !== null ? points[hoverIndex] : null;
  const hoveredCoord = hoverIndex !== null ? coords[hoverIndex] : null;

  return (
    <div className="space-y-2">
      <div className="relative">
        <svg
          viewBox={`0 0 ${CHART_WIDTH} ${height}`}
          preserveAspectRatio="none"
          className="h-[220px] w-full"
          role="img"
          aria-label={`Net worth trend from ${formatDate(points[0].date)} to ${formatDate(points[points.length - 1].date)}, ranging from ${formatInr(min)} to ${formatInr(max)}`}
          onMouseMove={(e) => {
            const rect = e.currentTarget.getBoundingClientRect();
            const relativeX = ((e.clientX - rect.left) / rect.width) * CHART_WIDTH;
            const index = coords.reduce((closest, c, i) => (Math.abs(c.x - relativeX) < Math.abs(coords[closest].x - relativeX) ? i : closest), 0);
            setHoverIndex(index);
          }}
          onMouseLeave={() => setHoverIndex(null)}
        >
          <path d={path} fill="none" stroke="currentColor" strokeWidth={2} className="text-primary" />
          {hoveredCoord && (
            <line x1={hoveredCoord.x} y1={0} x2={hoveredCoord.x} y2={height} stroke="currentColor" strokeWidth={1} className="text-muted-foreground/40" />
          )}
          {hoveredCoord && <circle cx={hoveredCoord.x} cy={hoveredCoord.y} r={4} className="fill-primary" />}
        </svg>
        {hovered && (
          <div className="pointer-events-none absolute left-0 top-0 rounded-md border border-border bg-popover px-2 py-1 text-xs shadow-md">
            <p className="font-medium">{formatInr(hovered.netWorth)}</p>
            <p className="text-muted-foreground">{formatDate(hovered.date)}</p>
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
