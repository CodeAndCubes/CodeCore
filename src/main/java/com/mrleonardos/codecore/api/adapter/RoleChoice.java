package com.mrleonardos.codecore.api.adapter;

/** Откуда взялся владелец роли. Уходит в стартовую сводку и в ответ команды. */
public enum RoleChoice {

    /** Назван именем в секции {@code [owners]}. */
    NAMED,

    /** Выбран правилом {@code auto}. */
    AUTO,

    /** Имени из {@code [owners]} нет среди заявок роли, поэтому роль ушла по правилу {@code auto}. */
    UNKNOWN_NAME,

    /** В {@code [owners]} стоит {@code off}: роль не занята никем. */
    OFF
}
