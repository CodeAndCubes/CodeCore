package com.mrleonardos.codecore.internal.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.net.CodeBuffer;
import com.mrleonardos.codecore.api.net.MalformedPacketException;
import com.mrleonardos.codecore.api.net.NetLimits;

import io.netty.buffer.Unpooled;

class ActionBarPacketTest {

    @Test
    @DisplayName("текст и срок доезжают без изменений")
    void goesThereAndBack() {
        CodeBuffer buffer = buffer();
        new ActionBarPacket("Тут нельзя ломать", 4).write(buffer);

        ActionBarPacket received = new ActionBarPacket();
        received.read(buffer);

        assertEquals("Тут нельзя ломать", received.text());
        assertEquals(4, received.seconds());
        assertEquals(0, buffer.readableBytes());
    }

    @Test
    @DisplayName("строка длиннее предела не отправляется")
    void tooLongTextIsRejected() {
        StringBuilder huge = new StringBuilder();
        while (huge.length() <= NetLimits.MAX_STRING_BYTES) {
            huge.append('x');
        }
        ActionBarPacket packet = new ActionBarPacket(huge.toString(), 3);

        assertThrows(MalformedPacketException.class, () -> packet.write(buffer()));
    }

    private static CodeBuffer buffer() {
        return new NettyBuffer(Unpooled.buffer());
    }
}
