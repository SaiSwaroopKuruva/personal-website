import { FreshnessBadge } from "@/components/stocks/freshness-badge";
import type { DataFreshness } from "@/types/stock";

interface DataSourceNoteProps {
  source: string;
  dataType: string;
  timestamp: string | null;
  freshness: DataFreshness;
}

/** Makes the source of every financial data point explicit (Part 38) - never hides provider/ingestion timing. */
export function DataSourceNote({ source, dataType, timestamp, freshness }: DataSourceNoteProps) {
  return (
    <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
      <FreshnessBadge freshness={freshness} />
      <span>
        Source: <span className="font-medium text-foreground">{source}</span>
      </span>
      <span aria-hidden="true">·</span>
      <span>{dataType.replaceAll("_", " ")}</span>
      {timestamp && (
        <>
          <span aria-hidden="true">·</span>
          <span>Updated {new Date(timestamp).toLocaleString("en-IN")}</span>
        </>
      )}
    </div>
  );
}
