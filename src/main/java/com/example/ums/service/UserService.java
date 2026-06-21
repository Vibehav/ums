package com.example.ums.service;

import com.example.ums.dto.UserCreateRequest;
import com.example.ums.dto.UserResponse;
import com.example.ums.entity.User;
import com.example.ums.mapper.UserMapper;
import com.example.ums.repository.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        validateUniqueness(request.getEmail(), request.getAadhaar(), request.getPan(),null);
        User user = userMapper.toEntity(request);
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    private void validateUniqueness(String email, String aadhaar, String pan, Long excludeId) {
        if (StringUtils.hasText(email)) {
            boolean conflict;
            if(excludeId == null) {
             conflict = userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(email);
            } else conflict = userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNullAndIdNot(email, excludeId);

            if (conflict) {
                throw new RuntimeException("A user with email '" + email + "' already exists");
            }
        }
        if (StringUtils.hasText(aadhaar)) {
            boolean conflict;
            conflict = excludeId == null
                    ? userRepository.existsByAadhaarAndDeletedAtIsNull(aadhaar)
                    : userRepository.existsByAadhaarAndDeletedAtIsNullAndIdNot(aadhaar, excludeId);
            if (conflict) {
                throw new RuntimeException("A user with this Aadhaar number already exists");
            }
        }
        if (StringUtils.hasText(pan)) {
            boolean conflict = excludeId == null
                    ? userRepository.existsByPanIgnoreCaseAndDeletedAtIsNull(pan)
                    : userRepository.existsByPanIgnoreCaseAndDeletedAtIsNullAndIdNot(pan, excludeId);
            if (conflict) {
                throw new RuntimeException("A user with this PAN already exists");
            }
        }
    }

}
