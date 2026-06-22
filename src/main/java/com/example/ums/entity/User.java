package com.example.ums.entity;

import com.example.ums.validation.ValidAadhaar;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "users", uniqueConstraints = {@UniqueConstraint(name = "uq_users_email", columnNames = "email"),
                            @UniqueConstraint(name = "uq_users_aadhaar", columnNames = "aadhaar"),
                            @UniqueConstraint(name = "uq_users_pan", columnNames = "pan")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @Column(name = "primary_mobile", nullable = false, length = 15)
    private String primaryMobile;

    @Column(name = "secondary_mobile", length = 15)
    private String secondaryMobile;


    @ValidAadhaar
    private String aadhaar;

    @Column(name = "pan", length = 10)
    private String pan;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "place_of_birth", length = 150)
    private String placeOfBirth;

    @Column(name = "current_address", length = 500)
    private String currentAddress;

    @Column(name = "permanent_address", length = 500)
    private String permanentAddress;

    // TimeStamps
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
