"use client";

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { EmptyState } from "@/components/ui/empty-state";
import { DeviceCard } from "@/components/profile/device-card";
import { useDevices, useRevokeDevice } from "@/hooks/use-security";
import { Laptop } from "lucide-react";

export default function DevicesPage() {
  const { data: devices, isLoading } = useDevices();
  const revokeDevice = useRevokeDevice();

  return (
    <Card>
      <CardHeader>
        <CardTitle>Devices</CardTitle>
        <CardDescription>Devices that have signed in to your account</CardDescription>
      </CardHeader>
      <CardContent className="space-y-3">
        {isLoading ? (
          <Skeleton className="h-24" />
        ) : !devices || devices.length === 0 ? (
          <EmptyState icon={Laptop} title="No devices found" />
        ) : (
          devices.map((device) => (
            <DeviceCard
              key={device.id}
              device={device}
              onRevoke={() => revokeDevice.mutate(device.id)}
              isRevoking={revokeDevice.isPending}
            />
          ))
        )}
      </CardContent>
    </Card>
  );
}
