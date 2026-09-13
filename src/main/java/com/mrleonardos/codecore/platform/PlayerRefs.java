package com.mrleonardos.codecore.platform;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrleonardos.codecore.api.actor.PlayerRef;

/**
 * Переход между ссылкой на игрока и сущностью игрока.
 *
 * <p>
 * Через границу api ходит {@link PlayerRef}, а телепортируют, выдают предметы и шлют пакеты уже сущности.
 * Место, где одно превращается в другое, ровно одно, и оно здесь.
 *
 * <p>
 * Обратный переход не обещает результата: между сбором списка получателей и отправкой игрок мог выйти,
 * тогда ответ {@code null}.
 */
public final class PlayerRefs {

    private PlayerRefs() {}

    /**
     * Ссылка на игрока или {@code null}, если сущности нет.
     *
     * <p>
     * Принимает любого игрока, а не только серверного, нарочно: события Forge отдают
     * {@link EntityPlayer}, и без этого каждый мод линейки завёл бы свою проверку типа перед вызовом.
     * Ссылка это пара «идентификатор и ник», и у игрока на клиенте она такая же.
     */
    public static PlayerRef of(EntityPlayer player) {
        return player == null ? null : PlayerRef.of(player.getUniqueID(), player.getCommandSenderName());
    }

    /** Игрок по ссылке или {@code null}, если его уже нет на сервере. */
    public static EntityPlayerMP online(PlayerRef player) {
        return player == null ? null : Players.online(player.id());
    }

    /** Ссылки на всех, кто сейчас на сервере. */
    public static List<PlayerRef> allOnline() {
        List<EntityPlayerMP> players = Players.allOnline();
        List<PlayerRef> refs = new ArrayList<>(players.size());
        for (EntityPlayerMP player : players) {
            refs.add(of(player));
        }
        return refs;
    }

    /** Игроки по ссылкам; ушедшие с сервера в список не попадают. */
    public static List<EntityPlayerMP> online(Iterable<PlayerRef> players) {
        List<EntityPlayerMP> found = new ArrayList<>();
        for (PlayerRef ref : players) {
            EntityPlayerMP player = online(ref);
            if (player != null) {
                found.add(player);
            }
        }
        return found;
    }
}
