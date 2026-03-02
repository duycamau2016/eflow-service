package com.eflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private boolean success;
    private String username;
    private String role;
    private String message;
}
