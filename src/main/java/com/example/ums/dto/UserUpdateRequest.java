package com.example.ums.dto;

import com.example.ums.validation.ValidAadhaar;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserUpdateRequest {

    @Size(max = 150, message = "Name must not exceed 150 characters")
    private String name;

    @Email(message = "Email must be a valid email address")
    @Size(max = 254, message = "Email must not exceed 254 characters")
    private String email;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Primary mobile must be a valid 10-digit Indian mobile number")
    private String primaryMobile;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Secondary mobile must be a valid 10-digit Indian mobile number")
    private String secondaryMobile;

    @ValidAadhaar
    private String aadhaar;

    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]$", message = "PAN must match the format AAAAA9999A")
    private String pan;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Size(max = 150, message = "Place of birth must not exceed 150 characters")
    private String placeOfBirth;

    @Size(max = 500, message = "Current address must not exceed 500 characters")
    private String currentAddress;

    @Size(max = 500, message = "Permanent address must not exceed 500 characters")
    private String permanentAddress;
}
