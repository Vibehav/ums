package com.example.ums.controller;

import com.example.ums.dto.UserCreateRequest;
import com.example.ums.dto.UserResponse;
import com.example.ums.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
