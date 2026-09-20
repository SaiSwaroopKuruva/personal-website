export interface DeviceResponse {
  id: string;
  deviceName: string | null;
  browser: string | null;
  ipAddress: string | null;
  lastLogin: string;
  current: boolean;
}

export interface LoginHistoryEntry {
  id: string;
  action: string;
  ipAddress: string | null;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
