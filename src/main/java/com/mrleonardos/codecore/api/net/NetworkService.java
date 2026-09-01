package com.mrleonardos.codecore.api.net;

/** Выдаёт модам сетевые каналы. */
public interface NetworkService {

    /**
     * Открыть канал с указанным именем. Повторный вызов с тем же именем вернёт тот же канал.
     *
     * @param name имя канала, не длиннее {@link NetLimits#MAX_CHANNEL_NAME_LENGTH}
     */
    NetChannel open(String name);
}
