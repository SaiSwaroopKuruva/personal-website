"use client";

import { useMemo, useState } from "react";
import { formatDate, formatNav } from "@/lib/utils";
import type { NavPoint } from "@/types/mutual-fund";

interface NavChartProps {
  points: NavPoint[];
  height?: number;
}

const CHART_WIDTH = 600;

export function NavChart({ points, height = 220 }: NavChartProps) {
  const [hoverIndex, setHoverIndex] = useState<number | null>(null);

  const { path, coords, min, max } = useMemo(() => {
    if (points.length === 0) {
      return { path: "", coords: [] as { x: number; y: number }[], min: 0, max: 0 };
    }
    const navs = points.map((p) => Number(p.nav));
    const minNav = Math.min(...navs);
    const maxNav = Math.max(...navs);
    const range = maxNav - minNav || 1;
    const stepX = points.length > 1 ? CHART_WIDTH / (points.length - 1) : 0;

    const pointCoords = navs.map((nav, i) => ({
      x: i * stepX,
      y: height - ((nav - minNav) / range) * (height - 20) - 10,
    }));

    const linePath = pointCoords.map((c, i) => `${i === 0 ? "M" : "L"}${c.x.toFixed(2)},${c.y.toFixed(2)}`).join(" ");

    return { path: linePath, coords: pointCoords, min: minNav, max: maxNav };
  }, [points, height]);

  if (points.length === 0) {
    return (
      <div className="flex h-[220px] items-center justify-center rounded-lg border border-dashed border-border text-sm text-muted-foreground">
        No NAV history available for this period
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
          aria-label={`NAV chart from ${formatDate(points[0].date)} to ${formatDate(points[points.length - 1].date)}, ranging from ${formatNav(min)} to ${formatNav(max)}`}
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
            <p className="font-medium">{formatNav(hovered.nav)}</p>
            <p className="text-muted-foreground">{formatDate(hovered.date)}</p>
          </div>
        )}
      </div>
      <div className="flex justify-between text-xs text-muted-foreground">
        <span>{formatDate(points[0].date)}</span>
        <span>{formatDate(points[points.length - 1].date)}</span>
      </div>
      <p className="text-[11px] text-muted-foreground">
        Historical NAV movement shown for reference only; it does not predict future performance.
      </p>
    </div>
  );
}
