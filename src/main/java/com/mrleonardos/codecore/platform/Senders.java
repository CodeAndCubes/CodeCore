package com.mrleonardos.codecore.platform;

import net.minecraft.command.ICommandSender;

import com.mrleonardos.codecore.api.command.CommandSender;

/**
 * Переход между игровым отправителем команды и {@link CommandSender}.
 *
 * <p>
 * Раскладка по видам стоит здесь одна на всю линейку. Иначе цепочку {@code instanceof} по
 * {@code EntityPlayerMP}, {@code RConConsoleSource} и {@code CommandBlockLogic} пришлось бы повторить в
 * слое платформы каждого мода, а это тот же дубль перечисления, ради снятия которого затевался переезд:
 * добавили пятый вид отправителя, и четыре копии разъехались молча.
 *
 * <p>
 * Обратный переход нужен заявкам к чужим модам, чей api просит игровой тип. Мостам ядра он не
 * понадобился: ForgeEssentials и LuckPerms спрашивают по идентификатору, UltraMine по нику, а и то, и
 * другое {@link CommandSender} отдаёт сам.
 */
public final class Senders {

    private Senders() {}

    /** Отправитель без типов игры или {@code null}, если игрового отправителя нет. */
    public static CommandSender of(ICommandSender sender) {
        return sender == null ? null : new SenderView(sender);
    }

    /** Игровой отправитель под ссылкой или {@code null}, если ссылка сделана не здесь. */
    public static ICommandSender platform(CommandSender sender) {
        return sender instanceof SenderView ? ((SenderView) sender).platform() : null;
    }
}
