package com.eflow.config;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Tập trung quản lý thông tin tài khoản người dùng (hardcoded).
 * <p>
 * Được dùng bởi {@link AuthController} (xác thực) và {@link ActorFilter}
 * (populate role/department vào {@link RequestContext} per-request).
 * <p>
 * Thay bằng DB-backed UserRepository khi chuyển lên production với Spring Security.
 */
@Component
public class UserRegistry {

    public record UserInfo(String password, String role, String department) {}

    private static final Map<String, UserInfo> USERS = Map.of(
            "TIENTTT14",  new UserInfo("Matkhau1!", "ADMIN",   null),
            "DUYHN4",     new UserInfo("Matkhau1!", "ADMIN",   null),
            "MGRKYTHUAT", new UserInfo("Matkhau1!", "MANAGER", "Kỹ thuật / Engineering"),
            "MGRNHANSU",  new UserInfo("Matkhau1!", "MANAGER", "Nhân sự / HR"),
            "MGRDUAN",    new UserInfo("Matkhau1!", "MANAGER", "Quản lý Dự án / PMO")
    );

    /** Tra cứu toàn bộ thông tin user (null nếu không tồn tại). */
    public UserInfo get(String username) {
        if (username == null) return null;
        return USERS.get(username.toUpperCase());
    }

    /** Role của user: "ADMIN" | "MANAGER" | null (guest/unknown). */
    public String getRole(String username) {
        UserInfo ui = get(username);
        return ui != null ? ui.role() : null;
    }

    /**
     * Phòng ban phụ trách của Manager.
     * Định dạng: {@code "Kỹ thuật / Engineering"} — phần trước {@code " / "} là tên tiếng Việt.
     * Trả về null cho Admin / guest.
     */
    public String getDepartment(String username) {
        UserInfo ui = get(username);
        return ui != null ? ui.department() : null;
    }

    /** Kiểm tra password (dùng trong AuthController). */
    public boolean verify(String username, String password) {
        UserInfo ui = get(username);
        return ui != null && ui.password().equals(password);
    }

    public Map<String, UserInfo> getAll() {
        return USERS;
    }
}
