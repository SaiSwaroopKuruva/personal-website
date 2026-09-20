"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { profileApi } from "@/lib/api/profile-api";
import type {
  SaveAddressPayload,
  UpdateAddressPayload,
  UpdatePreferencesPayload,
  UpdateProfilePayload,
} from "@/types/profile";

const PROFILE_KEY = ["profile"];
const ADDRESSES_KEY = ["profile", "addresses"];

export function useProfile() {
  return useQuery({ queryKey: PROFILE_KEY, queryFn: profileApi.getProfile });
}

export function useUpdateProfile() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: UpdateProfilePayload) => profileApi.updateProfile(payload),
    onSuccess: (data) => {
      queryClient.setQueryData(PROFILE_KEY, data);
      toast.success("Profile updated successfully");
    },
    onError: () => toast.error("Could not update your profile. Please try again."),
  });
}

export function useUploadPhoto() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (file: File) => profileApi.uploadPhoto(file),
    onSuccess: (data) => {
      queryClient.setQueryData(PROFILE_KEY, data);
      toast.success("Profile photo updated");
    },
    onError: () => toast.error("Could not upload your photo. Please try again."),
  });
}

export function useDeletePhoto() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => profileApi.deletePhoto(),
    onSuccess: (data) => {
      queryClient.setQueryData(PROFILE_KEY, data);
      toast.success("Profile photo removed");
    },
  });
}

export function useUpdatePreferences() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: UpdatePreferencesPayload) => profileApi.updatePreferences(payload),
    onSuccess: (data) => {
      queryClient.setQueryData(PROFILE_KEY, data);
      toast.success("Investor preferences updated");
    },
    onError: () => toast.error("Could not update preferences. Please try again."),
  });
}

export function useUpdateLanguage() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (preferredLanguage: string) => profileApi.updateLanguage(preferredLanguage),
    onSuccess: (data) => {
      queryClient.setQueryData(PROFILE_KEY, data);
      toast.success("Preferred language updated");
    },
  });
}

export function useUpdateAddress() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: UpdateAddressPayload) => profileApi.updateAddress(payload),
    onSuccess: (data) => {
      queryClient.setQueryData(PROFILE_KEY, data);
      toast.success("Address updated");
    },
    onError: () => toast.error("Could not update your address. Please try again."),
  });
}

export function useAddresses() {
  return useQuery({ queryKey: ADDRESSES_KEY, queryFn: profileApi.getAddresses });
}

export function useAddAddress() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: SaveAddressPayload) => profileApi.addAddress(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ADDRESSES_KEY });
      toast.success("Address added");
    },
    onError: () => toast.error("Could not add this address. Please try again."),
  });
}

export function useUpdateAddressBookEntry() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: SaveAddressPayload }) =>
      profileApi.updateAddressBookEntry(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ADDRESSES_KEY });
      toast.success("Address updated");
    },
  });
}

export function useDeleteAddress() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => profileApi.deleteAddress(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ADDRESSES_KEY });
      toast.success("Address removed");
    },
  });
}
