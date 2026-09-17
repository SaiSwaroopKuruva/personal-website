package finadvisor.service.impl;

import finadvisor.dto.profile.SaveAddressRequest;
import finadvisor.dto.profile.UpdateLanguageRequest;
import finadvisor.dto.profile.UpdateProfileRequest;
import finadvisor.entity.AddressType;
import finadvisor.entity.Gender;
import finadvisor.entity.User;
import finadvisor.entity.UserAddress;
import finadvisor.exception.UserNotFoundException;
import finadvisor.mapper.AddressMapper;
import finadvisor.mapper.ProfileMapper;
import finadvisor.repository.UserAddressRepository;
import finadvisor.repository.UserRepository;
import finadvisor.security.RequestMetadata;
import finadvisor.security.RequestMetadataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserAddressRepository userAddressRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private RequestMetadataProvider requestMetadataProvider;

    private ProfileServiceImpl profileService;

    @BeforeEach
    void setUp() {
        profileService = new ProfileServiceImpl(userRepository, userAddressRepository,
                new ProfileMapper(), new AddressMapper(), eventPublisher, requestMetadataProvider);
        lenient().when(requestMetadataProvider.current()).thenReturn(new RequestMetadata("127.0.0.1", "test-agent"));
    }

    private User sampleUser() {
        return User.builder().id(UUID.randomUUID()).firstName("Asha").lastName("Rao")
                .email("asha.rao@example.com").build();
    }

    @Test
    void getProfile_shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getProfile("missing@example.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateProfile_shouldUpdateFieldsAndMarkIncompleteWhenMissingData() {
        User user = sampleUser();
        UpdateProfileRequest request = new UpdateProfileRequest("Asha", "Rao", LocalDate.of(1990, 1, 1),
                Gender.FEMALE, "Engineer", BigDecimal.valueOf(1200000), BigDecimal.valueOf(40000), "ABCDE1234F", "1234");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = profileService.updateProfile(user.getEmail(), request);

        assertThat(response.occupation()).isEqualTo("Engineer");
        assertThat(response.panNumber()).isEqualTo("ABCDE1234F");
        assertThat(response.profileCompleted()).isFalse(); // city/state/country/postalCode still missing
    }

    @Test
    void updateLanguage_shouldPersistPreferredLanguage() {
        User user = sampleUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = profileService.updateLanguage(user.getEmail(), new UpdateLanguageRequest("en-IN"));

        assertThat(response.preferredLanguage()).isEqualTo("en-IN");
    }

    @Test
    void addAddress_shouldClearExistingDefaultWhenNewAddressIsDefault() {
        User user = sampleUser();
        UserAddress existingDefault = UserAddress.builder().id(UUID.randomUUID()).user(user).isDefault(true).build();
        SaveAddressRequest request = new SaveAddressRequest(AddressType.HOME, "221B Baker Street", null,
                "Mumbai", "Maharashtra", "India", "400001", true);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userAddressRepository.findByUser_IdAndIsDefaultTrue(user.getId())).thenReturn(List.of(existingDefault));
        when(userAddressRepository.save(any(UserAddress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = profileService.addAddress(user.getEmail(), request);

        assertThat(existingDefault.isDefault()).isFalse();
        assertThat(response.isDefault()).isTrue();
        assertThat(response.postalCode()).isEqualTo("400001");
    }
}
