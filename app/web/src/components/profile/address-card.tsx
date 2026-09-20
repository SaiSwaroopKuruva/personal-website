import { Home, MapPin, Star, Trash2 } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import type { AddressResponse } from "@/types/profile";

interface AddressCardProps {
  address: AddressResponse;
  onEdit?: () => void;
  onDelete?: () => void;
}

export function AddressCard({ address, onEdit, onDelete }: AddressCardProps) {
  return (
    <Card>
      <CardContent className="flex items-start justify-between gap-4 pt-6">
        <div className="flex items-start gap-3">
          <div className="rounded-full bg-primary/10 p-2 text-primary">
            <Home className="h-4 w-4" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <p className="text-sm font-medium capitalize">{address.type.toLowerCase()}</p>
              {address.isDefault ? (
                <Badge variant="success" className="gap-1">
                  <Star className="h-3 w-3" /> Default
                </Badge>
              ) : null}
            </div>
            <p className="mt-1 flex items-start gap-1 text-sm text-muted-foreground">
              <MapPin className="mt-0.5 h-3.5 w-3.5 shrink-0" />
              <span>
                {address.addressLine1}
                {address.addressLine2 ? `, ${address.addressLine2}` : ""}, {address.city}, {address.state}{" "}
                {address.postalCode}, {address.country}
              </span>
            </p>
          </div>
        </div>
        <div className="flex shrink-0 gap-2">
          {onEdit ? (
            <Button variant="outline" size="sm" onClick={onEdit}>
              Edit
            </Button>
          ) : null}
          {onDelete ? (
            <Button variant="ghost" size="icon" onClick={onDelete} aria-label="Delete address">
              <Trash2 className="h-4 w-4 text-destructive" />
            </Button>
          ) : null}
        </div>
      </CardContent>
    </Card>
  );
}
