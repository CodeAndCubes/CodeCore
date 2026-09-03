package com.mrleonardos.codecore.internal.net;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.net.CodeBuffer;
import com.mrleonardos.codecore.api.net.Codec;
import com.mrleonardos.codecore.api.net.Packet;
import com.mrleonardos.codecore.api.net.PacketContext;

import io.netty.buffer.Unpooled;

/**
 * Пакет ходит по проводу, ничего не зная о том, чем подставлены байты.
 *
 * <p>
 * Ровно это делает {@code PacketCodec}: заворачивает буфер сетевой библиотеки в {@link CodeBuffer} и
 * отдаёт его пакету. Дальше по коду тип буфера не виден никому.
 */
class PacketBufferBridgeTest {

    @Test
    @DisplayName("записанное пакетом читается обратно через буфер ядра")
    void packetTravelsThroughTheCoreBuffer() {
        Sample written = new Sample("привет", 7);
        CodeBuffer wire = new NettyBuffer(Unpooled.buffer());

        written.write(wire);

        Sample read = new Sample();
        read.read(wire);

        assertEquals("привет", read.text);
        assertEquals(7, read.number);
        assertEquals(0, wire.readableBytes(), "прочитано ровно столько, сколько записано");
    }

    private static final class Sample extends Packet {

        private String text;
        private int number;

        Sample() {}

        Sample(String text, int number) {
            this.text = text;
            this.number = number;
        }

        @Override
        public void write(CodeBuffer buffer) {
            Codec.writeString(buffer, text);
            buffer.writeInt(number);
        }

        @Override
        public void read(CodeBuffer buffer) {
            text = Codec.readString(buffer);
            number = buffer.readInt();
        }

        @Override
        public void handle(PacketContext context) {}
    }
}
