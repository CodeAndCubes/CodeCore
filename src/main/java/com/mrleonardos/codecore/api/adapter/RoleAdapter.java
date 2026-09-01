package com.mrleonardos.codecore.api.adapter;

import java.util.Set;

/**
 * Заявка на роль: кто готов её держать и что он умеет.
 *
 * <p>
 * Подаётся в фазе init через {@link AdapterRegistry#offer(RoleAdapter)}. Решение принимается в конце
 * постинициализации, и {@link #create()} зовётся ровно у победителя.
 *
 * <p>
 * Отражение к чужому моду живёт внутри заявки: статической ссылки на его классы быть не должно, иначе
 * отсутствующий класс уронит загрузку всего мода.
 */
public interface RoleAdapter {

    /** Роль, на которую подана заявка: {@code permissions}, {@code economy}, {@code essentials}. */
    String role();

    /** Имя владельца, которое админ пишет в {@code [owners]}: {@code codeperms}, {@code luckperms}. */
    String name();

    /** Кто подал заявку: от этого зависит выбор по {@code auto} и вес регистрации. */
    RoleOwnerKind kind();

    /** Готова ли заявка работать прямо сейчас: стоит ли чужой мод, отвечает ли его api. */
    boolean available();

    /** Умения роли, которые эта заявка закрывает. Остальные не будут работать вовсе. */
    Set<RoleCapability> capabilities();

    /** Собрать реализации. Зовётся один раз и только у победителя роли. */
    RoleServices create();
}
