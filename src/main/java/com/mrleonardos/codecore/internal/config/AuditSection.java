package com.mrleonardos.codecore.internal.config;

import com.mrleonardos.codecore.api.config.Comment;

/**
 * Секция {@code [audit]}: что моды пишут в лог.
 *
 * <p>
 * Описание секции начинается с примера перекрытия по роли: в файле эти строки стоят ровно между
 * последним ключом {@code [storage]} и заголовком {@code [audit]}, а комментарий цепляется к ключу,
 * который идёт следом.
 */
@Comment({ "Отличие одной роли пишется соседней секцией, только теми ключами, которые отличаются:", "[storage.economy]",
    "provider = \"sql\"", "autosaveSeconds = 120", "", "Записи в лог о том, что моды меняют и что проверяют." })
public final class AuditSection {

    @Comment("Каждая правка группы, счёта, дома с автором и причиной.")
    public boolean logChanges = true;

    @Comment("Объяснение каждого отказа на уровне debug. Шумно, для разбора полётов.")
    public boolean logChecks;
}
