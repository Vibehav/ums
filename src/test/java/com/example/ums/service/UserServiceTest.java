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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.LocalDateTime;

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

    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setAadhaar("[Aadhaar Redacted]");

        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setEmail("test@example.com");
    }

    // ==========================================
    // CREATE TESTS
    // ==========================================

    @Test
    void create_ValidRequest_Success() {
        UserCreateRequest request = new UserCreateRequest();
        request.setEmail("test@example.com");

        when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());
        when(userRepository.findByAadhaar(any())).thenReturn(Optional.empty());
        when(userRepository.findByPanIgnoreCase(any())).thenReturn(Optional.empty());
        when(userMapper.toEntity(request)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository).save(user);
    }

    @Test
    void create_DuplicateActiveAadhaar_ThrowsDuplicateResourceException() {
        UserCreateRequest request = new UserCreateRequest();
        request.setAadhaar("123456789012");

        UserStatusView activeUser = mock(UserStatusView.class);
        when(activeUser.getDeletedAt()).thenReturn(null); // Active

        when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());
        when(userRepository.findByAadhaar(request.getAadhaar())).thenReturn(Optional.of(activeUser));

        assertThrows(DuplicateResourceException.class, () -> userService.create(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_InactivePan_ThrowsAccountInactiveException() {
        UserCreateRequest request = new UserCreateRequest();
        request.setPan("ABCDE1234F");

        UserStatusView inactiveUser = mock(UserStatusView.class);
        when(inactiveUser.getDeletedAt()).thenReturn(LocalDateTime.now()); // Soft Deleted

        when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());
        when(userRepository.findByAadhaar(any())).thenReturn(Optional.empty());
        when(userRepository.findByPanIgnoreCase(request.getPan())).thenReturn(Optional.of(inactiveUser));

        assertThrows(AccountInactiveException.class, () -> userService.create(request));
        verify(userRepository, never()).save(any());
    }


    // UPDATE TESTS
    @Test
    void update_ValidRequest_Success() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("updated@example.com");

        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());
        when(userRepository.findByAadhaar(any())).thenReturn(Optional.empty());
        when(userRepository.findByPanIgnoreCase(any())).thenReturn(Optional.empty());
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.update(1L, request);

        assertNotNull(result);
        verify(userMapper).applyUpdate(user, request);
    }

    @Test
    void update_UserNotFound_ThrowsException() {
        UserUpdateRequest request = new UserUpdateRequest();
        when(userRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.update(99L, request));
    }

    @Test
    void update_SameEmailExcludeId_Success() {
        // Tests the logic: if existing.getId().equals(excludeId) -> return;
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("test@example.com");

        UserStatusView myself = mock(UserStatusView.class);
        when(myself.getId()).thenReturn(1L); // Same ID as the user being updated

        when(userRepository.findActiveById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmailIgnoreCase(request.getEmail())).thenReturn(Optional.of(myself));

        // This should NOT throw an exception because the IDs match
        assertDoesNotThrow(() -> userService.update(1L, request));
    }

}