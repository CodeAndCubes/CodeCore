package com.mrleonardos.codecore.api.actor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Кто сейчас на сервере.
 *
 * <p>
 * Берётся из {@code CodeApi.players()}. Тому, кто взял только api-джар, это единственный способ получить
 * ссылку на игрока по нику: перебор списка игроков делается типами игры, и живёт он в слое платформы
 * ядра.
 *
 * <p>
 * Ответ верен на момент вопроса и не более того: между ответом и следующей строкой игрок мог выйти.
 */
public interface PlayerDirectory {

    /** Игрок по нику без учёта регистра. */
    Optional<PlayerRef> byName(String name);

    /** Игрок по идентификатору. */
    Optional<PlayerRef> byId(UUID id);

    /** Все, кто сейчас на сервере. На клиенте без запущенного сервера список пуст. */
    List<PlayerRef> online();

    /** Ники всех, кто сейчас на сервере. */
    List<String> onlineNames();
}
