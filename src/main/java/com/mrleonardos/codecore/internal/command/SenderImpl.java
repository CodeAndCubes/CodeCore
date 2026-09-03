package com.mrleonardos.codecore.internal.command;

import java.util.Optional;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import com.mrleonardos.codecore.api.actor.PlayerRef;
import com.mrleonardos.codecore.api.command.CommandSender;
import com.mrleonardos.codecore.api.command.SenderKind;
import com.mrleonardos.codecore.api.command.SenderPosition;

public final class SenderImpl implements CommandSender {

    private final ICommandSender sender;
    private final SenderKind kind;

    public SenderImpl(ICommandSender sender) {
        this.sender = sender;
        this.kind = kindOf(sender);
    }

    @Override
    public SenderKind kind() {
        return kind;
    }

    @Override
    public Optional<PlayerRef> player() {
        if (kind != SenderKind.PLAYER) {
            return Optional.empty();
        }
        EntityPlayerMP player = (EntityPlayerMP) sender;
        return Optional.of(PlayerRef.of(player.getUniqueID(), player.getCommandSenderName()));
    }

    @Override
    public String name() {
        return sender.getCommandSenderName();
    }

    @Override
    public Optional<SenderPosition> position() {
        if (kind == SenderKind.CONSOLE || kind == SenderKind.RCON) {
            return Optional.empty();
        }
        ChunkCoordinates point = sender.getPlayerCoordinates();
        if (point == null) {
            return Optional.empty();
        }
        return Optional.of(new SenderPosition(dimensionOf(sender), point.posX, point.posY, point.posZ));
    }

    @Override
    public void reply(String translationKey, Object... arguments) {
        sender.addChatMessage(new ChatComponentTranslation(translationKey, arguments));
    }

    @Override
    public void replyError(String translationKey, Object... arguments) {
        IChatComponent message = new ChatComponentTranslation(translationKey, arguments);
        message.getChatStyle()
            .setColor(EnumChatFormatting.RED);
        sender.addChatMessage(message);
    }

    public ICommandSender platform() {
        return sender;
    }

    private static SenderKind kindOf(ICommandSender sender) {
        if (sender instanceof EntityPlayerMP) {
            return SenderKind.PLAYER;
        }
        if (sender instanceof RConConsoleSource) {
            return SenderKind.RCON;
        }
        if (sender instanceof CommandBlockLogic) {
            return SenderKind.COMMAND_BLOCK;
        }
        return SenderKind.CONSOLE;
    }

    private static int dimensionOf(ICommandSender sender) {
        return sender.getEntityWorld() == null ? 0 : sender.getEntityWorld().provider.dimensionId;
    }
}
