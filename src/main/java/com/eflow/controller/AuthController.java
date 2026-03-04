package com.eflow.controller;

import com.eflow.dto.LoginRequestDTO;
import com.eflow.dto.LoginResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Xác thực đơn giản – không dùng Spring Security.
 * Chỉ dùng cho demo/prototype, credentials hardcoded.
 *
 * Accounts:
 *   TIENTTT14  / Matkhau1!  → ADMIN
 *   DUYHN4     / Matkhau1!  → ADMIN
 *   MGRKYTHUAT / Matkhau1!  → MANAGER (Kỹ thuật / Engineering)
 *   MGRNHANSU  / Matkhau1!  → MANAGER (Nhân sự / HR)
 *   MGRDUAN    / Matkhau1!  → MANAGER (Quản lý Dự án / PMO)
 */
@Tag(name = "Xác thực", description = "Login / logout")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private record UserInfo(String password, String role, String department) {}

    private static final Map<String, UserInfo> USERS = Map.of(
            "TIENTTT14",  new UserInfo("Matkhau1!", "ADMIN",   null),
            "DUYHN4",     new UserInfo("Matkhau1!", "ADMIN",   null),
            "MGRKYTHUAT", new UserInfo("Matkhau1!", "MANAGER", "Kỹ thuật / Engineering"),
            "MGRNHANSU",  new UserInfo("Matkhau1!", "MANAGER", "Nhân sự / HR"),
            "MGRDUAN",    new UserInfo("Matkhau1!", "MANAGER", "Quản lý Dự án / PMO")
    );

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO req) {
        if (req.getUsername() == null || req.getPassword() == null) {
            return ResponseEntity.badRequest()
                    .body(new LoginResponseDTO(false, null, null, null, "Vui lòng nhập tên đăng nhập và mật khẩu"));
        }

        String key  = req.getUsername().toUpperCase();
        UserInfo ui = USERS.get(key);

        if (ui != null && ui.password().equals(req.getPassword())) {
            return ResponseEntity.ok(
                    new LoginResponseDTO(true, key, ui.role(), ui.department(), "Đăng nhập thành công")
            );
        }

        return ResponseEntity.status(401)
                .body(new LoginResponseDTO(false, null, null, null, "Tên đăng nhập hoặc mật khẩu không đúng"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }
}

