package com.eflow.config;

/**
 * ThreadLocal holder để truyền username của người thực hiện
 * qua các tầng service mà không cần sửa signature method.
 *
 * Vòng đời: được set bởi {@link ActorFilter} trước mỗi request,
 * clear sau khi request hoàn thành.
 */
public final class RequestContext {

    private RequestContext() {}

    private static final ThreadLocal<String> actorHolder = new ThreadLocal<>();

    public static String getActor() {
        String a = actorHolder.get();
        return (a != null && !a.isBlank()) ? a : "system";
    }

    public static void setActor(String actor) {
        actorHolder.set(actor);
    }

    public static void clear() {
        actorHolder.remove();
    }
}
