package com.mrleonardos.codecore.internal.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.adapter.PermissionCapabilities;
import com.mrleonardos.codecore.api.adapter.RoleCapability;
import com.mrleonardos.codecore.api.command.FakeSender;
import com.mrleonardos.codecore.api.service.PermissionService;

/**
 * Один набор проверок на роль прав.
 *
 * <p>
 * Гоняется по встроенной реализации и по каждому мосту к чужому моду: мост обязан отвечать так же, а не
 * бросать из середины метода. Проверка умения, которого владелец не назвал в своих capabilities,
 * пропускается с отметкой в отчёте, а не считается пройденной.
 */
public abstract class PermissionServiceContract {

    private static final String NODE = "codechat.channel.staff.read";
    private static final String OTHER_NODE = "codechat.admin";
    private static final String PREFIX = "prefix";

    /** Реализация, которую проверяем. */
    protected abstract PermissionService service();

    /** Что эта реализация про себя объявила. */
    protected abstract Set<RoleCapability> capabilities();

    /** Выдать игроку право средствами своего хранилища. */
    protected abstract void grant(UUID player, String node);

    /** Назначить игроку группу средствами своего хранилища. */
    protected abstract void assign(UUID player, String group);

    /** Проставить игроку значение меты средствами своего хранилища. */
    protected abstract void meta(UUID player, String key, String value);

    @Test
    @DisplayName("выданное право видно, невыданное нет")
    void grantedNodeIsVisible() {
        assumeSupported(PermissionCapabilities.HAS);
        UUID player = UUID.randomUUID();
        grant(player, NODE);

        assertTrue(service().has(player, NODE));
        assertFalse(service().has(player, OTHER_NODE));
    }

    @Test
    @DisplayName("игрок, о котором ничего не известно, прав не получает")
    void unknownPlayerHasNothing() {
        assumeSupported(PermissionCapabilities.HAS);

        assertFalse(service().has(UUID.randomUUID(), NODE));
    }

    @Test
    @DisplayName("группа игрока называется")
    void groupIsReported() {
        assumeSupported(PermissionCapabilities.GROUP);
        UUID player = UUID.randomUUID();
        assign(player, "moderator");

        assertEquals("moderator", service().group(player));
    }

    @Test
    @DisplayName("консоль, RCON и командный блок получают право без обращения к файлу")
    void nonPlayersAreAllowed() {
        assumeSupported(PermissionCapabilities.HAS);

        assertTrue(service().has(FakeSender.console(), NODE));
        assertTrue(service().has(FakeSender.rcon(), NODE));
        assertTrue(service().has(FakeSender.commandBlock(0, 1, 2, 3), NODE));
    }

    @Test
    @DisplayName("право игрока проверяется по ссылке из отправителя")
    void playerIsCheckedByItsReference() {
        assumeSupported(PermissionCapabilities.HAS);
        UUID player = UUID.randomUUID();
        grant(player, NODE);

        assertTrue(service().has(FakeSender.player(player, "Steve"), NODE));
        assertFalse(service().has(FakeSender.player(player, "Steve"), OTHER_NODE));
        assertFalse(service().has(FakeSender.player(UUID.randomUUID(), "Alex"), NODE));
    }

    @Test
    @DisplayName("мета берётся из хранилища, а на её месте пусто отдаётся запасное")
    void metaIsReported() {
        assumeSupported(PermissionCapabilities.META);
        UUID player = UUID.randomUUID();
        meta(player, PREFIX, "&c[M]");

        assertEquals("&c[M]", service().meta(player, PREFIX, ""));
        assertEquals("нет", service().meta(player, "suffix", "нет"));
    }

    private void assumeSupported(RoleCapability capability) {
        assumeTrue(
            capabilities().contains(capability),
            "умение " + capability + " эта реализация не объявила, проверка пропущена");
    }
}
