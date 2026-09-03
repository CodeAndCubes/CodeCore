package com.mrleonardos.codecore.platform;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mrleonardos.codecore.api.actor.PlayerDirectory;
import com.mrleonardos.codecore.api.actor.PlayerRef;

/**
 * Список игроков сервера ссылками.
 *
 * <p>
 * Ставится ядром при запуске и отвечает на {@code CodeApi.players()}. Перебор игровых сущностей остаётся
 * здесь, наружу уходят только ссылки.
 */
public final class ServerPlayers implements PlayerDirectory {

    @Override
    public Optional<PlayerRef> byName(String name) {
        return Optional.ofNullable(PlayerRefs.of(Players.online(name)));
    }

    @Override
    public Optional<PlayerRef> byId(UUID id) {
        return Optional.ofNullable(PlayerRefs.of(Players.online(id)));
    }

    @Override
    public List<PlayerRef> online() {
        return PlayerRefs.allOnline();
    }

    @Override
    public List<String> onlineNames() {
        return Players.onlineNames();
    }
}
