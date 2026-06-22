package com.example.ums.controller;

import com.example.ums.dto.PagedResponse;
import com.example.ums.dto.UserCreateRequest;
import com.example.ums.dto.UserResponse;
import com.example.ums.dto.UserUpdateRequest;
import com.example.ums.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;
    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request,
                                               UriComponentsBuilder uriBuilder) {
        UserResponse created = userService.create(request);
        URI location = uriBuilder.path("/api/v1/users/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update a user")
    public ResponseEntity<UserResponse> update(@PathVariable @Positive Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete an active user by ID")
    public ResponseEntity<Void> softDeleteUser(@PathVariable Long id) {
        userService.softDeleteUser(id);

        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{id}/restore")
    @Operation(summary = "Restore a soft-deleted user account")
    public ResponseEntity<Void> restoreUser(@PathVariable Long id) {
        userService.restoreUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List users with pagination, sorting, and optional search")
    public ResponseEntity<PagedResponse<UserResponse>> getAll(@RequestParam(required = false) String search,
                                                              Pageable pageable) {
        return ResponseEntity.ok(userService.getAll(pageable, search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single user by id")
    public ResponseEntity<UserResponse> getById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }
}
