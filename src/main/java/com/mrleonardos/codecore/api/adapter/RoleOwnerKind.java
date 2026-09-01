package com.mrleonardos.codecore.api.adapter;

/**
 * Кто подал заявку на роль. Определяет, кого выбирает правило {@code auto} и каким весом победитель
 * встаёт в реестр сервисов.
 */
public enum RoleOwnerKind {

    /** Встроенная реализация ядра. Остаётся за ролью, пока никто другой её не занял. */
    BUILTIN,

    /** Наш мод линейки: CodePerms, CodeEconomy, CodeEssentials. */
    MOD,

    /** Мост к чужому моду. Сам по себе, без имени в конфиге, не подключается. */
    ADAPTER
}
