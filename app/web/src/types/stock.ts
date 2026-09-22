export interface StockDetails {
  symbol: string;
  exchange: string;
  isin: string | null;
  companyName: string;
  sector: string | null;
  series: string | null;
  lotSize: number | null;
}

export type DataFreshness = "LIVE" | "FRESH" | "STALE" | "UNKNOWN";

export interface StockQuote {
  symbol: string;
  exchange: string;
  lastTradedPrice: string | null;
  previousClose: string | null;
  open: string | null;
  high: string | null;
  low: string | null;
  volume: number | null;
  change: string | null;
  changePercent: string | null;
  timestamp: string | null;
  source: string;
  dataType: string;
  freshness: DataFreshness;
}

export interface Candle {
  timestamp: string;
  open: string | null;
  high: string | null;
  low: string | null;
  close: string | null;
  volume: number | null;
}

export interface CandleHistory {
  symbol: string;
  interval: string;
  candles: Candle[];
}

export interface MarketIndex {
  name: string;
  lastPrice: string | null;
  change: string | null;
  changePercent: string | null;
  timestamp: string | null;
}

export interface MarketStatus {
  exchange: string;
  status: string;
  lastUpdated: string | null;
}

export interface StockNewsItem {
  heading: string | null;
  summary: string | null;
  articleLink: string | null;
  publishedAt: string | null;
}
