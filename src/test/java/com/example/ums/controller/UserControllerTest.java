package com.example.ums.controller;

import com.example.ums.dto.PagedResponse;
import com.example.ums.dto.UserCreateRequest;
import com.example.ums.dto.UserResponse;
import com.example.ums.dto.UserUpdateRequest;
import com.example.ums.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Using the Spring Boot 3.4+ annotation
    @MockitoBean
    private UserService userService;

    private UserCreateRequest createRequest;
    private UserUpdateRequest updateRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        createRequest = UserCreateRequest.builder()
                .name("Jane Doe")
                .email("jane.doe@example.com")
                .primaryMobile("9876543210")
                .aadhaar("234567890123")
                .pan("ABCDE1234F")
                .dateOfBirth(LocalDate.of(1995, 5, 15))
                .build();

        updateRequest = new UserUpdateRequest();
        updateRequest.setName("Jane Updated");

        userResponse = new UserResponse();
        userResponse.setId(10L);
        userResponse.setName("Jane Doe");
        userResponse.setEmail("jane.doe@example.com");
    }

    // ==========================================
    // 1. POST /api/v1/users (CREATE)
    // ==========================================

    @Test
    @DisplayName("POST: Should return 201 Created and Location header on successful creation")
    void create_ValidPayload_Returns201Created() throws Exception {
        when(userService.create(any(UserCreateRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/users/10"))
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("Jane Doe"));
    }

    @Test
    @DisplayName("POST: Should return 400 Bad Request when validation fails (@NotBlank, @Email, etc.)")
    void create_InvalidPayload_Returns400BadRequest() throws Exception {
        createRequest.setEmail("invalid-email"); // Violates @Email

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any());
    }

    // ==========================================
    // 2. PATCH /api/v1/users/{id} (UPDATE)
    // ==========================================

    @Test
    @DisplayName("PATCH: Should return 200 OK on successful update")
    void update_ValidPayload_Returns200Ok() throws Exception {
        UserResponse updatedResponse = new UserResponse();
        updatedResponse.setId(10L);
        updatedResponse.setName("Jane Updated");

        when(userService.update(eq(10L), any(UserUpdateRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(patch("/api/v1/users/{id}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Updated"));
    }

    @Test
    @DisplayName("PATCH: Should return 400 Bad Request if ID violates @Positive constraint")
    void update_NegativeId_Returns400BadRequest() throws Exception {
        mockMvc.perform(patch("/api/v1/users/{id}", -5L) // Violates @Positive
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).update(anyLong(), any());
    }

    // ==========================================
    // 3. DELETE /api/v1/users/{id} (SOFT DELETE)
    // ==========================================

    @Test
    @DisplayName("DELETE: Should return 204 No Content on successful soft delete")
    void softDeleteUser_ValidId_Returns204NoContent() throws Exception {
        doNothing().when(userService).softDeleteUser(10L);

        mockMvc.perform(delete("/api/v1/users/{id}", 10L))
                .andExpect(status().isNoContent());

        verify(userService).softDeleteUser(10L);
    }

    // ==========================================
    // 4. PATCH /api/v1/users/{id}/restore (RESTORE)
    // ==========================================

    @Test
    @DisplayName("PATCH: Should return 204 No Content on successful restore")
    void restoreUser_ValidId_Returns204NoContent() throws Exception {
        doNothing().when(userService).restoreUser(10L);

        mockMvc.perform(patch("/api/v1/users/{id}/restore", 10L))
                .andExpect(status().isNoContent());

        verify(userService).restoreUser(10L);
    }

    // ==========================================
    // 5. GET /api/v1/users (GET ALL / SEARCH)
    // ==========================================

    @Test
    @DisplayName("GET ALL: Should parse pagination parameters and return 200 OK")
    void getAll_WithPagination_Returns200Ok() throws Exception {
        PagedResponse<UserResponse> mockPagedResponse = new PagedResponse<>();
        // Note: String search is null in this request
        when(userService.getAll(any(Pageable.class), isNull())).thenReturn(mockPagedResponse);

        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk());

        verify(userService).getAll(any(Pageable.class), isNull());
    }

    @Test
    @DisplayName("GET ALL: Should parse search parameter and return 200 OK")
    void getAll_WithSearch_Returns200Ok() throws Exception {
        PagedResponse<UserResponse> mockPagedResponse = new PagedResponse<>();
        when(userService.getAll(any(Pageable.class), eq("Jane"))).thenReturn(mockPagedResponse);

        mockMvc.perform(get("/api/v1/users")
                        .param("search", "Jane"))
                .andExpect(status().isOk());

        verify(userService).getAll(any(Pageable.class), eq("Jane"));
    }

    // ==========================================
    // 6. GET /api/v1/users/{id} (GET BY ID)
    // ==========================================

    @Test
    @DisplayName("GET BY ID: Should return 200 OK with UserResponse")
    void getById_ValidId_Returns200Ok() throws Exception {
        when(userService.getById(10L)).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/users/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("Jane Doe"));
    }

    @Test
    @DisplayName("GET BY ID: Should return 400 Bad Request if ID violates @Positive constraint")
    void getById_NegativeId_Returns400BadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", 0L)) // Violates @Positive (must be > 0)
                .andExpect(status().isBadRequest());

        verify(userService, never()).getById(anyLong());
    }

    // ==========================================
    // 7. GET /api/v1/users/deleted (GET DELETED)
    // ==========================================

    @Test
    @DisplayName("GET DELETED: Should return 200 OK with List of UserResponses")
    void getDeletedUsers_Returns200Ok() throws Exception {
        when(userService.getDeletedUsers()).thenReturn(List.of(userResponse));

        mockMvc.perform(get("/api/v1/users/deleted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L)); // Checks that it returned an array
    }
}