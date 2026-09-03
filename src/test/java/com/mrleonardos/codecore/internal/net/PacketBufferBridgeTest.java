package com.mrleonardos.codecore.internal.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.net.CodeBuffer;
import com.mrleonardos.codecore.api.net.Codec;
import com.mrleonardos.codecore.api.net.Packet;
import com.mrleonardos.codecore.api.net.PacketContext;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Пакет, написанный на CodeBuffer, ходит по проводу, где ещё живёт ByteBuf.
 *
 * <p>
 * Пока у {@code Packet} живы обе пары методов, наследник реализует ровно одну, а ядро сводит их само.
 * Это и позволяет соседям переводить свои пакеты по одному, не дожидаясь сноса старых подписей.
 */
class PacketBufferBridgeTest {

    @Test
    @DisplayName("пакет на CodeBuffer читается через ByteBuf сетевого слоя")
    void freshPacketTravelsOverTheOldWire() {
        Fresh written = new Fresh("привет", 7);
        ByteBuf wire = Unpooled.buffer();

        written.write(wire);

        Fresh read = new Fresh();
        read.read(wire);

        assertEquals("привет", read.text);
        assertEquals(7, read.number);
        assertEquals(0, wire.readableBytes(), "прочитано ровно столько, сколько записано");
    }

    @Test
    @DisplayName("пакет на ByteBuf по-прежнему работает как раньше")
    void legacyPacketStillWorks() {
        Legacy written = new Legacy(42);
        ByteBuf wire = Unpooled.buffer();

        written.write(wire);

        Legacy read = new Legacy();
        read.read(wire);

        assertEquals(42, read.number);
    }

    @Test
    @DisplayName("пакет, не написавший ни одной пары, падает громко")
    void packetWithoutBodiesFailsLoudly() {
        assertThrows(UnsupportedOperationException.class, () -> new Empty().write(Unpooled.buffer()));
        assertThrows(UnsupportedOperationException.class, () -> new Empty().read(Unpooled.buffer()));
    }

    private static final class Fresh extends Packet {

        private String text;
        private int number;

        Fresh() {}

        Fresh(String text, int number) {
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

    private static final class Legacy extends Packet {

        private int number;

        Legacy() {}

        Legacy(int number) {
            this.number = number;
        }

        @Override
        public void write(ByteBuf buffer) {
            buffer.writeInt(number);
        }

        @Override
        public void read(ByteBuf buffer) {
            number = buffer.readInt();
        }

        @Override
        public void handle(PacketContext context) {}
    }

    private static final class Empty extends Packet {

        @Override
        public void handle(PacketContext context) {}
    }
}
