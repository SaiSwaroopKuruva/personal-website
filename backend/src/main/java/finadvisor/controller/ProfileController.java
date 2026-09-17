package finadvisor.controller;

import finadvisor.dto.profile.AddressResponse;
import finadvisor.dto.profile.ProfileResponse;
import finadvisor.dto.profile.SaveAddressRequest;
import finadvisor.dto.profile.UpdateAddressRequest;
import finadvisor.dto.profile.UpdateLanguageRequest;
import finadvisor.dto.profile.UpdatePreferencesRequest;
import finadvisor.dto.profile.UpdateProfileRequest;
import finadvisor.service.FileStorageService;
import finadvisor.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "User profile, address book and investor preference management")
public class ProfileController {

    private final ProfileService profileService;
    private final FileStorageService fileStorageService;

    @GetMapping
    @Operation(summary = "Get the current user's profile")
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(profileService.getProfile(authentication.getName()));
    }

    @PutMapping
    @Operation(summary = "Update the current user's profile details")
    public ResponseEntity<ProfileResponse> updateProfile(Authentication authentication,
                                                           @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(authentication.getName(), request));
    }

    @PostMapping("/upload-photo")
    @Operation(summary = "Upload a new profile photo")
    public ResponseEntity<ProfileResponse> uploadPhoto(Authentication authentication,
                                                        @RequestParam("file") MultipartFile file) {
        String photoUrl = fileStorageService.storeProfilePhoto(file);
        return ResponseEntity.ok(profileService.updatePhoto(authentication.getName(), photoUrl));
    }

    @DeleteMapping("/photo")
    @Operation(summary = "Remove the current profile photo")
    public ResponseEntity<ProfileResponse> deletePhoto(Authentication authentication) {
        return ResponseEntity.ok(profileService.deletePhoto(authentication.getName()));
    }

    @PatchMapping("/preferences")
    @Operation(summary = "Update investor preferences (experience, horizon, monthly budget)")
    public ResponseEntity<ProfileResponse> updatePreferences(Authentication authentication,
                                                               @Valid @RequestBody UpdatePreferencesRequest request) {
        return ResponseEntity.ok(profileService.updatePreferences(authentication.getName(), request));
    }

    @PatchMapping("/language")
    @Operation(summary = "Update the preferred display language")
    public ResponseEntity<ProfileResponse> updateLanguage(Authentication authentication,
                                                            @Valid @RequestBody UpdateLanguageRequest request) {
        return ResponseEntity.ok(profileService.updateLanguage(authentication.getName(), request));
    }

    @PatchMapping("/address")
    @Operation(summary = "Update the primary residential address fields on the profile")
    public ResponseEntity<ProfileResponse> updateAddress(Authentication authentication,
                                                          @Valid @RequestBody UpdateAddressRequest request) {
        return ResponseEntity.ok(profileService.updateAddress(authentication.getName(), request));
    }

    @GetMapping("/addresses")
    @Operation(summary = "List all saved addresses in the address book")
    public ResponseEntity<List<AddressResponse>> getAddresses(Authentication authentication) {
        return ResponseEntity.ok(profileService.getAddresses(authentication.getName()));
    }

    @PostMapping("/addresses")
    @Operation(summary = "Add a new address to the address book")
    public ResponseEntity<AddressResponse> addAddress(Authentication authentication,
                                                       @Valid @RequestBody SaveAddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profileService.addAddress(authentication.getName(), request));
    }

    @PutMapping("/addresses/{id}")
    @Operation(summary = "Update an existing address book entry")
    public ResponseEntity<AddressResponse> updateAddress(Authentication authentication,
                                                          @PathVariable("id") UUID id,
                                                          @Valid @RequestBody SaveAddressRequest request) {
        return ResponseEntity.ok(profileService.updateAddressBookEntry(authentication.getName(), id, request));
    }

    @DeleteMapping("/addresses/{id}")
    @Operation(summary = "Remove an address book entry")
    public ResponseEntity<Void> deleteAddress(Authentication authentication, @PathVariable("id") UUID id) {
        profileService.deleteAddress(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
