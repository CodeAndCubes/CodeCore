package com.mrleonardos.codecore.internal.permission;

import java.util.UUID;

/**
 * Ответ на вопрос «оператор ли этот игрок».
 *
 * <p>
 * Отдельный интерфейс, потому что настоящая проверка лезет в запущенный сервер, а правила прав хочется
 * проверять и без него.
 */
@FunctionalInterface
public interface OperatorCheck {

    boolean isOperator(UUID player);
}
