package com.mrleonardos.codecore.api.net;

/**
 * Направление пакета: кто его получает.
 *
 * <p>
 * Сопоставление со стороной сетевого слоя игры делает диспетчер ядра: наружу из api тип стороны не
 * торчит.
 */
public enum PacketSide {

    /** От сервера клиенту. */
    CLIENT_BOUND,

    /** От клиента серверу. */
    SERVER_BOUND
}
