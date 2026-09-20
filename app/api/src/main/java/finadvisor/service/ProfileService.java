package finadvisor.service;

import finadvisor.dto.profile.AddressResponse;
import finadvisor.dto.profile.ProfileResponse;
import finadvisor.dto.profile.SaveAddressRequest;
import finadvisor.dto.profile.UpdateAddressRequest;
import finadvisor.dto.profile.UpdateLanguageRequest;
import finadvisor.dto.profile.UpdatePreferencesRequest;
import finadvisor.dto.profile.UpdateProfileRequest;

import java.util.List;
import java.util.UUID;

public interface ProfileService {
    ProfileResponse getProfile(String email);

    ProfileResponse updateProfile(String email, UpdateProfileRequest request);

    ProfileResponse updatePhoto(String email, String photoUrl);

    ProfileResponse deletePhoto(String email);

    ProfileResponse updatePreferences(String email, UpdatePreferencesRequest request);

    ProfileResponse updateLanguage(String email, UpdateLanguageRequest request);

    ProfileResponse updateAddress(String email, UpdateAddressRequest request);

    List<AddressResponse> getAddresses(String email);

    AddressResponse addAddress(String email, SaveAddressRequest request);

    AddressResponse updateAddressBookEntry(String email, UUID addressId, SaveAddressRequest request);

    void deleteAddress(String email, UUID addressId);
}
