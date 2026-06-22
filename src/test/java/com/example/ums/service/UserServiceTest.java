package com.example.ums.service;

import com.example.ums.dto.UserCreateRequest;
import com.example.ums.dto.UserResponse;
import com.example.ums.entity.User;
import com.example.ums.exception.AccountInactiveException;
import com.example.ums.exception.DuplicateResourceException;
import com.example.ums.mapper.UserMapper;
import com.example.ums.repository.UserRepository;
import com.example.ums.repository.UserStatusView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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

    private UserCreateRequest validRequest;
    private User mockUser;
    private UserResponse mockResponse;

    @BeforeEach
    void setUp() {
        validRequest = UserCreateRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .primaryMobile("9876543210")
                .aadhaar("[Aadhaar Redacted]") // Replace with valid regex match locally
                .pan("ABCDE1234F")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("john@example.com");

        mockResponse = new UserResponse();
        mockResponse.setId(1L);
        mockResponse.setEmail("john@example.com");
    }

    @Test
    void create_ValidRequest_ReturnsUserResponse() {
        when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());
        when(userRepository.findByAadhaar(any())).thenReturn(Optional.empty());
        when(userRepository.findByPanIgnoreCase(any())).thenReturn(Optional.empty());
        when(userMapper.toEntity(any(UserCreateRequest.class))).thenReturn(mockUser);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(mockResponse);

        UserResponse result = userService.create(validRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_DuplicateActiveEmail_ThrowsDuplicateResourceException() {
        UserStatusView activeExistingUser = mock(UserStatusView.class);
        when(activeExistingUser.getDeletedAt()).thenReturn(null);
        when(userRepository.findByEmailIgnoreCase(validRequest.getEmail())).thenReturn(Optional.of(activeExistingUser));

        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
            userService.create(validRequest);
        });

        assertTrue(exception.getMessage().contains("already exists"));
        verify(userRepository, never()).save(any());
    }
}