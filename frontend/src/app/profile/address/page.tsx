"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { EmptyState } from "@/components/ui/empty-state";
import { AddressCard } from "@/components/profile/address-card";
import { useProfile, useUpdateAddress } from "@/hooks/use-profile";
import { useAddAddress, useAddresses, useDeleteAddress, useUpdateAddressBookEntry } from "@/hooks/use-profile";
import { saveAddressSchema, updateAddressSchema, type SaveAddressFormValues, type UpdateAddressFormValues } from "@/lib/validations/profile";
import type { AddressResponse } from "@/types/profile";
import { MapPin } from "lucide-react";

function PrimaryAddressForm() {
  const { data: profile } = useProfile();
  const updateAddress = useUpdateAddress();
  const form = useForm<UpdateAddressFormValues>({
    resolver: zodResolver(updateAddressSchema),
    defaultValues: { city: "", state: "", country: "", postalCode: "" },
  });

  useEffect(() => {
    if (profile) {
      form.reset({
        city: profile.city ?? "",
        state: profile.state ?? "",
        country: profile.country ?? "",
        postalCode: profile.postalCode ?? "",
      });
    }
  }, [profile, form]);

  return (
    <Card>
      <CardHeader>
        <CardTitle>Primary Address</CardTitle>
        <CardDescription>Used for KYC and profile completeness</CardDescription>
      </CardHeader>
      <CardContent>
        <Form {...form}>
          <form
            onSubmit={form.handleSubmit((values) => updateAddress.mutate(values))}
            className="grid gap-4 sm:grid-cols-2"
          >
            <FormField
              control={form.control}
              name="city"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>City</FormLabel>
                  <FormControl>
                    <Input {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="state"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>State</FormLabel>
                  <FormControl>
                    <Input {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="country"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Country</FormLabel>
                  <FormControl>
                    <Input {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="postalCode"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>PIN code</FormLabel>
                  <FormControl>
                    <Input {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <div className="sm:col-span-2">
              <Button type="submit" isLoading={updateAddress.isPending}>
                Save
              </Button>
            </div>
          </form>
        </Form>
      </CardContent>
    </Card>
  );
}

function AddressFormDialog({
  open,
  onOpenChange,
  address,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  address?: AddressResponse;
}) {
  const addAddress = useAddAddress();
  const updateEntry = useUpdateAddressBookEntry();
  const form = useForm<SaveAddressFormValues>({
    resolver: zodResolver(saveAddressSchema),
    defaultValues: {
      type: address?.type ?? "HOME",
      addressLine1: address?.addressLine1 ?? "",
      addressLine2: address?.addressLine2 ?? "",
      city: address?.city ?? "",
      state: address?.state ?? "",
      country: address?.country ?? "",
      postalCode: address?.postalCode ?? "",
      isDefault: address?.isDefault ?? false,
    },
  });

  function onSubmit(values: SaveAddressFormValues) {
    const payload = { ...values, addressLine2: values.addressLine2 || null };
    if (address) {
      updateEntry.mutate({ id: address.id, payload }, { onSuccess: () => onOpenChange(false) });
    } else {
      addAddress.mutate(payload, { onSuccess: () => onOpenChange(false) });
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{address ? "Edit address" : "Add address"}</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="grid gap-3">
            <FormField
              control={form.control}
              name="type"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Type</FormLabel>
                  <FormControl>
                    <select {...field} className="flex h-10 w-full rounded-lg border border-input bg-background px-3 py-2 text-sm shadow-sm">
                      <option value="HOME">Home</option>
                      <option value="WORK">Work</option>
                      <option value="OTHER">Other</option>
                    </select>
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="addressLine1"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Address line 1</FormLabel>
                  <FormControl>
                    <Input {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="addressLine2"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Address line 2 (optional)</FormLabel>
                  <FormControl>
                    <Input {...field} value={field.value ?? ""} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <div className="grid grid-cols-2 gap-3">
              <FormField
                control={form.control}
                name="city"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>City</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="state"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>State</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="country"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Country</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="postalCode"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>PIN code</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>
            <FormField
              control={form.control}
              name="isDefault"
              render={({ field }) => (
                <label className="flex items-center gap-2 text-sm">
                  <input
                    type="checkbox"
                    checked={field.value}
                    onChange={(event) => field.onChange(event.target.checked)}
                  />
                  Set as default address
                </label>
              )}
            />
            <Button type="submit" isLoading={addAddress.isPending || updateEntry.isPending}>
              Save address
            </Button>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}

export default function AddressPage() {
  const { data: addresses, isLoading } = useAddresses();
  const deleteAddress = useDeleteAddress();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingAddress, setEditingAddress] = useState<AddressResponse | undefined>(undefined);

  function openAddDialog() {
    setEditingAddress(undefined);
    setDialogOpen(true);
  }

  function openEditDialog(address: AddressResponse) {
    setEditingAddress(address);
    setDialogOpen(true);
  }

  return (
    <div className="space-y-6">
      <PrimaryAddressForm />

      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0">
          <div>
            <CardTitle>Address Book</CardTitle>
            <CardDescription>Manage saved home, work and other addresses</CardDescription>
          </div>
          <Button size="sm" onClick={openAddDialog}>
            <Plus className="h-4 w-4" /> Add address
          </Button>
        </CardHeader>
        <CardContent className="space-y-3">
          {isLoading ? (
            <Skeleton className="h-20" />
          ) : !addresses || addresses.length === 0 ? (
            <EmptyState icon={MapPin} title="No addresses saved yet" description="Add an address to speed up KYC and delivery of physical documents." />
          ) : (
            addresses.map((address) => (
              <AddressCard
                key={address.id}
                address={address}
                onEdit={() => openEditDialog(address)}
                onDelete={() => deleteAddress.mutate(address.id)}
              />
            ))
          )}
        </CardContent>
      </Card>

      <AddressFormDialog open={dialogOpen} onOpenChange={setDialogOpen} address={editingAddress} />
    </div>
  );
}
