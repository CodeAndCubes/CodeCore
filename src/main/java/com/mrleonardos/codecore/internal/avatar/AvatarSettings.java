package com.mrleonardos.codecore.internal.avatar;

import com.mrleonardos.codecore.api.avatar.AvatarConfig;

/**
 * Содержимое {@code config/codecore/avatars.json}, серверного файла.
 *
 * <p>
 * Лежит на сервере и рассылается клиентам при входе: адрес аватарок задаёт проект, а не игрок.
 * У себя игрок может только выключить показ целиком.
 */
public final class AvatarSettings {

    /** Источник: {@code none}, {@code urlTemplate}, {@code jsonEndpoint}, {@code localFolder}. */
    public String provider = AvatarConfig.PROVIDER_NONE;

    /**
     * Шаблон адреса для {@code urlTemplate} и {@code jsonEndpoint}.
     *
     * <p>
     * Понимает подстановки {@code {name}}, {@code {uuid}} и {@code {uuid_nodash}}.
     */
    public String url = "";

    /** Путь к адресу картинки в ответе {@code jsonEndpoint}, например {@code data.avatar}. */
    public String jsonPath = "avatar";

    /** Папка у игрока для {@code localFolder}; пустая означает {@code config/codecore/avatars}. */
    public String folder = "";

    /** Сторона картинки в пикселях после обработки. */
    public int size = 64;

    /** Настройка в том виде, в каком она уходит клиенту. */
    public AvatarConfig toConfig() {
        return AvatarConfig.of(provider, url, jsonPath, folder, size);
    }
}
