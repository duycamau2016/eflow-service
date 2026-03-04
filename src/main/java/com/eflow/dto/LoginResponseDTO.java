package com.eflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private boolean success;
    private String  username;
    private String  role;
    /** Phòng ban quản lý (chỉ có với role MANAGER) */
    private String  department;
    private String  message;
}
