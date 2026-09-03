package com.mrleonardos.codecore.internal.net;

import com.mrleonardos.codecore.api.avatar.AvatarConfig;
import com.mrleonardos.codecore.api.net.CodeBuffer;
import com.mrleonardos.codecore.api.net.Codec;
import com.mrleonardos.codecore.api.net.Packet;
import com.mrleonardos.codecore.api.net.PacketContext;
import com.mrleonardos.codecore.internal.CoreBridge;

/**
 * Сервер сообщает клиенту, откуда брать аватары.
 *
 * <p>
 * Уходит при входе игрока. Клиент, который пакет не получил, аватары не показывает: на сервере без
 * CodeCore их и неоткуда взять.
 */
public final class AvatarConfigPacket extends Packet {

    private AvatarConfig config;

    public AvatarConfigPacket() {}

    public AvatarConfigPacket(AvatarConfig config) {
        this.config = config;
    }

    @Override
    public void write(CodeBuffer buffer) {
        Codec.writeString(buffer, config.provider());
        Codec.writeString(buffer, config.url());
        Codec.writeString(buffer, config.jsonPath());
        Codec.writeString(buffer, config.folder());
        buffer.writeInt(config.size());
    }

    @Override
    public void read(CodeBuffer buffer) {
        String provider = Codec.readString(buffer);
        String url = Codec.readString(buffer);
        String jsonPath = Codec.readString(buffer);
        String folder = Codec.readString(buffer);
        int size = buffer.readInt();
        config = AvatarConfig.of(provider, url, jsonPath, folder, size);
    }

    @Override
    public void handle(PacketContext context) {
        CoreBridge.applyAvatars(config);
    }
}
