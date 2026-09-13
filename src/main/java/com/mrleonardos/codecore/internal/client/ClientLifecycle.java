package com.mrleonardos.codecore.internal.client;

import com.mrleonardos.codecore.api.client.image.ImageService;
import com.mrleonardos.codecore.api.client.ui.render.WorldOutline;
import com.mrleonardos.codecore.internal.ActionBarSink;
import com.mrleonardos.codecore.internal.CoreRuntimeImpl;
import com.mrleonardos.codecore.internal.client.avatar.AvatarServiceImpl;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;

/**
 * Что клиент забывает при выходе с сервера.
 *
 * <p>
 * Игра идёт не от запуска до выхода: игрок ходит между серверами и в одиночные миры. Всё, что дал прошлый
 * сервер, к следующему отношения не имеет — ни источник аватаров, ни его картинки, ни задачи, поставленные
 * пакетами последних секунд, которым теперь некуда прийти.
 */
public final class ClientLifecycle {

    private final CoreRuntimeImpl runtime;
    private final ImageService images;
    private final AvatarServiceImpl avatars;
    private final ActionBarSink actionBar;
    private final WorldOutline outline;

    public ClientLifecycle(CoreRuntimeImpl runtime, ImageService images, AvatarServiceImpl avatars,
        ActionBarSink actionBar, WorldOutline outline) {
        this.runtime = runtime;
        this.images = images;
        this.avatars = avatars;
        this.actionBar = actionBar;
        this.outline = outline;
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        avatars.reset();
        images.clearMemory();
        actionBar.clear();
        outline.clear();
        runtime.detachClient();
    }
}
