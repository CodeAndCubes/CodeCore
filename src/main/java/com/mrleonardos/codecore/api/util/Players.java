package com.mrleonardos.codecore.api.util;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Поиск игроков, которые сейчас на сервере.
 *
 * <p>
 * Тело переехало в {@link com.mrleonardos.codecore.platform.Players}: тип игрока здесь и есть предмет
 * разговора, абстрагировать его незачем, а в api ему не место. Здесь остался переходник, чтобы соседние
 * моды переставили импорт своим проходом, а не в тот же час. Класс уходит вместе со сносом старых подписей.
 */
public final class Players {

    private Players() {}

    /** Игрок по нику без учёта регистра или {@code null}, если он не в сети. */
    public static EntityPlayerMP online(String name) {
        return com.mrleonardos.codecore.platform.Players.online(name);
    }

    /** Игрок по идентификатору или {@code null}, если он не в сети. */
    public static EntityPlayerMP online(UUID id) {
        return com.mrleonardos.codecore.platform.Players.online(id);
    }

    /** Все игроки на сервере. На клиенте без запущенного сервера список пуст. */
    public static List<EntityPlayerMP> allOnline() {
        return com.mrleonardos.codecore.platform.Players.allOnline();
    }

    /** Ники всех игроков на сервере. */
    public static List<String> onlineNames() {
        return com.mrleonardos.codecore.platform.Players.onlineNames();
    }
}
