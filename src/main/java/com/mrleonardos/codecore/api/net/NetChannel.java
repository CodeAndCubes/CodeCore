package com.mrleonardos.codecore.api.net;

import com.mrleonardos.codecore.api.actor.PlayerRef;

/**
 * Сетевой канал одного мода.
 *
 * <p>
 * Пакеты регистрируются на обеих сторонах в одном и том же порядке: он задаёт их номера в протоколе.
 * Регистрировать канал в предварительной инициализации, до первых отправок.
 */
public interface NetChannel {

    /**
     * Объявить тип пакета и направление, в котором он ходит.
     *
     * @throws IllegalStateException если порядок регистрации разошёлся с лимитом протокола
     */
    <T extends Packet> void register(Class<T> type, PacketSide side);

    /**
     * Отправить одному игроку.
     *
     * <p>
     * Ссылка на игрока, которого уже нет на сервере, отбрасывается молча: адресат мог выйти между сбором
     * списка и отправкой.
     */
    void toPlayer(Packet packet, PlayerRef player);

    /** Отправить перечисленным игрокам; ушедшие с сервера пропускаются. */
    void toPlayers(Packet packet, Iterable<PlayerRef> players);

    /** Отправить всем на сервере. */
    void toAll(Packet packet);

    /** Отправить с клиента на сервер. */
    void toServer(Packet packet);
}
