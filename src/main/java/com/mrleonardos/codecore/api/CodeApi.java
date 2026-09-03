package com.mrleonardos.codecore.api;

import com.mrleonardos.codecore.api.actor.PlayerDirectory;
import com.mrleonardos.codecore.api.adapter.AdapterRegistry;
import com.mrleonardos.codecore.api.command.CommandService;
import com.mrleonardos.codecore.api.config.ConfigService;
import com.mrleonardos.codecore.api.net.NetworkService;
import com.mrleonardos.codecore.api.service.ServiceRegistry;
import com.mrleonardos.codecore.api.util.Scheduler;

/**
 * Единственная точка входа в CodeCore для модов-потребителей.
 *
 * <p>
 * Всё, что предоставляет ядро, берётся отсюда и только отсюда: конкретные реализации живут во
 * внутренних пакетах и могут меняться без предупреждения.
 *
 * <p>
 * Готово к использованию с фазы инициализации мода. Более раннее обращение означает ошибку порядка
 * загрузки: ядро скажет об этом явно, а не отдаст полупустой объект.
 */
public final class CodeApi {

    private static volatile CoreRuntime runtime;

    private CodeApi() {}

    /** Реестр сервисов: сюда регистрируют реализации и отсюда их получают. */
    public static ServiceRegistry services() {
        return runtime().services();
    }

    /** Реестр адаптеров: кто держит каждую область ответственности. */
    public static AdapterRegistry adapters() {
        return runtime().adapters();
    }

    /** Файлы настроек мода. */
    public static ConfigService configs() {
        return runtime().configs();
    }

    /** Сетевые каналы: свой канал на мод. */
    public static NetworkService network() {
        return runtime().network();
    }

    /** Выполнение задач в главном потоке своей стороны. */
    public static Scheduler scheduler() {
        return runtime().scheduler();
    }

    /** Команды сервера: дерево подкоманд с правами и автодополнением. */
    public static CommandService commands() {
        return runtime().commands();
    }

    /** Кто сейчас на сервере: ссылки на игроков по нику и по идентификатору. */
    public static PlayerDirectory players() {
        return runtime().players();
    }

    /**
     * Подключает рантайм ядра. Вызывается самим CodeCore при запуске, модам-потребителям этот метод
     * не нужен.
     *
     * @throws IllegalStateException если рантайм уже подключён
     */
    public static void install(CoreRuntime installed) {
        if (runtime != null) {
            throw new IllegalStateException("CodeCore runtime is already installed");
        }
        runtime = installed;
    }

    private static CoreRuntime runtime() {
        if (runtime == null) {
            throw new IllegalStateException(
                "CodeCore runtime is not ready yet, request it no earlier than your mod's init phase");
        }
        return runtime;
    }
}
