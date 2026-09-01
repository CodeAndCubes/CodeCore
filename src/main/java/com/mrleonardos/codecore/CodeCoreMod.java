package com.mrleonardos.codecore;

import java.io.File;

import net.minecraftforge.common.DimensionManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.api.CodeApi;
import com.mrleonardos.codecore.api.config.ConfigScope;
import com.mrleonardos.codecore.api.config.ConfigSpec;
import com.mrleonardos.codecore.internal.BuiltinServices;
import com.mrleonardos.codecore.internal.CoreRuntimeImpl;
import com.mrleonardos.codecore.internal.SideSetup;
import com.mrleonardos.codecore.internal.avatar.AvatarConfigSender;
import com.mrleonardos.codecore.internal.avatar.AvatarSettings;
import com.mrleonardos.codecore.internal.command.CoreCommands;
import com.mrleonardos.codecore.internal.net.CorePackets;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

@Mod(
    modid = CoreConstants.MODID,
    name = CoreConstants.MOD_NAME,
    version = Tags.VERSION,
    acceptedMinecraftVersions = CoreConstants.ACCEPTED_MINECRAFT_VERSIONS)
public final class CodeCoreMod {

    public static final Logger LOG = LogManager.getLogger(CoreConstants.MOD_NAME);

    private static final String AVATARS_FILE = "avatars";

    @SidedProxy(
        clientSide = "com.mrleonardos.codecore.internal.client.ClientSetup",
        serverSide = "com.mrleonardos.codecore.internal.DedicatedSetup")
    public static SideSetup side;

    private CoreRuntimeImpl runtime;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        runtime = new CoreRuntimeImpl(
            event.getModConfigurationDirectory()
                .toPath(),
            LOG);
        CodeApi.install(runtime);
        FMLCommonHandler.instance()
            .bus()
            .register(runtime.tickDriver());
        CorePackets.register();
        LOG.info("CodeCore {} is starting up", Tags.VERSION);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        BuiltinServices.register(LOG);
        CodeApi.commands()
            .register(CoreCommands.root());
        FMLCommonHandler.instance()
            .bus()
            .register(
                new AvatarConfigSender(
                    CodeApi.configs()
                        .open(avatarSpec())));
        side.install(runtime);
    }

    private static ConfigSpec<AvatarSettings> avatarSpec() {
        return ConfigSpec.of(CoreConstants.MODID, AVATARS_FILE, AvatarSettings.class)
            .scope(ConfigScope.SETTINGS)
            .build();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        runtime.freeze();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        runtime.installCommands(event);

        File worldDirectory = DimensionManager.getCurrentSaveRootDirectory();
        if (worldDirectory == null) {
            LOG.warn("World directory is not available at server start, world state configs stay unloaded");
            return;
        }
        runtime.attachWorld(worldDirectory.toPath());
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        runtime.detachWorld();
    }
}
