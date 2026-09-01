package com.mrleonardos.codecore.internal.client.avatar;

import java.util.UUID;

import com.mrleonardos.codecore.api.client.avatar.AvatarProvider;
import com.mrleonardos.codecore.api.client.image.ImageSource;

/**
 * Аватар по шаблону адреса.
 *
 * <p>
 * Самый простой случай: картинки лежат на сайте проекта под ником или идентификатором игрока.
 */
final class UrlTemplateProvider implements AvatarProvider {

    private final String template;

    UrlTemplateProvider(String template) {
        this.template = template;
    }

    @Override
    public ImageSource sourceFor(UUID playerId, String playerName) {
        String address = AvatarPlaceholders.apply(template, playerId, playerName);
        if (address.isEmpty()) {
            return null;
        }
        try {
            return ImageSource.url(address);
        } catch (IllegalArgumentException wrongScheme) {
            return null;
        }
    }
}
