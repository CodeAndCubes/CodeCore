package com.mrleonardos.codecore.internal.avatar;

import net.minecraft.entity.player.EntityPlayerMP;

import com.mrleonardos.codecore.api.config.ConfigFile;
import com.mrleonardos.codecore.internal.net.AvatarConfigPacket;
import com.mrleonardos.codecore.internal.net.CorePackets;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

/**
 * Рассылает настройку аватаров входящим игрокам.
 *
 * <p>
 * Настройка читается из файла при каждой отправке, а не запоминается при старте: после
 * {@code /codecore reload} следующий вошедший должен получить уже новую.
 */
public final class AvatarConfigSender {

    private final ConfigFile<AvatarSettings> settings;

    public AvatarConfigSender(ConfigFile<AvatarSettings> settings) {
        this.settings = settings;
    }

    @SubscribeEvent
    public void onPlayerJoined(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof EntityPlayerMP)) {
            return;
        }
        CorePackets.channel()
            .toPlayer(
                new AvatarConfigPacket(
                    settings.get()
                        .toConfig()),
                (EntityPlayerMP) event.player);
    }
}
