package com.mrleonardos.codecore.internal.client;

import java.nio.file.Path;

import net.minecraftforge.common.MinecraftForge;

import com.mrleonardos.codecore.CodeCoreMod;
import com.mrleonardos.codecore.CoreConstants;
import com.mrleonardos.codecore.api.CodeApi;
import com.mrleonardos.codecore.api.avatar.AvatarConfig;
import com.mrleonardos.codecore.api.client.ClientApi;
import com.mrleonardos.codecore.api.client.ClientRuntime;
import com.mrleonardos.codecore.api.client.avatar.AvatarService;
import com.mrleonardos.codecore.api.client.image.ImageLimits;
import com.mrleonardos.codecore.api.client.image.ImageService;
import com.mrleonardos.codecore.api.client.ui.render.Painter;
import com.mrleonardos.codecore.api.client.ui.render.WorldOutline;
import com.mrleonardos.codecore.api.config.ConfigFile;
import com.mrleonardos.codecore.api.config.ConfigRoles;
import com.mrleonardos.codecore.api.config.ConfigScope;
import com.mrleonardos.codecore.api.config.ConfigSpec;
import com.mrleonardos.codecore.internal.AvatarConfigSink;
import com.mrleonardos.codecore.internal.CoreBridge;
import com.mrleonardos.codecore.internal.CoreRuntimeImpl;
import com.mrleonardos.codecore.internal.SideSetup;
import com.mrleonardos.codecore.internal.client.avatar.AvatarServiceImpl;
import com.mrleonardos.codecore.internal.client.avatar.ClientAvatarSettings;
import com.mrleonardos.codecore.internal.client.image.ImageBudget;
import com.mrleonardos.codecore.internal.client.image.ImageServiceImpl;
import com.mrleonardos.codecore.internal.client.ui.ActionBarOverlay;
import com.mrleonardos.codecore.internal.client.ui.ClientOutlineSettings;
import com.mrleonardos.codecore.internal.client.ui.GlPainter;
import com.mrleonardos.codecore.internal.client.ui.WorldOutlineImpl;

import cpw.mods.fml.common.FMLCommonHandler;

/**
 * Клиентская часть ядра: картинки, аватары, надписи поверх экрана и рамки в мире.
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
    private static final String OUTLINE_FILE = "outline";
    private static final String CACHE_DIRECTORY = "cache/images";

    private final ClientWorkers workers = new ClientWorkers();
    private final GlPainter painter = new GlPainter();
    private final ActionBarOverlay actionBar = new ActionBarOverlay(painter);

    private ImageServiceImpl imageService;
    private AvatarServiceImpl avatarService;
    private WorldOutlineImpl worldOutline;

    @Override
    public void install(CoreRuntimeImpl runtime) {
        Path configDirectory = CodeApi.configs()
            .directory(ConfigRoles.CORE);

        ImageLimits.install(
            ImageBudget.of(
                runtime.sections()
                    .images()
                    .get(),
                CodeCoreMod.LOG));

        ClientAvatarSettings settings = CodeApi.configs()
            .open(
                ConfigSpec.of(CoreConstants.MODID, AVATARS_FILE, ClientAvatarSettings.class)
                    .role(ConfigRoles.CORE)
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

        ConfigFile<ClientOutlineSettings> outline = CodeApi.configs()
            .open(
                ConfigSpec.of(CoreConstants.MODID, OUTLINE_FILE, ClientOutlineSettings.class)
                    .role(ConfigRoles.CORE)
                    .scope(ConfigScope.CLIENT)
                    .build());
        worldOutline = new WorldOutlineImpl(outline::get);

        ClientApi.install(this);
        CoreBridge.avatars(this);
        CoreBridge.actionBar(actionBar);
        MinecraftForge.EVENT_BUS.register(actionBar);
        MinecraftForge.EVENT_BUS.register(worldOutline);
        FMLCommonHandler.instance()
            .bus()
            .register(new ClientLifecycle(runtime, imageService, avatarService, actionBar, worldOutline));
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

    @Override
    public Painter painter() {
        return painter;
    }

    @Override
    public WorldOutline outline() {
        return worldOutline;
    }
}
