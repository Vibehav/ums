package com.example.ums.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 254, message = "Email must not exceed 254 characters")
    private String email;

    @NotBlank(message = "Primary mobile is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Primary mobile must be a valid 10-digit Indian mobile number")
    private String primaryMobile;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Secondary mobile must be a valid 10-digit Indian mobile number")
    private String secondaryMobile;

    @NotBlank
    @Pattern(regexp = "^[0-9]{12}",message = "Aadhaar must be a valid 12-digit number")
    private String aadhaar;

    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]$", message = "PAN must match the format AAAAA9999A")
    private String pan;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Size(max = 150, message = "Place of birth must not exceed 150 characters")
    private String placeOfBirth;

    @Size(max = 500, message = "Current address must not exceed 500 characters")
    private String currentAddress;

    @Size(max = 500, message = "Permanent address must not exceed 500 characters")
    private String permanentAddress;
}
