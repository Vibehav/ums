package com.example.ums.mapper;

import com.example.ums.dto.UserCreateRequest;
import com.example.ums.dto.UserResponse;
import com.example.ums.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toEntity(UserCreateRequest request) {
        return User.builder()
                .name(request.getName())
                .email(normalizeEmail(request.getEmail()))
                .primaryMobile(request.getPrimaryMobile())
                .secondaryMobile(request.getSecondaryMobile())
                .aadhaar(request.getAadhaar())
                .pan(normalizePan(request.getPan()))
                .dateOfBirth(request.getDateOfBirth())
                .placeOfBirth(request.getPlaceOfBirth())
                .currentAddress(request.getCurrentAddress())
                .permanentAddress(request.getPermanentAddress())
                .build();
    }


    private String normalizeEmail(String email) {
        return email == null?null : email.trim().toLowerCase();
    }

    private String normalizePan(String pan) {
        return pan == null?null : pan.trim().toUpperCase();
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .primaryMobile(user.getPrimaryMobile())
                .secondaryMobile(user.getSecondaryMobile())
                .aadhaar(user.getAadhaar())
                .pan(user.getPan())
                .dateOfBirth(user.getDateOfBirth())
                .placeOfBirth(user.getPlaceOfBirth())
                .currentAddress(user.getCurrentAddress())
                .permanentAddress(user.getPermanentAddress())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }


}
