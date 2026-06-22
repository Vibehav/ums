package com.example.ums.controller;

import com.example.ums.dto.UserCreateRequest;
import com.example.ums.dto.UserResponse;
import com.example.ums.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @MockitoBean
    private UserService userService;

    private UserCreateRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = UserCreateRequest.builder()
                .name("Jane Doe")
                .email("jane.doe@example.com")
                .primaryMobile("9876543210")
                .aadhaar("264530973399") // Replace with valid regex match locally
                .pan("ABCDE1234F")
                .dateOfBirth(LocalDate.of(1995, 5, 15))
                .build();
    }

    @Test
    void create_ValidPayload_Returns201Created() throws Exception {
        UserResponse responseBody = new UserResponse();
        responseBody.setId(10L);
        responseBody.setName("Jane Doe");

        when(userService.create(any(UserCreateRequest.class))).thenReturn(responseBody);

        mockMvc.perform(post("/api/v1/users") // Ensure this matches your @RequestMapping
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/users/10"))
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("Jane Doe"));
    }

    @Test
    void create_InvalidEmail_Returns400BadRequest() throws Exception {
        validRequest.setEmail("invalid-email-format");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }
}