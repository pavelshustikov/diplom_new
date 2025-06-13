package com.example.cloud_storage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    @NotBlank(message = "Login cannot be empty")
    private String login;
    @NotBlank(message = "Password cannot be empty")
    private String password;
}