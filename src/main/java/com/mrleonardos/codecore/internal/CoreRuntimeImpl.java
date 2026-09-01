package com.mrleonardos.codecore.internal;

import java.nio.file.Path;

import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.api.CoreRuntime;
import com.mrleonardos.codecore.api.command.CommandService;
import com.mrleonardos.codecore.api.config.ConfigService;
import com.mrleonardos.codecore.api.net.NetworkService;
import com.mrleonardos.codecore.api.service.ServiceRegistry;
import com.mrleonardos.codecore.api.util.Scheduler;
import com.mrleonardos.codecore.internal.command.CommandServiceImpl;
import com.mrleonardos.codecore.internal.config.ConfigPaths;
import com.mrleonardos.codecore.internal.config.JsonConfigService;
import com.mrleonardos.codecore.internal.net.NetworkServiceImpl;
import com.mrleonardos.codecore.internal.schedule.MainThreadQueue;
import com.mrleonardos.codecore.internal.schedule.SchedulerImpl;
import com.mrleonardos.codecore.internal.schedule.TickDriver;
import com.mrleonardos.codecore.internal.service.ServiceRegistryImpl;

import cpw.mods.fml.common.event.FMLServerStartingEvent;

public final class CoreRuntimeImpl implements CoreRuntime {

    private static final String SERVER_QUEUE = "server";
    private static final String CLIENT_QUEUE = "client";

    private final ServiceRegistryImpl services;
    private final JsonConfigService configs;
    private final MainThreadQueue serverQueue;
    private final MainThreadQueue clientQueue;
    private final SchedulerImpl scheduler;
    private final NetworkServiceImpl network;
    private final CommandServiceImpl commands;
    private final TickDriver tickDriver;

    public CoreRuntimeImpl(Path configDirectory, Logger log) {
        this.services = new ServiceRegistryImpl(log);
        this.configs = new JsonConfigService(new ConfigPaths(configDirectory), log);
        this.serverQueue = new MainThreadQueue(SERVER_QUEUE, log);
        this.clientQueue = new MainThreadQueue(CLIENT_QUEUE, log);
        this.scheduler = new SchedulerImpl(serverQueue, clientQueue);
        this.network = new NetworkServiceImpl(scheduler, log);
        this.commands = new CommandServiceImpl(log);
        this.tickDriver = new TickDriver(serverQueue, clientQueue);
    }

    @Override
    public ServiceRegistry services() {
        return services;
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

    /** Отдать накопленные команды стартующему серверу. */
    public void installCommands(FMLServerStartingEvent event) {
        commands.installAll(event);
    }

    /** Обработчик тиков, который крутит очереди главного потока: подписывается на шину событий. */
    public TickDriver tickDriver() {
        return tickDriver;
    }

    /** Закрывает приём регистраций: вызывается ядром в конце загрузки модов. */
    public void freeze() {
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
