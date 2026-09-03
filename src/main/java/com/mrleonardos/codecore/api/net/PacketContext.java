package com.mrleonardos.codecore.api.net;

import java.util.Optional;

import com.mrleonardos.codecore.api.actor.PlayerRef;

/**
 * Обстановка, в которой обрабатывается пакет.
 *
 * <p>
 * Обработчик уже выполняется в главном потоке своей стороны, так что обращаться к миру и игрокам отсюда
 * безопасно.
 */
public interface PacketContext {

    /**
     * Ссылка на игрока, приславшего пакет; на клиенте пустая.
     *
     * <p>
     * Единственный источник правды об отправителе: имя или идентификатор внутри самого пакета доверия не
     * заслуживают.
     */
    Optional<PlayerRef> player();

    /** Обрабатывается ли пакет на сервере. */
    boolean onServer();
}
