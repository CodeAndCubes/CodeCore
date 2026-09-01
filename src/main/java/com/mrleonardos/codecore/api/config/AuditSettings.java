package com.mrleonardos.codecore.api.config;

/**
 * Что роль пишет в лог. Собирается ядром из секции {@code [audit]} главного файла и, если она есть, из
 * секции {@code [audit.<роль>]} с теми ключами, которые для этой роли отличаются.
 */
public final class AuditSettings {

    private final boolean logChanges;
    private final boolean logChecks;

    public AuditSettings(boolean logChanges, boolean logChecks) {
        this.logChanges = logChanges;
        this.logChecks = logChecks;
    }

    /** Писать ли каждую правку группы, счёта или дома с автором и причиной. */
    public boolean logChanges() {
        return logChanges;
    }

    /** Писать ли на уровне debug объяснение каждого отказа. Шумно, для разбора полётов. */
    public boolean logChecks() {
        return logChecks;
    }

    @Override
    public String toString() {
        return "audit[logChanges=" + logChanges + ", logChecks=" + logChecks + "]";
    }
}
