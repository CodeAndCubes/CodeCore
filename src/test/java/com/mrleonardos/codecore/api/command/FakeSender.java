package com.mrleonardos.codecore.api.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mrleonardos.codecore.api.actor.PlayerRef;

/**
 * Отправитель команды без единого класса игры: тем и ценен.
 *
 * <p>
 * Общий для всех тестов, которым нужен отправитель: подставлять по своему на каждый набор проверок значит
 * заводить четыре расходящиеся копии одного и того же.
 */
public final class FakeSender implements CommandSender {

    /** Что отправитель услышал: ключи перевода в порядке отправки. */
    public final List<String> said = new ArrayList<>();

    private final SenderKind kind;
    private final PlayerRef player;
    private final String name;
    private final SenderPosition position;

    private FakeSender(SenderKind kind, PlayerRef player, String name, SenderPosition position) {
        this.kind = kind;
        this.player = player;
        this.name = name;
        this.position = position;
    }

    /** Игрок с этим идентификатором и ником, стоящий в начале координат. */
    public static FakeSender player(UUID id, String name) {
        return new FakeSender(SenderKind.PLAYER, PlayerRef.of(id, name), name, new SenderPosition(0, 0, 64, 0));
    }

    /** Консоль сервера. */
    public static FakeSender console() {
        return new FakeSender(SenderKind.CONSOLE, null, "Server", null);
    }

    /** Удалённая консоль. */
    public static FakeSender rcon() {
        return new FakeSender(SenderKind.RCON, null, "Rcon", null);
    }

    /** Командный блок в точке. */
    public static FakeSender commandBlock(int dimension, int x, int y, int z) {
        return new FakeSender(SenderKind.COMMAND_BLOCK, null, "@", new SenderPosition(dimension, x, y, z));
    }

    @Override
    public SenderKind kind() {
        return kind;
    }

    @Override
    public Optional<PlayerRef> player() {
        return Optional.ofNullable(player);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Optional<SenderPosition> position() {
        return Optional.ofNullable(position);
    }

    @Override
    public void reply(String translationKey, Object... arguments) {
        said.add(translationKey);
    }

    @Override
    public void replyError(String translationKey, Object... arguments) {
        said.add("error " + translationKey);
    }
}
