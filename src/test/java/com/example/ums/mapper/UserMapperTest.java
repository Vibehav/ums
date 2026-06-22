package com.example.ums.mapper;

import com.example.ums.dto.UserCreateRequest;
import com.example.ums.dto.UserResponse;
import com.example.ums.dto.UserUpdateRequest;
import com.example.ums.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        // Just instantiate directly, it's a simple @Component
        userMapper = new UserMapper();
    }

    // ==========================================
    // 1. toEntity() TESTS
    // ==========================================

    @Test
    @DisplayName("Should map all fields from CreateRequest to Entity and normalize Email/PAN")
    void toEntity_ValidRequest_MapsAndNormalizesCorrectly() {
        UserCreateRequest request = UserCreateRequest.builder()
                .name("John Doe")
                .email("  JoHn.DoE@ExAmPlE.cOm  ") // Mixed case with leading/trailing spaces
                .primaryMobile("9876543210")
                .secondaryMobile("8765432109")
                .aadhaar("[Aadhaar Redacted]") // Placeholder for testing
                .pan("  abcde1234f  ") // Lowercase with spaces
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .placeOfBirth("Mumbai")
                .currentAddress("Current Addr")
                .permanentAddress("Perm Addr")
                .build();

        User entity = userMapper.toEntity(request);

        assertNotNull(entity);
        assertEquals("John Doe", entity.getName());
        assertEquals("9876543210", entity.getPrimaryMobile());
        assertEquals("8765432109", entity.getSecondaryMobile());
        assertEquals("[Aadhaar Redacted]", entity.getAadhaar());
        assertEquals(LocalDate.of(1990, 1, 1), entity.getDateOfBirth());
        assertEquals("Mumbai", entity.getPlaceOfBirth());
        assertEquals("Current Addr", entity.getCurrentAddress());
        assertEquals("Perm Addr", entity.getPermanentAddress());

        // Verify Normalization Logic
        assertEquals("john.doe@example.com", entity.getEmail(), "Email should be lowercase and trimmed");
        assertEquals("ABCDE1234F", entity.getPan(), "PAN should be uppercase and trimmed");
    }

    @Test
    @DisplayName("Should handle null Email and PAN safely during toEntity mapping")
    void toEntity_NullNormalizableFields_DoesNotThrow() {
        UserCreateRequest request = UserCreateRequest.builder()
                .name("Jane Doe")
                .email(null)
                .pan(null)
                .build();

        User entity = userMapper.toEntity(request);

        assertNotNull(entity);
        assertNull(entity.getEmail());
        assertNull(entity.getPan());
    }

    // ==========================================
    // 2. toResponse() TESTS
    // ==========================================

    @Test
    @DisplayName("Should map all fields, including auditing timestamps, from Entity to Response")
    void toResponse_ValidEntity_MapsCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        User entity = User.builder()
                .id(100L)
                .name("Alice")
                .email("alice@example.com")
                .primaryMobile("1111111111")
                .secondaryMobile("2222222222")
                .aadhaar("[Aadhaar Redacted]")
                .pan("ZZZZZ9999Z")
                .dateOfBirth(LocalDate.of(1995, 5, 5))
                .placeOfBirth("Delhi")
                .currentAddress("Addr 1")
                .permanentAddress("Addr 2")
                .createdAt(now.minusDays(1))
                .updatedAt(now)
                .build();

        UserResponse response = userMapper.toResponse(entity);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Alice", response.getName());
        assertEquals("alice@example.com", response.getEmail());
        assertEquals("1111111111", response.getPrimaryMobile());
        assertEquals("2222222222", response.getSecondaryMobile());
        assertEquals("[Aadhaar Redacted]", response.getAadhaar());
        assertEquals("ZZZZZ9999Z", response.getPan());
        assertEquals(LocalDate.of(1995, 5, 5), response.getDateOfBirth());
        assertEquals("Delhi", response.getPlaceOfBirth());
        assertEquals("Addr 1", response.getCurrentAddress());
        assertEquals("Addr 2", response.getPermanentAddress());
        assertEquals(now.minusDays(1), response.getCreatedAt());
        assertEquals(now, response.getUpdatedAt());
    }

    // ==========================================
    // 3. applyUpdate() TESTS
    // ==========================================

    @Test
    @DisplayName("Should update existing entity fields when request fields are not null, including normalization")
    void applyUpdate_AllFieldsProvided_UpdatesAndNormalizes() {
        User existingUser = User.builder()
                .name("Old Name")
                .email("old@example.com")
                .pan("OLDPA1234N")
                .primaryMobile("0000000000")
                .build();

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setName("New Name");
        updateRequest.setEmail("  NEW@EXAMPLE.COM  "); // Needs normalization
        updateRequest.setPan("  newpa9876n  "); // Needs normalization
        updateRequest.setPrimaryMobile("9999999999");
        updateRequest.setSecondaryMobile("8888888888");
        updateRequest.setAadhaar("[Aadhaar Redacted]");
        updateRequest.setDateOfBirth(LocalDate.of(2000, 1, 1));
        updateRequest.setPlaceOfBirth("Pune");
        updateRequest.setCurrentAddress("New Current");
        updateRequest.setPermanentAddress("New Permanent");

        userMapper.applyUpdate(existingUser, updateRequest);

        assertEquals("New Name", existingUser.getName());
        assertEquals("new@example.com", existingUser.getEmail()); // Verified normalization
        assertEquals("NEWPA9876N", existingUser.getPan()); // Verified normalization
        assertEquals("9999999999", existingUser.getPrimaryMobile());
        assertEquals("8888888888", existingUser.getSecondaryMobile());
        assertEquals("[Aadhaar Redacted]", existingUser.getAadhaar());
        assertEquals(LocalDate.of(2000, 1, 1), existingUser.getDateOfBirth());
        assertEquals("Pune", existingUser.getPlaceOfBirth());
        assertEquals("New Current", existingUser.getCurrentAddress());
        assertEquals("New Permanent", existingUser.getPermanentAddress());
    }

    @Test
    @DisplayName("Should ignore null fields in the request and NOT overwrite existing entity data")
    void applyUpdate_NullFieldsInRequest_LeavesEntityUnchanged() {
        User existingUser = User.builder()
                .name("Original Name")
                .email("original@example.com")
                .primaryMobile("1234567890")
                .pan("ORIGI1234N")
                .build();

        // Only updating the Name. Everything else is null.
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setName("Updated Name");

        userMapper.applyUpdate(existingUser, updateRequest);

        // This field should change
        assertEquals("Updated Name", existingUser.getName());

        // These fields must remain exactly as they were
        assertEquals("original@example.com", existingUser.getEmail());
        assertEquals("1234567890", existingUser.getPrimaryMobile());
        assertEquals("ORIGI1234N", existingUser.getPan());
    }
}