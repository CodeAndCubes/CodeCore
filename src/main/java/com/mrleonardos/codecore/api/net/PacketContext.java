package com.mrleonardos.codecore.api.net;

import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Обстановка, в которой обрабатывается пакет.
 *
 * <p>
 * Обработчик уже выполняется в главном потоке своей стороны, так что обращаться к миру и игрокам отсюда
 * безопасно.
 */
public interface PacketContext {

    /**
     * Игрок, приславший пакет.
     *
     * <p>
     * Единственный источник правды об отправителе: имя или идентификатор внутри самого пакета доверия не
     * заслуживают. На клиенте {@code null}.
     */
    EntityPlayerMP sender();

    /** Обрабатывается ли пакет на сервере. */
    boolean onServer();
}
