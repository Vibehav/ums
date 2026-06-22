package com.example.ums.service;

import com.example.ums.dto.PagedResponse;
import com.example.ums.dto.UserCreateRequest;
import com.example.ums.dto.UserResponse;
import com.example.ums.dto.UserUpdateRequest;
import com.example.ums.entity.User;
import com.example.ums.exception.AccountInactiveException;
import com.example.ums.exception.DuplicateResourceException;
import com.example.ums.exception.UserNotFoundException;
import com.example.ums.mapper.UserMapper;
import com.example.ums.repository.UserRepository;
import com.example.ums.repository.UserStatusView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private UserCreateRequest baseRequest;
    private User mockUserEntity;
    private UserResponse mockUserResponse;

    @BeforeEach
    void setUp() {
        // A complete request with all unique identifiers populated
        baseRequest = new UserCreateRequest();
        baseRequest.setEmail("test@example.com");
        baseRequest.setAadhaar("123456789012");
        baseRequest.setPan("ABCDE1234F");

        mockUserEntity = new User();
        mockUserEntity.setId(1L);

        mockUserResponse = new UserResponse();
        mockUserResponse.setId(1L);
    }

    // ==========================================
    // 1. HAPPY PATHS (SUCCESS SCENARIOS)
    // ==========================================

    @Test
    @DisplayName("Should create user successfully when all unique fields are available")
    void create_ValidRequest_Success() {
        when(userRepository.findByEmailIgnoreCase(baseRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByAadhaar(baseRequest.getAadhaar())).thenReturn(Optional.empty());
        when(userRepository.findByPanIgnoreCase(baseRequest.getPan())).thenReturn(Optional.empty());

        when(userMapper.toEntity(baseRequest)).thenReturn(mockUserEntity);
        when(userRepository.save(mockUserEntity)).thenReturn(mockUserEntity);
        when(userMapper.toResponse(mockUserEntity)).thenReturn(mockUserResponse);

        UserResponse result = userService.create(baseRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository).save(mockUserEntity);
    }

    @Test
    @DisplayName("Should bypass validation and create user successfully when optional unique fields (PAN/Aadhaar) are null")
    void create_ValidRequestWithNullOptionalFields_Success() {
        // Set optional unique constraints to null
        baseRequest.setAadhaar(null);
        baseRequest.setPan("   "); // Test blank string bypass as well

        when(userRepository.findByEmailIgnoreCase(baseRequest.getEmail())).thenReturn(Optional.empty());
        // We DO NOT mock findByAadhaar or findByPan because StringUtils.hasText() should return false and skip DB calls

        when(userMapper.toEntity(baseRequest)).thenReturn(mockUserEntity);
        when(userRepository.save(mockUserEntity)).thenReturn(mockUserEntity);
        when(userMapper.toResponse(mockUserEntity)).thenReturn(mockUserResponse);

        UserResponse result = userService.create(baseRequest);

        assertNotNull(result);
        // Verify that the repository was NEVER queried for Aadhaar or PAN
        verify(userRepository, never()).findByAadhaar(any());
        verify(userRepository, never()).findByPanIgnoreCase(any());
        verify(userRepository).save(mockUserEntity);
    }

    // ==========================================
    // 2. EMAIL CONFLICTS
    // ==========================================

    @Test
    @DisplayName("Should throw DuplicateResourceException when Email belongs to an active user")
    void create_ActiveEmail_ThrowsDuplicateResourceException() {
        UserStatusView activeUser = mock(UserStatusView.class);
        when(activeUser.getDeletedAt()).thenReturn(null); // Active

        when(userRepository.findByEmailIgnoreCase(baseRequest.getEmail())).thenReturn(Optional.of(activeUser));

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () -> userService.create(baseRequest));
        assertTrue(ex.getMessage().contains("already exists"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw AccountInactiveException when Email belongs to a softly deleted user")
    void create_InactiveEmail_ThrowsAccountInactiveException() {
        UserStatusView inactiveUser = mock(UserStatusView.class);
        when(inactiveUser.getDeletedAt()).thenReturn(LocalDateTime.now()); // Soft deleted

        when(userRepository.findByEmailIgnoreCase(baseRequest.getEmail())).thenReturn(Optional.of(inactiveUser));

        AccountInactiveException ex = assertThrows(AccountInactiveException.class, () -> userService.create(baseRequest));
        assertTrue(ex.getMessage().contains("inactive account"));
        verify(userRepository, never()).save(any());
    }

    // ==========================================
    // 3. AADHAAR CONFLICTS
    // ==========================================

    @Test
    @DisplayName("Should throw DuplicateResourceException when Aadhaar belongs to an active user")
    void create_ActiveAadhaar_ThrowsDuplicateResourceException() {
        UserStatusView activeUser = mock(UserStatusView.class);
        when(activeUser.getDeletedAt()).thenReturn(null); // Active

        when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());
        when(userRepository.findByAadhaar(baseRequest.getAadhaar())).thenReturn(Optional.of(activeUser));

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () -> userService.create(baseRequest));
        assertTrue(ex.getMessage().contains("Aadhaar number already exists"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw AccountInactiveException when Aadhaar belongs to a softly deleted user")
    void create_InactiveAadhaar_ThrowsAccountInactiveException() {
        UserStatusView inactiveUser = mock(UserStatusView.class);
        when(inactiveUser.getDeletedAt()).thenReturn(LocalDateTime.now()); // Soft deleted

        when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());
        when(userRepository.findByAadhaar(baseRequest.getAadhaar())).thenReturn(Optional.of(inactiveUser));

        AccountInactiveException ex = assertThrows(AccountInactiveException.class, () -> userService.create(baseRequest));
        assertTrue(ex.getMessage().contains("inactive account"));
        verify(userRepository, never()).save(any());
    }

    // ==========================================
    // 4. PAN CONFLICTS
    // ==========================================

    @Test
    @DisplayName("Should throw DuplicateResourceException when PAN belongs to an active user")
    void create_ActivePan_ThrowsDuplicateResourceException() {
        UserStatusView activeUser = mock(UserStatusView.class);
        when(activeUser.getDeletedAt()).thenReturn(null); // Active

        when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());
        when(userRepository.findByAadhaar(any())).thenReturn(Optional.empty());
        when(userRepository.findByPanIgnoreCase(baseRequest.getPan())).thenReturn(Optional.of(activeUser));

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () -> userService.create(baseRequest));
        assertTrue(ex.getMessage().contains("PAN already exists"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw AccountInactiveException when PAN belongs to a softly deleted user")
    void create_InactivePan_ThrowsAccountInactiveException() {
        UserStatusView inactiveUser = mock(UserStatusView.class);
        when(inactiveUser.getDeletedAt()).thenReturn(LocalDateTime.now()); // Soft deleted

        when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());
        when(userRepository.findByAadhaar(any())).thenReturn(Optional.empty());
        when(userRepository.findByPanIgnoreCase(baseRequest.getPan())).thenReturn(Optional.of(inactiveUser));

        AccountInactiveException ex = assertThrows(AccountInactiveException.class, () -> userService.create(baseRequest));
        assertTrue(ex.getMessage().contains("inactive account"));
        verify(userRepository, never()).save(any());
    }
    // ==========================================
    // 5. UPDATE: HAPPY PATHS & NOT FOUND
    // ==========================================

    @Test
    @DisplayName("Should update user successfully when there are no conflicts")
    void update_ValidRequest_Success() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setEmail("new.email@example.com");
        // Aadhaar and PAN are null, so their DB checks will be skipped by StringUtils.hasText()

        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(mockUserEntity));
        when(userRepository.findByEmailIgnoreCase(updateRequest.getEmail())).thenReturn(Optional.empty());

        when(userMapper.toResponse(mockUserEntity)).thenReturn(mockUserResponse);

        UserResponse result = userService.update(1L, updateRequest);

        assertNotNull(result);
        verify(userMapper).applyUpdate(mockUserEntity, updateRequest);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException if the user to update does not exist or is softly deleted")
    void update_UserNotFound_ThrowsException() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();

        when(userRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.update(99L, updateRequest));
        verify(userMapper, never()).applyUpdate(any(), any());
    }

    @Test
    @DisplayName("Should bypass validation and update successfully if the matching unique fields belong to the SAME user being updated")
    void update_SameUserBypass_Success() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setEmail("my.own.email@example.com");

        UserStatusView myself = mock(UserStatusView.class);
        when(myself.getId()).thenReturn(1L); // The IDs MATCH

        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(mockUserEntity));
        when(userRepository.findByEmailIgnoreCase(updateRequest.getEmail())).thenReturn(Optional.of(myself));

        when(userMapper.toResponse(mockUserEntity)).thenReturn(mockUserResponse);

        assertDoesNotThrow(() -> userService.update(1L, updateRequest));
        verify(userMapper).applyUpdate(mockUserEntity, updateRequest);
    }

    // ==========================================
    // 6. UPDATE: EMAIL CONFLICTS
    // ==========================================

    @Test
    @DisplayName("Should throw DuplicateResourceException on update when Email belongs to a DIFFERENT active user")
    void update_ActiveEmailConflict_ThrowsDuplicateResourceException() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setEmail("taken@example.com");

        UserStatusView differentActiveUser = mock(UserStatusView.class);
        when(differentActiveUser.getId()).thenReturn(2L);
        when(differentActiveUser.getDeletedAt()).thenReturn(null);

        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(mockUserEntity));
        when(userRepository.findByEmailIgnoreCase(updateRequest.getEmail())).thenReturn(Optional.of(differentActiveUser));

        assertThrows(DuplicateResourceException.class, () -> userService.update(1L, updateRequest));
        verify(userMapper, never()).applyUpdate(any(), any());
    }

    @Test
    @DisplayName("Should throw AccountInactiveException on update when Email belongs to a DIFFERENT soft-deleted user")
    void update_InactiveEmailConflict_ThrowsAccountInactiveException() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setEmail("deleted@example.com");

        UserStatusView differentInactiveUser = mock(UserStatusView.class);
        when(differentInactiveUser.getId()).thenReturn(2L);
        when(differentInactiveUser.getDeletedAt()).thenReturn(LocalDateTime.now());

        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(mockUserEntity));
        when(userRepository.findByEmailIgnoreCase(updateRequest.getEmail())).thenReturn(Optional.of(differentInactiveUser));

        assertThrows(AccountInactiveException.class, () -> userService.update(1L, updateRequest));
        verify(userMapper, never()).applyUpdate(any(), any());
    }

    // ==========================================
    // 7. UPDATE: AADHAAR CONFLICTS
    // ==========================================

    @Test
    @DisplayName("Should throw DuplicateResourceException on update when Aadhaar belongs to a DIFFERENT active user")
    void update_ActiveAadhaarConflict_ThrowsDuplicateResourceException() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setAadhaar("987654321098");

        UserStatusView differentActiveUser = mock(UserStatusView.class);
        when(differentActiveUser.getId()).thenReturn(2L);
        when(differentActiveUser.getDeletedAt()).thenReturn(null);

        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(mockUserEntity));
        when(userRepository.findByAadhaar(updateRequest.getAadhaar())).thenReturn(Optional.of(differentActiveUser));

        assertThrows(DuplicateResourceException.class, () -> userService.update(1L, updateRequest));
        verify(userMapper, never()).applyUpdate(any(), any());
    }

    @Test
    @DisplayName("Should throw AccountInactiveException on update when Aadhaar belongs to a DIFFERENT soft-deleted user")
    void update_InactiveAadhaarConflict_ThrowsAccountInactiveException() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setAadhaar("987654321098");

        UserStatusView differentInactiveUser = mock(UserStatusView.class);
        when(differentInactiveUser.getId()).thenReturn(2L);
        when(differentInactiveUser.getDeletedAt()).thenReturn(LocalDateTime.now());

        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(mockUserEntity));
        when(userRepository.findByAadhaar(updateRequest.getAadhaar())).thenReturn(Optional.of(differentInactiveUser));

        assertThrows(AccountInactiveException.class, () -> userService.update(1L, updateRequest));
        verify(userMapper, never()).applyUpdate(any(), any());
    }

    // ==========================================
    // 8. UPDATE: PAN CONFLICTS
    // ==========================================

    @Test
    @DisplayName("Should throw DuplicateResourceException on update when PAN belongs to a DIFFERENT active user")
    void update_ActivePanConflict_ThrowsDuplicateResourceException() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setPan("ZYXWV9876U");

        UserStatusView differentActiveUser = mock(UserStatusView.class);
        when(differentActiveUser.getId()).thenReturn(2L);
        when(differentActiveUser.getDeletedAt()).thenReturn(null);

        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(mockUserEntity));
        when(userRepository.findByPanIgnoreCase(updateRequest.getPan())).thenReturn(Optional.of(differentActiveUser));

        assertThrows(DuplicateResourceException.class, () -> userService.update(1L, updateRequest));
        verify(userMapper, never()).applyUpdate(any(), any());
    }

    @Test
    @DisplayName("Should throw AccountInactiveException on update when PAN belongs to a DIFFERENT soft-deleted user")
    void update_InactivePanConflict_ThrowsAccountInactiveException() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setPan("ZYXWV9876U");

        UserStatusView differentInactiveUser = mock(UserStatusView.class);
        when(differentInactiveUser.getId()).thenReturn(2L);
        when(differentInactiveUser.getDeletedAt()).thenReturn(LocalDateTime.now());

        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(mockUserEntity));
        when(userRepository.findByPanIgnoreCase(updateRequest.getPan())).thenReturn(Optional.of(differentInactiveUser));

        assertThrows(AccountInactiveException.class, () -> userService.update(1L, updateRequest));
        verify(userMapper, never()).applyUpdate(any(), any());
    }

    // ==========================================
    // 11. GET BY ID TESTS
    // ==========================================

    @Test
    @DisplayName("Should return UserResponse when getById finds an active user")
    void getById_Success() {
        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(mockUserEntity));
        when(userMapper.toResponse(mockUserEntity)).thenReturn(mockUserResponse);

        UserResponse result = userService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when getById cannot find an active user")
    void getById_NotFound_ThrowsException() {
        when(userRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getById(99L));
        // No mapper stub is needed here because the exception halts execution
        verify(userMapper, never()).toResponse(any());
    }

    // ==========================================
    // 12. GET ALL (PAGINATION) TESTS
    // ==========================================

    @Test
    @DisplayName("Should return paged active users without calling search when search string is null")
    void getAll_WithoutSearch_ReturnsPagedResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(mockUserEntity));

        // ONLY stub findAllActive because StringUtils.hasText(null) is false
        when(userRepository.findAllActive(pageable)).thenReturn(userPage);
        when(userMapper.toResponse(mockUserEntity)).thenReturn(mockUserResponse);

        PagedResponse<UserResponse> result = userService.getAll(pageable, null);

        assertNotNull(result);
        // Verify it routed to the correct DB call
        verify(userRepository).findAllActive(pageable);
        verify(userRepository, never()).searchActive(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should trim search string and return paged active users using searchActive")
    void getAll_WithSearch_ReturnsPagedResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(mockUserEntity));
        String rawSearch = "  John  ";
        String trimmedSearch = "John"; // The service should apply .trim()

        // ONLY stub searchActive because StringUtils.hasText("  John  ") is true
        when(userRepository.searchActive(trimmedSearch, pageable)).thenReturn(userPage);
        when(userMapper.toResponse(mockUserEntity)).thenReturn(mockUserResponse);

        PagedResponse<UserResponse> result = userService.getAll(pageable, rawSearch);

        assertNotNull(result);
        // Verify it routed to the correct DB call and properly trimmed the string
        verify(userRepository).searchActive(trimmedSearch, pageable);
        verify(userRepository, never()).findAllActive(any(Pageable.class));
    }

    // ==========================================
    // 13. GET DELETED USERS
    // ==========================================

    @Test
    @DisplayName("Should return list of mapped deleted users")
    void getDeletedUsers_ReturnsMappedList() {
        // Setup: Ensure our mock entity looks like a deleted user
        mockUserEntity.setDeletedAt(LocalDateTime.now());

        when(userRepository.findAllByDeletedAtIsNotNull()).thenReturn(List.of(mockUserEntity));
        when(userMapper.toResponse(mockUserEntity)).thenReturn(mockUserResponse);

        List<UserResponse> result = userService.getDeletedUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findAllByDeletedAtIsNotNull();
    }
}