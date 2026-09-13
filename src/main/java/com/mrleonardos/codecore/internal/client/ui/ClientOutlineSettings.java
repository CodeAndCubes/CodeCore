package com.mrleonardos.codecore.internal.client.ui;

import com.mrleonardos.codecore.api.config.Comment;

/**
 * Содержимое {@code config/code/core/client/core-outline.toml}: то, что решает сам игрок.
 *
 * <p>
 * Дальность стоит на клиенте, а не на сервере, потому что платит за неё видеокарта игрока. Сервер знает,
 * где границы, и не знает, сколько кадров стоит их нарисовать.
 */
public final class ClientOutlineSettings {

    /** Дальность по умолчанию: дальше этого границу всё равно не разглядеть. */
    public static final int DEFAULT_DISTANCE = 192;

    @Comment({ "Дальше этого числа блоков рамки регионов и выделений не рисуются.",
        "Ноль снимает отсечение: на сервере с сотнями видимых границ так лучше не делать." })
    public int maxDistance = DEFAULT_DISTANCE;

    @Comment("Ложь убирает все рамки в мире, что бы ни просили моды.")
    public boolean enabled = true;
}
