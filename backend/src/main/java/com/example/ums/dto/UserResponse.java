package com.example.ums.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String primaryMobile;
    private String secondaryMobile;
    private String aadhaar;
    private String pan;
    private LocalDate dateOfBirth;
    private String placeOfBirth;
    private String currentAddress;
    private String permanentAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
