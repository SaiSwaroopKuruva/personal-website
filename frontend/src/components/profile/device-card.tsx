import { Laptop, ShieldOff } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import type { DeviceResponse } from "@/types/security";

interface DeviceCardProps {
  device: DeviceResponse;
  onRevoke?: () => void;
  isRevoking?: boolean;
}

export function DeviceCard({ device, onRevoke, isRevoking }: DeviceCardProps) {
  return (
    <Card>
      <CardContent className="flex items-center justify-between gap-4 pt-6">
        <div className="flex items-center gap-3">
          <div className="rounded-full bg-primary/10 p-2 text-primary">
            <Laptop className="h-4 w-4" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <p className="text-sm font-medium">{device.deviceName ?? device.browser ?? "Unknown device"}</p>
              {device.current ? <Badge variant="success">This device</Badge> : null}
            </div>
            <p className="text-xs text-muted-foreground">
              {device.ipAddress ?? "Unknown IP"} · Last active {new Date(device.lastLogin).toLocaleString()}
            </p>
          </div>
        </div>
        {onRevoke && !device.current ? (
          <Button variant="outline" size="sm" onClick={onRevoke} isLoading={isRevoking}>
            <ShieldOff className="h-4 w-4" /> Revoke
          </Button>
        ) : null}
      </CardContent>
    </Card>
  );
}
