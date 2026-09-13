package com.mrleonardos.codecore.internal;

import java.nio.file.Path;

import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.api.CoreRuntime;
import com.mrleonardos.codecore.api.actor.PlayerDirectory;
import com.mrleonardos.codecore.api.adapter.AdapterRegistry;
import com.mrleonardos.codecore.api.command.CommandService;
import com.mrleonardos.codecore.api.config.ConfigService;
import com.mrleonardos.codecore.api.net.NetworkService;
import com.mrleonardos.codecore.api.service.ServiceRegistry;
import com.mrleonardos.codecore.api.util.Scheduler;
import com.mrleonardos.codecore.internal.adapter.AdapterRegistryImpl;
import com.mrleonardos.codecore.internal.command.CommandServiceImpl;
import com.mrleonardos.codecore.internal.config.ConfigPaths;
import com.mrleonardos.codecore.internal.config.ConfigServiceImpl;
import com.mrleonardos.codecore.internal.db.DatabaseServiceImpl;
import com.mrleonardos.codecore.internal.net.NetworkServiceImpl;
import com.mrleonardos.codecore.internal.schedule.MainThreadQueue;
import com.mrleonardos.codecore.internal.schedule.SchedulerImpl;
import com.mrleonardos.codecore.internal.schedule.TickDriver;
import com.mrleonardos.codecore.internal.service.ServiceRegistryImpl;
import com.mrleonardos.codecore.platform.ServerPlayers;

import cpw.mods.fml.common.event.FMLServerStartingEvent;

public final class CoreRuntimeImpl implements CoreRuntime {

    private static final String SERVER_QUEUE = "server";
    private static final String CLIENT_QUEUE = "client";

    private final ServiceRegistryImpl services;
    private final ConfigServiceImpl configs;
    private final CoreSections sections;
    private final AdapterRegistryImpl adapters;
    private final MainThreadQueue serverQueue;
    private final MainThreadQueue clientQueue;
    private final SchedulerImpl scheduler;
    private final NetworkServiceImpl network;
    private final CommandServiceImpl commands;
    private final TickDriver tickDriver;
    private final ServerPlayers players;
    private final Logger log;

    private DatabaseServiceImpl databases;

    public CoreRuntimeImpl(Path configDirectory, Logger log) {
        this.services = new ServiceRegistryImpl(log);
        this.configs = new ConfigServiceImpl(new ConfigPaths(configDirectory), log);
        this.sections = new CoreSections(configs, log);
        this.adapters = new AdapterRegistryImpl(services, log);
        this.serverQueue = new MainThreadQueue(SERVER_QUEUE, log);
        this.clientQueue = new MainThreadQueue(CLIENT_QUEUE, log);
        this.scheduler = new SchedulerImpl(serverQueue, clientQueue);
        this.network = new NetworkServiceImpl(scheduler, log);
        this.commands = new CommandServiceImpl(log);
        this.tickDriver = new TickDriver(serverQueue, clientQueue);
        this.players = new ServerPlayers();
        this.log = log;
    }

    @Override
    public PlayerDirectory players() {
        return players;
    }

    @Override
    public ServiceRegistry services() {
        return services;
    }

    @Override
    public AdapterRegistry adapters() {
        return adapters;
    }

    @Override
    public ConfigService configs() {
        return configs;
    }

    @Override
    public NetworkService network() {
        return network;
    }

    @Override
    public Scheduler scheduler() {
        return scheduler;
    }

    @Override
    public CommandService commands() {
        return commands;
    }

    /** Секции главного файла, которые объявило ядро. */
    public CoreSections sections() {
        return sections;
    }

    /** Журнал ядра: его же берут встроенные сервисы. */
    public Logger log() {
        return log;
    }

    /** Запомнить встроенный сервис баз: его надо проверить при старте сервера и закрыть при остановке. */
    public void databases(DatabaseServiceImpl service) {
        this.databases = service;
    }

    /**
     * Спросить каждую базу тестовым запросом.
     *
     * <p>
     * Зовётся при старте сервера. Главный поток не ждёт ответов: каждая база отвечает в своём рабочем
     * потоке, а строка сводки появляется, когда ответили все.
     */
    public void checkDatabases() {
        if (databases != null) {
            databases.checkAll();
        }
    }

    /** Закрыть пулы и оборвать недошедшие задачи: сервер останавливается. */
    public void closeDatabases() {
        if (databases != null) {
            databases.close();
        }
    }

    /**
     * Идёт ли серверный тик прямо сейчас.
     *
     * <p>
     * Слой баз запрещает держать главный поток, но подъём при старте и запись при остановке идут в тот же
     * поток, в котором тика ещё нет: очередь задач в эти моменты никто не крутит, и ждать в ней ответа
     * значило бы ждать вечно.
     */
    public boolean ticking() {
        return tickDriver.ticking();
    }

    /** Отдать накопленные команды стартующему серверу. */
    public void installCommands(FMLServerStartingEvent event) {
        commands.installAll(event);
    }

    /** Обработчик тиков, который крутит очереди главного потока: подписывается на шину событий. */
    public TickDriver tickDriver() {
        return tickDriver;
    }

    /**
     * Закрывает приём регистраций: вызывается ядром в конце загрузки модов.
     *
     * <p>
     * Порядок жёсткий: сначала решаются роли, потому что победитель каждой из них ещё регистрирует свои
     * реализации, затем пишется главный файл со всеми объявленными секциями и ролями, и только потом
     * реестр сервисов замораживается.
     */
    public void freeze() {
        adapters.decide(configs.main());
        configs.seal(adapters.roles());
        services.freeze();
    }

    /** Мир загружен: состояние мира можно читать. */
    public void attachWorld(Path worldDirectory) {
        configs.attachWorld(worldDirectory);
    }

    /** Мир выгружается: состояние сохраняется, а незавершённые серверные задачи отбрасываются. */
    public void detachWorld() {
        configs.detachWorld();
        serverQueue.clear();
    }

    /** Клиент отключился от сервера: задачи из последних пакетов выполнять уже негде. */
    public void detachClient() {
        clientQueue.clear();
    }
}
