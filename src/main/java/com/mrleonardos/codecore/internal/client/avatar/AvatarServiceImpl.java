package com.mrleonardos.codecore.internal.client.avatar;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.api.avatar.AvatarConfig;
import com.mrleonardos.codecore.api.client.avatar.AvatarProvider;
import com.mrleonardos.codecore.api.client.avatar.AvatarService;
import com.mrleonardos.codecore.api.client.image.ImageHandle;
import com.mrleonardos.codecore.api.client.image.ImageRequest;
import com.mrleonardos.codecore.api.client.image.ImageService;
import com.mrleonardos.codecore.api.client.image.ImageSource;

/**
 * Аватары поверх сервиса изображений.
 *
 * <p>
 * Откуда их брать, говорит сервер: до его сообщения источника нет и аватары не показываются. Провайдеры,
 * добавленные модами, спрашиваются первыми, и переключение сервера их не сбрасывает. Иначе мод,
 * поставивший свой источник на инициализации, терял бы его при каждом входе в игру.
 *
 * <p>
 * Настройка живёт ровно до отключения. Иначе клиент, вышедший с сервера A, продолжал бы ходить на его
 * эндпоинт с никами игроков сервера B и тем самым сдавал бы состав сервера B владельцу сервера A.
 *
 * <p>
 * Игрок может выключить аватары у себя целиком; это его настройка, и сервер её не перебивает.
 */
public final class AvatarServiceImpl implements AvatarService {

    private static final String DEFAULT_FOLDER = "avatars";

    private final List<AvatarProvider> custom = new ArrayList<>();
    private final ImageService images;
    private final Path configDirectory;
    private final Executor workers;
    private final Logger log;
    private final boolean allowedByPlayer;

    private AvatarProvider configured;
    private int size = AvatarConfig.MIN_SIZE;

    public AvatarServiceImpl(boolean allowedByPlayer, ImageService images, Path configDirectory, Executor workers,
        Logger log) {
        this.allowedByPlayer = allowedByPlayer;
        this.images = images;
        this.configDirectory = configDirectory;
        this.workers = workers;
        this.log = log;
    }

    /** Принять настройку сервера: источник меняется на лету, уже загруженные картинки остаются в кэше. */
    public void reconfigure(AvatarConfig config) {
        if (!allowedByPlayer) {
            return;
        }
        configured = build(config);
        size = config.size();
        log.info("Avatars are taken from {}", describe(config));
    }

    /**
     * Сервер отключился: его источник аватаров вместе с накопленными адресами больше не действует.
     *
     * <p>
     * Зовётся из клиентского тика при выходе с сервера, поэтому поля видны рисующему коду без синхронизации.
     */
    public void reset() {
        if (configured == null) {
            return;
        }
        configured = null;
        size = AvatarConfig.MIN_SIZE;
        log.info("Avatars are taken from {}", AvatarConfig.PROVIDER_NONE);
    }

    @Override
    public ImageHandle avatar(UUID playerId, String playerName, int requestedSize) {
        if (!allowedByPlayer) {
            return null;
        }
        for (AvatarProvider provider : custom) {
            ImageHandle handle = requestFrom(provider, playerId, playerName, requestedSize);
            if (handle != null) {
                return handle;
            }
        }
        return configured == null ? null : requestFrom(configured, playerId, playerName, requestedSize);
    }

    @Override
    public void addProvider(AvatarProvider provider) {
        custom.add(0, provider);
    }

    @Override
    public boolean enabled() {
        return allowedByPlayer && (configured != null || !custom.isEmpty());
    }

    private ImageHandle requestFrom(AvatarProvider provider, UUID playerId, String playerName, int requestedSize) {
        ImageSource source = provider.sourceFor(playerId, playerName);
        return source == null ? null : images.request(ImageRequest.of(source, Math.max(requestedSize, size)));
    }

    private AvatarProvider build(AvatarConfig config) {
        if (!config.enabled()) {
            return null;
        }
        if (AvatarConfig.PROVIDER_URL.equals(config.provider())) {
            return remote(config) ? new UrlTemplateProvider(config.url()) : null;
        }
        if (AvatarConfig.PROVIDER_JSON.equals(config.provider())) {
            return remote(config) ? new JsonEndpointProvider(config.url(), config.jsonPath(), workers, log) : null;
        }
        return new LocalFolderProvider(folderOf(config));
    }

    private static String describe(AvatarConfig config) {
        String place = config.url()
            .isEmpty() ? config.folder() : config.url();
        return config.provider() + (place.isEmpty() ? "" : " " + place);
    }

    private boolean remote(AvatarConfig config) {
        if (ImageSource.allowed(config.url())) {
            return true;
        }
        log.warn("Avatar address {} is not http or https, avatars stay off", config.url());
        return false;
    }

    private Path folderOf(AvatarConfig config) {
        Path fallback = configDirectory.resolve(DEFAULT_FOLDER);
        if (config.folder()
            .isEmpty()) {
            return fallback;
        }
        Path candidate = configDirectory.resolve(config.folder())
            .normalize();
        if (candidate.startsWith(configDirectory.normalize())) {
            return candidate;
        }
        log.warn("Avatar folder {} leads outside the config directory, using {}", config.folder(), fallback);
        return fallback;
    }
}
