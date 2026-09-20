package finadvisor.service.impl;

import finadvisor.dto.profile.AddressResponse;
import finadvisor.dto.profile.ProfileResponse;
import finadvisor.dto.profile.SaveAddressRequest;
import finadvisor.dto.profile.UpdateAddressRequest;
import finadvisor.dto.profile.UpdateLanguageRequest;
import finadvisor.dto.profile.UpdatePreferencesRequest;
import finadvisor.dto.profile.UpdateProfileRequest;
import finadvisor.entity.User;
import finadvisor.entity.UserAddress;
import finadvisor.events.AuditEvent;
import finadvisor.exception.AddressNotFoundException;
import finadvisor.exception.UserNotFoundException;
import finadvisor.mapper.AddressMapper;
import finadvisor.mapper.ProfileMapper;
import finadvisor.repository.UserAddressRepository;
import finadvisor.repository.UserRepository;
import finadvisor.security.RequestMetadataProvider;
import finadvisor.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final ProfileMapper profileMapper;
    private final AddressMapper addressMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final RequestMetadataProvider requestMetadataProvider;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String email) {
        return profileMapper.toResponse(findUser(email));
    }

    @Override
    public ProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = findUser(email);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setDateOfBirth(request.dateOfBirth());
        user.setGender(request.gender());
        user.setOccupation(request.occupation());
        user.setAnnualIncome(request.annualIncome());
        user.setMonthlyExpenses(request.monthlyExpenses());
        user.setPanNumber(request.panNumber());
        user.setAadhaarLastFour(request.aadhaarLastFour());
        user.setProfileCompleted(isProfileComplete(user));
        User saved = userRepository.save(user);
        publishAudit(user, "PROFILE_UPDATED", "Profile details updated");
        return profileMapper.toResponse(saved);
    }

    @Override
    public ProfileResponse updatePhoto(String email, String photoUrl) {
        User user = findUser(email);
        user.setProfilePicture(photoUrl);
        User saved = userRepository.save(user);
        publishAudit(user, "PROFILE_PHOTO_UPDATED", "Profile photo updated");
        return profileMapper.toResponse(saved);
    }

    @Override
    public ProfileResponse deletePhoto(String email) {
        User user = findUser(email);
        user.setProfilePicture(null);
        User saved = userRepository.save(user);
        publishAudit(user, "PROFILE_PHOTO_REMOVED", "Profile photo removed");
        return profileMapper.toResponse(saved);
    }

    @Override
    public ProfileResponse updatePreferences(String email, UpdatePreferencesRequest request) {
        User user = findUser(email);
        user.setInvestmentExperience(request.investmentExperience());
        user.setInvestmentHorizon(request.investmentHorizon());
        user.setMonthlyInvestmentBudget(request.monthlyInvestmentBudget());
        User saved = userRepository.save(user);
        publishAudit(user, "PROFILE_PREFERENCES_UPDATED", "Investor preferences updated");
        return profileMapper.toResponse(saved);
    }

    @Override
    public ProfileResponse updateLanguage(String email, UpdateLanguageRequest request) {
        User user = findUser(email);
        user.setPreferredLanguage(request.preferredLanguage());
        User saved = userRepository.save(user);
        publishAudit(user, "PROFILE_LANGUAGE_UPDATED", "Preferred language updated to " + request.preferredLanguage());
        return profileMapper.toResponse(saved);
    }

    @Override
    public ProfileResponse updateAddress(String email, UpdateAddressRequest request) {
        User user = findUser(email);
        user.setCity(request.city());
        user.setState(request.state());
        user.setCountry(request.country());
        user.setPostalCode(request.postalCode());
        user.setProfileCompleted(isProfileComplete(user));
        User saved = userRepository.save(user);
        publishAudit(user, "PROFILE_ADDRESS_UPDATED", "Primary address updated");
        return profileMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses(String email) {
        User user = findUser(email);
        return userAddressRepository.findByUser_IdOrderByIsDefaultDescCreatedAtDesc(user.getId()).stream()
                .map(addressMapper::toResponse)
                .toList();
    }

    @Override
    public AddressResponse addAddress(String email, SaveAddressRequest request) {
        User user = findUser(email);
        if (request.isDefault()) {
            clearExistingDefault(user.getId());
        }
        UserAddress address = UserAddress.builder()
                .user(user)
                .type(request.type())
                .addressLine1(request.addressLine1())
                .addressLine2(request.addressLine2())
                .city(request.city())
                .state(request.state())
                .country(request.country())
                .postalCode(request.postalCode())
                .isDefault(request.isDefault())
                .build();
        UserAddress saved = userAddressRepository.save(address);
        publishAudit(user, "ADDRESS_ADDED", "Address added to address book");
        return addressMapper.toResponse(saved);
    }

    @Override
    public AddressResponse updateAddressBookEntry(String email, UUID addressId, SaveAddressRequest request) {
        User user = findUser(email);
        UserAddress address = userAddressRepository.findByIdAndUser_Id(addressId, user.getId())
                .orElseThrow(() -> new AddressNotFoundException("Address not found"));
        if (request.isDefault() && !address.isDefault()) {
            clearExistingDefault(user.getId());
        }
        address.setType(request.type());
        address.setAddressLine1(request.addressLine1());
        address.setAddressLine2(request.addressLine2());
        address.setCity(request.city());
        address.setState(request.state());
        address.setCountry(request.country());
        address.setPostalCode(request.postalCode());
        address.setDefault(request.isDefault());
        UserAddress saved = userAddressRepository.save(address);
        publishAudit(user, "ADDRESS_UPDATED", "Address book entry updated");
        return addressMapper.toResponse(saved);
    }

    @Override
    public void deleteAddress(String email, UUID addressId) {
        User user = findUser(email);
        UserAddress address = userAddressRepository.findByIdAndUser_Id(addressId, user.getId())
                .orElseThrow(() -> new AddressNotFoundException("Address not found"));
        userAddressRepository.delete(address);
        publishAudit(user, "ADDRESS_REMOVED", "Address book entry removed");
    }

    private void clearExistingDefault(UUID userId) {
        userAddressRepository.findByUser_IdAndIsDefaultTrue(userId)
                .forEach(existing -> {
                    existing.setDefault(false);
                    userAddressRepository.save(existing);
                });
    }

    private boolean isProfileComplete(User user) {
        return user.getFirstName() != null && user.getLastName() != null
                && user.getDateOfBirth() != null && user.getGender() != null
                && user.getOccupation() != null && user.getCity() != null
                && user.getState() != null && user.getCountry() != null
                && user.getPostalCode() != null && user.getPanNumber() != null;
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private void publishAudit(User user, String action, String details) {
        eventPublisher.publishEvent(new AuditEvent(user.getId(), action, details,
                requestMetadataProvider.current().ipAddress()));
    }
}
