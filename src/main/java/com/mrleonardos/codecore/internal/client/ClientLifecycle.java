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
 *
 * <p>
 * Событие приходит из сетевого потока, а TextureManager и поля рисующего кода принадлежат клиентскому.
 * Очистка уезжает в клиентский тик, как и обработка пакетов. Очередь чистится до постановки очистки:
 * задача из последних пакетов не должна выполняться над уже забытым состоянием, а сама очистка не должна
 * стираться вместе с ними.
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
        runtime.detachClient();
        runtime.scheduler()
            .onClientThread(this::forgetServerState);
    }

    private void forgetServerState() {
        avatars.reset();
        images.clearMemory();
        actionBar.clear();
        outline.clear();
    }
}
