package com.eflow.controller;

import com.eflow.config.UserRegistry;
import com.eflow.dto.LoginRequestDTO;
import com.eflow.dto.LoginResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Xác thực đơn giản – không dùng Spring Security.
 * Chỉ dùng cho demo/prototype. Accounts được quản lý bởi {@link UserRegistry}.
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
@RequiredArgsConstructor
public class AuthController {

    private final UserRegistry userRegistry;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO req) {
        if (req.getUsername() == null || req.getPassword() == null) {
            return ResponseEntity.badRequest()
                    .body(new LoginResponseDTO(false, null, null, null, "Vui lòng nhập tên đăng nhập và mật khẩu"));
        }

        String key = req.getUsername().toUpperCase();
        UserRegistry.UserInfo ui = userRegistry.get(key);

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
