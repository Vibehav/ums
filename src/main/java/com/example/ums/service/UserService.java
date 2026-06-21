package com.example.ums.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        validateUserUniqueness(request.getEmail(), request.getAadhaar(), request.getPan(),null);
        User user = userMapper.toEntity(request);
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    @Transactional // Dirty checking , version control at db-level for concurrency
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> new UserNotFoundException("User Not Found: "+ id ));

        validateUserUniqueness(request.getEmail(), request.getAadhaar(), request.getPan(), id);
        userMapper.applyUpdate(user, request);

        return userMapper.toResponse(user);
    }

    private void validateUserUniqueness(String email, String aadhaar, String pan, Long excludeId) {
        validateEmail(email, excludeId);
        validateAadhaar(aadhaar, excludeId);
        validatePan(pan, excludeId);
    }

    private void validateEmail(String email, Long excludeId) {
        if (!StringUtils.hasText(email)) {return;}
        // Check if DB recognize the user (He might be Active or Deleted)
        UserStatusView existing = userRepository.findByEmailIgnoreCase(email).orElse(null);

        validateExistingUser(existing, excludeId,
                "An inactive account with email " + email + " already exists. Please use the recovery flow to reactivate it.",
                "A user with email " + email + " already exists.");
    }

    private void validateAadhaar(String aadhaar, Long excludeId) {
        if (!StringUtils.hasText(aadhaar)) {return;}

        UserStatusView existing = userRepository.findByAadhaar(aadhaar).orElse(null);

        validateExistingUser(existing, excludeId,
                "An inactive account with this Aadhaar already exists. Please use the recovery flow to reactivate it.",
                "A user with this Aadhaar number already exists.");
    }

    private void validatePan(String pan, Long excludeId) {
        if (!StringUtils.hasText(pan)) {return;}

        UserStatusView existing = userRepository.findByPanIgnoreCase(pan).orElse(null);

        validateExistingUser(existing, excludeId,
                "An inactive account with this PAN already exists. Please use the recovery flow to reactivate it.",
                "A user with this PAN already exists.");
    }

    private void validateExistingUser(UserStatusView existing, Long excludeId, String inactiveMessage, String duplicateMessage) {
        // It means user is not in the DB, we can proceed further
        if (existing == null) {
            return;
        }
        // Ensures while UPDATE: user is not searching for itself
        if (excludeId != null && existing.getId().equals(excludeId)) {
            return;
        }

        //If the user has been softly deleted. Exception is thrown
        if (existing.getDeletedAt() != null) {
            throw new AccountInactiveException(inactiveMessage);
        }

        throw new DuplicateResourceException(duplicateMessage);
    }

}
