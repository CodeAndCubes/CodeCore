package com.mrleonardos.codecore.api.net;

import cpw.mods.fml.relauncher.Side;

/** Направление пакета: кто его получает. */
public enum PacketSide {

    /** От сервера клиенту. */
    CLIENT_BOUND(Side.CLIENT),

    /** От клиента серверу. */
    SERVER_BOUND(Side.SERVER);

    private final Side receiving;

    PacketSide(Side receiving) {
        this.receiving = receiving;
    }

    /** Сторона, на которой пакет допустимо обрабатывать. */
    public Side receivingSide() {
        return receiving;
    }
}
