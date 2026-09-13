package com.mrleonardos.codecore.internal.net;

import com.mrleonardos.codecore.api.net.CodeBuffer;
import com.mrleonardos.codecore.api.net.Codec;
import com.mrleonardos.codecore.api.net.Packet;
import com.mrleonardos.codecore.api.net.PacketContext;
import com.mrleonardos.codecore.internal.CoreBridge;

/**
 * Сервер просит клиента показать строку над хотбаром.
 *
 * <p>
 * Текст едет готовым, потому что языка клиента сервер не знает. Ключ антиспама остаётся на сервере: на
 * клиенте по нему нечего решать, там всегда показывается последняя пришедшая строка.
 */
public final class ActionBarPacket extends Packet {

    private String text;
    private int seconds;

    public ActionBarPacket() {}

    public ActionBarPacket(String text, int seconds) {
        this.text = text;
        this.seconds = seconds;
    }

    @Override
    public void write(CodeBuffer buffer) {
        Codec.writeString(buffer, text);
        buffer.writeInt(seconds);
    }

    @Override
    public void read(CodeBuffer buffer) {
        text = Codec.readString(buffer);
        seconds = buffer.readInt();
    }

    @Override
    public void handle(PacketContext context) {
        CoreBridge.showActionBar(text, seconds);
    }

    String text() {
        return text;
    }

    int seconds() {
        return seconds;
    }
}
