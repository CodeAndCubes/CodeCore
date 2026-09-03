package com.mrleonardos.codecore.api.client;

import com.mrleonardos.codecore.api.client.avatar.AvatarService;
import com.mrleonardos.codecore.api.client.image.ImageService;
import com.mrleonardos.codecore.api.client.ui.render.Painter;

/**
 * Точка входа в клиентскую часть ядра.
 *
 * <p>
 * Отдельно от {@link com.mrleonardos.codecore.api.CodeApi} по той же причине, по какой рендер лежит в
 * клиентском jar: на выделенном сервере этих сервисов нет и быть не должно. Обращаться к ним можно
 * только из клиентского кода.
 */
public final class ClientApi {

    private static ClientRuntime runtime;

    private ClientApi() {}

    /** Картинки: загрузка, обработка, кэш, текстуры. */
    public static ImageService images() {
        return runtime().images();
    }

    /** Аватары игроков. */
    public static AvatarService avatars() {
        return runtime().avatars();
    }

    /** Поверхность рисования: за ней прячется весь OpenGL и весь Minecraft. */
    public static Painter painter() {
        return runtime().painter();
    }

    /** Поднята ли клиентская часть: на выделенном сервере её нет. */
    public static boolean available() {
        return runtime != null;
    }

    /** Ставится ядром при запуске клиента. Мода это не касается. */
    public static void install(ClientRuntime installed) {
        if (runtime != null) {
            throw new IllegalStateException("Client runtime is already installed");
        }
        runtime = installed;
    }

    private static ClientRuntime runtime() {
        if (runtime == null) {
            throw new IllegalStateException("Client side of CodeCore is not available here");
        }
        return runtime;
    }
}
