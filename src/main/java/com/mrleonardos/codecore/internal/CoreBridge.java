package com.mrleonardos.codecore.internal;

import com.mrleonardos.codecore.api.avatar.AvatarConfig;

/**
 * Связь общего кода с клиентской половиной.
 *
 * <p>
 * Пакеты общие для обеих сторон, а применяет их клиент. Ссылаться из пакета на клиентский класс нельзя:
 * в серверном jar его нет. Поэтому обработчик обращается сюда, а клиент при запуске подставляет свою
 * реализацию.
 */
public final class CoreBridge {

    private static AvatarConfigSink avatars;
    private static ActionBarSink actionBar;

    private CoreBridge() {}

    public static void avatars(AvatarConfigSink sink) {
        avatars = sink;
    }

    public static void actionBar(ActionBarSink sink) {
        actionBar = sink;
    }

    /** Применить присланную сервером настройку аватаров; на сервере вызов ничего не делает. */
    public static void applyAvatars(AvatarConfig config) {
        if (avatars != null) {
            avatars.apply(config);
        }
    }

    /** Показать строку над хотбаром; на сервере вызов ничего не делает. */
    public static void showActionBar(String text, int seconds) {
        if (actionBar != null) {
            actionBar.show(text, seconds);
        }
    }
}
