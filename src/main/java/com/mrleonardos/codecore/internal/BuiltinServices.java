package com.mrleonardos.codecore.internal;

import com.mrleonardos.codecore.CoreConstants;
import com.mrleonardos.codecore.api.config.ConfigFormat;
import com.mrleonardos.codecore.api.config.ConfigRoles;
import com.mrleonardos.codecore.api.config.ConfigSpec;
import com.mrleonardos.codecore.api.db.DatabaseService;
import com.mrleonardos.codecore.api.service.ActionBarService;
import com.mrleonardos.codecore.api.service.ServicePriority;
import com.mrleonardos.codecore.internal.db.DatabaseServiceImpl;
import com.mrleonardos.codecore.internal.db.DatabasesFile;
import com.mrleonardos.codecore.internal.db.MainThreads;
import com.mrleonardos.codecore.internal.net.ActionBarPacket;
import com.mrleonardos.codecore.internal.net.CorePackets;
import com.mrleonardos.codecore.internal.text.ActionBarImpl;

/**
 * Сервисы, которые ядро приносит само.
 *
 * <p>
 * Ролью строка над хотбаром не объявлена нарочно: подменять тут нечего, рисует её один и тот же клиент
 * линейки. Вес {@link ServicePriority#BUILTIN} оставляет дорогу моду, который захочет рисовать её
 * по-своему.
 *
 * <p>
 * Тем же весом встаёт слой баз. Встроенный знает MariaDB и SQLite, а чужой мод волен принести свою
 * реализацию с любым движком: реестр это допускает, и рамка двух движков касается только встроенной.
 */
public final class BuiltinServices {

    private static final String DATABASES_FILE = "databases";

    private BuiltinServices() {}

    public static void install(CoreRuntimeImpl runtime) {
        ActionBarImpl actionBar = new ActionBarImpl(
            () -> runtime.sections()
                .hud()
                .get().actionBarRepeatSeconds,
            System::currentTimeMillis,
            (player, text, seconds) -> CorePackets.channel()
                .toPlayer(new ActionBarPacket(text, seconds), player));
        runtime.services()
            .register(ActionBarService.class, actionBar, ServicePriority.BUILTIN);
        installDatabases(runtime);
    }

    private static void installDatabases(CoreRuntimeImpl runtime) {
        DatabasesFile file = runtime.configs()
            .open(
                ConfigSpec.of(CoreConstants.MODID, DATABASES_FILE, DatabasesFile.class)
                    .role(ConfigRoles.CORE)
                    .format(ConfigFormat.TOML)
                    .build())
            .get();
        DatabaseServiceImpl databases = DatabaseServiceImpl.of(
            file,
            runtime.configs()::serverId,
            runtime.scheduler()::onServerThread,
            () -> MainThreads.current()
                .getAsBoolean() && runtime.ticking(),
            System::currentTimeMillis,
            runtime.log());
        runtime.databases(databases);
        runtime.services()
            .register(DatabaseService.class, databases, ServicePriority.BUILTIN);
    }
}
