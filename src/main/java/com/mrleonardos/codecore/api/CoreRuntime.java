package com.mrleonardos.codecore.api;

import com.mrleonardos.codecore.api.command.CommandService;
import com.mrleonardos.codecore.api.config.ConfigService;
import com.mrleonardos.codecore.api.net.NetworkService;
import com.mrleonardos.codecore.api.service.ServiceRegistry;
import com.mrleonardos.codecore.api.util.Scheduler;

/**
 * Набор возможностей ядра, доступный через {@link CodeApi}.
 *
 * <p>
 * Реализуется самим CodeCore. Отдельный интерфейс нужен, чтобы {@link CodeApi} не знал о внутренних
 * классах, а тесты могли подставить свой рантайм.
 */
public interface CoreRuntime {

    /** Реестр сервисов ядра. */
    ServiceRegistry services();

    /** Файлы настроек: json со схемой, миграциями и атомарной записью. */
    ConfigService configs();

    /** Сетевые каналы модов. */
    NetworkService network();

    /** Выполнение задач в главном потоке. */
    Scheduler scheduler();

    /** Команды сервера. */
    CommandService commands();
}
