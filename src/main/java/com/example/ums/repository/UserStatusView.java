package com.example.ums.repository;

import java.time.LocalDateTime;
// Projections: Used to fetch only required data from the db to save memory
public interface UserStatusView {
    Long getId();
    LocalDateTime getDeletedAt();
}
