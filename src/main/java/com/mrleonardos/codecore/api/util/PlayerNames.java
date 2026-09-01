package com.mrleonardos.codecore.api.util;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import com.mojang.authlib.GameProfile;

/**
 * Ник по идентификатору игрока.
 *
 * <p>
 * Сначала спрашиваются те, кто сейчас в сети, потом кэш профилей сервера, который помнит всех, кто
 * заходил раньше. Это нужно тем правам, что работают по нику: без кэша оффлайн-игрок для них не
 * существует.
 *
 * <p>
 * Имена методов кэша в маппингах 1.7.10 не расшифрованы, поэтому обращение к нему спрятано здесь.
 */
public final class PlayerNames {

    private PlayerNames() {}

    /** Ник игрока или {@code null}, если сервер о нём ничего не знает. */
    public static String byId(UUID player) {
        EntityPlayerMP online = Players.online(player);
        if (online != null) {
            return online.getCommandSenderName();
        }

        MinecraftServer server = MinecraftServer.getServer();
        if (server == null || server.func_152358_ax() == null) {
            return null;
        }
        GameProfile profile = server.func_152358_ax()
            .func_152652_a(player);
        return profile == null ? null : profile.getName();
    }

    /**
     * Идентификатор игрока по нику или {@code null}, если сервер о нём ничего не знает.
     *
     * <p>
     * Обратная сторона {@link #byId(UUID)} и нужна ровно там же: команда получает от администратора
     * ник, а хранилище мода помнит только идентификаторы. Своей карты ников моду заводить не надо,
     * кэш профилей сервера помнит всех, кто заходил.
     */
    public static UUID idByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        EntityPlayerMP online = Players.online(name);
        if (online != null) {
            return online.getUniqueID();
        }

        MinecraftServer server = MinecraftServer.getServer();
        if (server == null || server.func_152358_ax() == null) {
            return null;
        }
        GameProfile profile = server.func_152358_ax()
            .func_152655_a(name);
        return profile == null ? null : profile.getId();
    }
}
