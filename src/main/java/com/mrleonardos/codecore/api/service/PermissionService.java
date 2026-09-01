package com.mrleonardos.codecore.api.service;

import java.util.UUID;

import net.minecraft.command.ICommandSender;

/**
 * Права игроков.
 *
 * <p>
 * Единственный способ спросить «можно ли». Реализацию подставляет ядро или специализированный мод, и
 * тот, кто спрашивает, разницы не видит.
 *
 * <p>
 * Ноды пишутся точками от общего к частному: {@code codechat.channel.global.write}. Шаблоны со звёздочкой
 * разрешены в правилах, но не в запросах: спрашивать нужно про конкретное действие.
 */
public interface PermissionService {

    /** Есть ли у игрока право, в том числе у оффлайн-игрока. */
    boolean has(UUID player, String node);

    /**
     * Есть ли право у отправителя команды.
     *
     * <p>
     * Консоль, RCON и командный блок получают всё: их ограничивают права доступа к серверу, а не мод.
     */
    boolean has(ICommandSender sender, String node);

    /** Группа игрока. */
    String group(UUID player);

    /**
     * Дополнительное значение, привязанное к игроку или его группе: префикс, суффикс, цвет ника.
     *
     * @param fallback что вернуть, если значения нет
     */
    String meta(UUID player, String key, String fallback);
}
