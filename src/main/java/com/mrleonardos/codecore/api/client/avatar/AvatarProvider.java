package com.mrleonardos.codecore.api.client.avatar;

import java.util.UUID;

import com.mrleonardos.codecore.api.client.image.ImageSource;

/**
 * Откуда брать аватар игрока.
 *
 * <p>
 * Провайдер только называет источник и ничего не загружает: качает, обрезает и кэширует картинку сервис
 * изображений, и делает это одинаково, откуда бы адрес ни взялся.
 */
public interface AvatarProvider {

    /**
     * Источник аватара игрока или {@code null}, если этот провайдер ничего о нём не знает.
     *
     * @param playerId   идентификатор игрока
     * @param playerName ник, по нему работают многие сервисы аватаров
     */
    ImageSource sourceFor(UUID playerId, String playerName);
}
