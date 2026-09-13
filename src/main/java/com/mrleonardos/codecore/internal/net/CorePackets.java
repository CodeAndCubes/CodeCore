package com.mrleonardos.codecore.internal.net;

import com.mrleonardos.codecore.CoreConstants;
import com.mrleonardos.codecore.api.CodeApi;
import com.mrleonardos.codecore.api.net.NetChannel;
import com.mrleonardos.codecore.api.net.PacketSide;

/**
 * Собственный канал ядра.
 *
 * <p>
 * Порядок регистрации задаёт номера пакетов в протоколе, поэтому новые типы дописываются только в конец.
 */
public final class CorePackets {

    private static NetChannel channel;

    private CorePackets() {}

    public static void register() {
        channel = CodeApi.network()
            .open(CoreConstants.MODID);
        channel.register(AvatarConfigPacket.class, PacketSide.CLIENT_BOUND);
        channel.register(ActionBarPacket.class, PacketSide.CLIENT_BOUND);
    }

    /** Канал ядра. Доступен после {@link #register()}. */
    public static NetChannel channel() {
        if (channel == null) {
            throw new IllegalStateException("Core packets are not registered yet");
        }
        return channel;
    }
}
