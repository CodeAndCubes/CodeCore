package com.mrleonardos.codecore.api.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/**
 * Поиск игроков, которые сейчас на сервере.
 *
 * <p>
 * Единственное место, где перебирается список игроков: в 1.7.10 он сырой, и приведение типов лучше делать
 * один раз здесь, чем в каждом моде.
 */
public final class Players {

    private Players() {}

    /** Игрок по нику без учёта регистра или {@code null}, если он не в сети. */
    public static EntityPlayerMP online(String name) {
        for (EntityPlayerMP player : allOnline()) {
            if (player.getCommandSenderName()
                .equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }

    /** Игрок по идентификатору или {@code null}, если он не в сети. */
    public static EntityPlayerMP online(UUID id) {
        for (EntityPlayerMP player : allOnline()) {
            if (player.getUniqueID()
                .equals(id)) {
                return player;
            }
        }
        return null;
    }

    /** Все игроки на сервере. На клиенте без запущенного сервера список пуст. */
    public static List<EntityPlayerMP> allOnline() {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null || server.getConfigurationManager() == null) {
            return Collections.emptyList();
        }
        List<?> raw = server.getConfigurationManager().playerEntityList;
        List<EntityPlayerMP> players = new ArrayList<>(raw.size());
        for (Object candidate : raw) {
            players.add((EntityPlayerMP) candidate);
        }
        return players;
    }

    /** Ники всех игроков на сервере. */
    public static List<String> onlineNames() {
        List<EntityPlayerMP> players = allOnline();
        List<String> names = new ArrayList<>(players.size());
        for (EntityPlayerMP player : players) {
            names.add(player.getCommandSenderName());
        }
        return names;
    }
}
