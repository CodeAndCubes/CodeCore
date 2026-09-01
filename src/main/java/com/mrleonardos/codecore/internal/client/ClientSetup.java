package com.mrleonardos.codecore.internal.client;

import java.nio.file.Path;

import com.mrleonardos.codecore.CodeCoreMod;
import com.mrleonardos.codecore.CoreConstants;
import com.mrleonardos.codecore.api.CodeApi;
import com.mrleonardos.codecore.api.avatar.AvatarConfig;
import com.mrleonardos.codecore.api.client.ClientApi;
import com.mrleonardos.codecore.api.client.ClientRuntime;
import com.mrleonardos.codecore.api.client.avatar.AvatarService;
import com.mrleonardos.codecore.api.client.image.ImageService;
import com.mrleonardos.codecore.api.config.ConfigScope;
import com.mrleonardos.codecore.api.config.ConfigSpec;
import com.mrleonardos.codecore.internal.AvatarConfigSink;
import com.mrleonardos.codecore.internal.CoreBridge;
import com.mrleonardos.codecore.internal.CoreRuntimeImpl;
import com.mrleonardos.codecore.internal.SideSetup;
import com.mrleonardos.codecore.internal.client.avatar.AvatarServiceImpl;
import com.mrleonardos.codecore.internal.client.avatar.ClientAvatarSettings;
import com.mrleonardos.codecore.internal.client.image.ImageServiceImpl;

import cpw.mods.fml.common.FMLCommonHandler;

/**
 * Клиентская часть ядра: картинки и аватары.
 *
 * <p>
 * Кэш обработанных картинок лежит рядом с конфигами, а не в папке мира: он не зависит ни от сервера, ни
 * от сохранения, и переживать его удаление не жалко.
 *
 * <p>
 * Источник аватаров приходит от сервера при входе; до этого их попросту нет.
 */
public final class ClientSetup implements SideSetup, ClientRuntime, AvatarConfigSink {

    private static final String AVATARS_FILE = "avatars";
    private static final String CACHE_DIRECTORY = "cache/images";

    private final ClientWorkers workers = new ClientWorkers();

    private ImageServiceImpl imageService;
    private AvatarServiceImpl avatarService;

    @Override
    public void install(CoreRuntimeImpl runtime) {
        Path configDirectory = CodeApi.configs()
            .directory(CoreConstants.MODID);

        ClientAvatarSettings settings = CodeApi.configs()
            .open(
                ConfigSpec.of(CoreConstants.MODID, AVATARS_FILE, ClientAvatarSettings.class)
                    .scope(ConfigScope.CLIENT)
                    .build())
            .get();

        imageService = new ImageServiceImpl(
            configDirectory.resolve(CACHE_DIRECTORY),
            CodeApi.scheduler(),
            workers,
            CodeCoreMod.LOG);
        avatarService = new AvatarServiceImpl(
            settings.enabled,
            imageService,
            configDirectory,
            workers,
            CodeCoreMod.LOG);

        ClientApi.install(this);
        CoreBridge.avatars(this);
        FMLCommonHandler.instance()
            .bus()
            .register(new ClientLifecycle(runtime, imageService, avatarService));
        CodeCoreMod.LOG.info("Client side ready, image formats: {}", imageService.formats());
    }

    @Override
    public void apply(AvatarConfig config) {
        avatarService.reconfigure(config);
    }

    @Override
    public ImageService images() {
        return imageService;
    }

    @Override
    public AvatarService avatars() {
        return avatarService;
    }
}
