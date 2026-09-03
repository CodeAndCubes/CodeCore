package com.mrleonardos.codecore.internal.config;

import com.mrleonardos.codecore.api.config.Comment;

/** Секция {@code [audit]}: что моды пишут в лог. */
@Comment("Записи в лог о том, что моды меняют и что проверяют.")
public final class AuditSection {

    @Comment("Каждая правка группы, счёта, дома с автором и причиной.")
    public boolean logChanges = true;

    @Comment("Объяснение каждого отказа на уровне debug. Шумно, для разбора полётов.")
    public boolean logChecks;
}
