package com.example.ums.mapper;

import com.example.ums.dto.UserCreateRequest;
import com.example.ums.dto.UserResponse;
import com.example.ums.dto.UserUpdateRequest;
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
                .aadhaar(maskAadhaar(user.getAadhaar()))
                .pan(maskPan(user.getPan()))
                .dateOfBirth(user.getDateOfBirth())
                .placeOfBirth(user.getPlaceOfBirth())
                .currentAddress(user.getCurrentAddress())
                .permanentAddress(user.getPermanentAddress())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }


    public void applyUpdate(User user, UserUpdateRequest request) {
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            user.setEmail(normalizeEmail(request.getEmail()));
        }

        if (request.getPrimaryMobile() != null) {
            user.setPrimaryMobile(request.getPrimaryMobile());
        }
        if (request.getSecondaryMobile() != null) {
            user.setSecondaryMobile(request.getSecondaryMobile());
        }
        if (request.getAadhaar() != null) {
            user.setAadhaar(request.getAadhaar());
        }
        if (request.getPan() != null) {
            user.setPan(normalizePan(request.getPan()));
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getPlaceOfBirth() != null) {
            user.setPlaceOfBirth(request.getPlaceOfBirth());
        }
        if (request.getCurrentAddress() != null) {
            user.setCurrentAddress(request.getCurrentAddress());
        }
        if (request.getPermanentAddress() != null) {
            user.setPermanentAddress(request.getPermanentAddress());
        }
    }


    private String maskAadhaar(String aadhaar) {
        if (aadhaar == null || aadhaar.length() != 12) {
            return aadhaar;
        }
        return "X".repeat(8) + aadhaar.substring(8);
    }


    private String maskPan(String pan) {
        if (pan == null || pan.length() != 10) {
            return pan;
        }
        return "X".repeat(5) + pan.substring(5);
    }
}
