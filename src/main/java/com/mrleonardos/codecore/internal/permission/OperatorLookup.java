package com.mrleonardos.codecore.internal.permission;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/**
 * Отвечает, считается ли игрок оператором сервера.
 *
 * <p>
 * Обращение к списку операторов вынесено сюда целиком: имя метода в маппингах 1.7.10 не расшифровано, и
 * держать его в одном месте дешевле, чем разбирать по всему коду.
 *
 * <p>
 * Для оффлайн-игрока ответ отрицательный: список операторов проверяется по профилю, а профиль есть только
 * у того, кто сейчас на сервере. Права из файла при этом работают для всех.
 */
public final class OperatorLookup {

    private OperatorLookup() {}

    public static boolean isOperator(UUID playerId) {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) {
            return false;
        }
        EntityPlayerMP player = findOnline(server, playerId);
        if (player == null) {
            return false;
        }
        return server.getConfigurationManager()
            .func_152596_g(player.getGameProfile());
    }

    private static EntityPlayerMP findOnline(MinecraftServer server, UUID playerId) {
        List<?> online = server.getConfigurationManager().playerEntityList;
        for (Object candidate : online) {
            EntityPlayerMP player = (EntityPlayerMP) candidate;
            if (player.getUniqueID()
                .equals(playerId)) {
                return player;
            }
        }
        return null;
    }
}
