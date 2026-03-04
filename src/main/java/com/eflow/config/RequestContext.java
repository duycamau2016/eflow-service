package com.eflow.config;

/**
 * ThreadLocal holder để truyền thông tin actor (username, role, managerDepartment)
 * qua các tầng service / controller mà không cần sửa signature method.
 * <p>
 * Vòng đời: được set bởi {@link ActorFilter} trước mỗi request,
 * clear sau khi request hoàn thành.
 */
public final class RequestContext {

    private RequestContext() {}

    private static final ThreadLocal<String> actorHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> roleHolder  = new ThreadLocal<>();
    private static final ThreadLocal<String> deptHolder  = new ThreadLocal<>();

    // ── Actor ────────────────────────────────────────────────────

    public static String getActor() {
        String a = actorHolder.get();
        return (a != null && !a.isBlank()) ? a : "system";
    }
    public static void setActor(String actor) { actorHolder.set(actor); }

    // ── Role ─────────────────────────────────────────────────────

    /** Role hiện tại: "ADMIN" | "MANAGER" | null (guest). */
    public static String getRole()          { return roleHolder.get(); }
    public static void   setRole(String r)  { roleHolder.set(r); }

    public static boolean isAdmin()   { return "ADMIN".equals(roleHolder.get()); }
    public static boolean isManager() { return "MANAGER".equals(roleHolder.get()); }

    // ── Manager Department ────────────────────────────────────────

    /**
     * Phòng ban phụ trách của Manager (vd: "Kỹ thuật / Engineering").
     * null nếu không phải Manager.
     */
    public static String getManagerDepartment()           { return deptHolder.get(); }
    public static void   setManagerDepartment(String dept){ deptHolder.set(dept); }

    // ── Lifecycle ─────────────────────────────────────────────────

    public static void clear() {
        actorHolder.remove();
        roleHolder.remove();
        deptHolder.remove();
    }
}
