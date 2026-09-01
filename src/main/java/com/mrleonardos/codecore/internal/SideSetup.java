package com.mrleonardos.codecore.internal;

/**
 * То, что ядро поднимает только на своей стороне.
 *
 * <p>
 * Реализация выбирается через {@code @SidedProxy}, то есть по имени класса: прямых ссылок на клиентский
 * код в общем не появляется, и он спокойно вырезается из серверного jar.
 */
public interface SideSetup {

    /** Поднять сторону. Вызывается на инициализации мода. */
    void install(CoreRuntimeImpl runtime);
}
