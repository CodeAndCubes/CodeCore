package com.mrleonardos.codecore.api.net;

import net.minecraft.entity.player.EntityPlayerMP;

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

    /** Отправить одному игроку. */
    void toPlayer(Packet packet, EntityPlayerMP player);

    /** Отправить перечисленным игрокам. */
    void toPlayers(Packet packet, Iterable<EntityPlayerMP> players);

    /** Отправить всем на сервере. */
    void toAll(Packet packet);

    /** Отправить с клиента на сервер. */
    void toServer(Packet packet);
}
