package com.eflow.controller;

import com.eflow.dto.LoginRequestDTO;
import com.eflow.dto.LoginResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Xác thực đơn giản – không dùng Spring Security.
 * Chỉ dùng cho demo/prototype, credentials hardcoded.
 *
 * Accounts:
 *   TIENTTT14 / Matkhau1!
 *   DUYHN4 / Matkhau1!
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Map<String, String> CREDENTIALS = Map.of(
            "TIENTTT14", "Matkhau1!",
            "DUYHN4", "Matkhau1!"
    );

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO req) {
        if (req.getUsername() == null || req.getPassword() == null) {
            return ResponseEntity.badRequest()
                    .body(new LoginResponseDTO(false, null, null, "Vui lòng nhập tên đăng nhập và mật khẩu"));
        }

        String expected = CREDENTIALS.get(req.getUsername().toUpperCase());
        if (expected != null && expected.equals(req.getPassword())) {
            return ResponseEntity.ok(
                    new LoginResponseDTO(true, req.getUsername().toUpperCase(), "ADMIN", "Đăng nhập thành công")
            );
        }

        return ResponseEntity.status(401)
                .body(new LoginResponseDTO(false, null, null, "Tên đăng nhập hoặc mật khẩu không đúng"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Stateless – client tự xoá session
        return ResponseEntity.ok().build();
    }
}
