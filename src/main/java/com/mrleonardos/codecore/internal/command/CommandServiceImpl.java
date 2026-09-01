package com.mrleonardos.codecore.internal.command;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.api.command.CommandNode;
import com.mrleonardos.codecore.api.command.CommandService;

import cpw.mods.fml.common.event.FMLServerStartingEvent;

/**
 * Собирает команды модов и отдаёт их серверу при запуске.
 *
 * <p>
 * Моды объявляют команды на своей инициализации, когда сервера ещё нет. Список хранится до старта, а при
 * каждом следующем запуске мира регистрация повторяется, иначе после выхода в главное меню и захода в
 * другой мир команды пропали бы.
 */
public final class CommandServiceImpl implements CommandService {

    private final List<CommandNode> roots = new ArrayList<>();
    private final Logger log;

    public CommandServiceImpl(Logger log) {
        this.log = log;
    }

    @Override
    public void register(CommandNode root) {
        roots.add(root);
    }

    /** Отдать все накопленные команды стартующему серверу. */
    public void installAll(FMLServerStartingEvent event) {
        for (CommandNode root : roots) {
            event.registerServerCommand(new CommandBridge(root));
        }
        log.info("Registered {} command(s)", roots.size());
    }
}
