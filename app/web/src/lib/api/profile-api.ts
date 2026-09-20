import { apiClient } from "@/lib/api/axios-client";
import type {
  AddressResponse,
  ProfileResponse,
  SaveAddressPayload,
  UpdateAddressPayload,
  UpdatePreferencesPayload,
  UpdateProfilePayload,
} from "@/types/profile";

export const profileApi = {
  getProfile: () => apiClient.get<ProfileResponse>("/api/profile").then((res) => res.data),

  updateProfile: (payload: UpdateProfilePayload) =>
    apiClient.put<ProfileResponse>("/api/profile", payload).then((res) => res.data),

  uploadPhoto: (file: File) => {
    const formData = new FormData();
    formData.append("file", file);
    return apiClient
      .post<ProfileResponse>("/api/profile/upload-photo", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      })
      .then((res) => res.data);
  },

  deletePhoto: () => apiClient.delete<ProfileResponse>("/api/profile/photo").then((res) => res.data),

  updatePreferences: (payload: UpdatePreferencesPayload) =>
    apiClient.patch<ProfileResponse>("/api/profile/preferences", payload).then((res) => res.data),

  updateLanguage: (preferredLanguage: string) =>
    apiClient.patch<ProfileResponse>("/api/profile/language", { preferredLanguage }).then((res) => res.data),

  updateAddress: (payload: UpdateAddressPayload) =>
    apiClient.patch<ProfileResponse>("/api/profile/address", payload).then((res) => res.data),

  getAddresses: () => apiClient.get<AddressResponse[]>("/api/profile/addresses").then((res) => res.data),

  addAddress: (payload: SaveAddressPayload) =>
    apiClient.post<AddressResponse>("/api/profile/addresses", payload).then((res) => res.data),

  updateAddressBookEntry: (id: string, payload: SaveAddressPayload) =>
    apiClient.put<AddressResponse>(`/api/profile/addresses/${id}`, payload).then((res) => res.data),

  deleteAddress: (id: string) => apiClient.delete<void>(`/api/profile/addresses/${id}`).then((res) => res.data),
};
