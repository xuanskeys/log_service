package com.xuan.logging;

/**
 * Per-request metadata used when an operation is performed before a JWT exists,
 * such as registration and login. The logging aspect always clears it.
 */
public final class OperationLogContext {
    private static final ThreadLocal<Metadata> HOLDER = new ThreadLocal<>();

    private OperationLogContext() {
    }

    public static void setContact(String email, String phone) {
        Metadata current = HOLDER.get();
        HOLDER.set(new Metadata(
                current == null ? null : current.userId(),
                current == null ? null : current.username(),
                current == null ? null : current.tenantId(),
                email,
                phone
        ));
    }

    public static void setIdentity(Long userId, String username, Long tenantId, String email, String phone) {
        Metadata current = HOLDER.get();
        HOLDER.set(new Metadata(
                userId,
                username,
                tenantId,
                email != null ? email : current == null ? null : current.email(),
                phone != null ? phone : current == null ? null : current.phone()
        ));
    }

    static Metadata current() {
        return HOLDER.get();
    }

    static void clear() {
        HOLDER.remove();
    }

    record Metadata(Long userId, String username, Long tenantId, String email, String phone) {
    }
}
