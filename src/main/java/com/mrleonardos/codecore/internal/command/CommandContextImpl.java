package com.mrleonardos.codecore.internal.command;

import java.util.Map;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import com.mrleonardos.codecore.api.command.CommandContext;
import com.mrleonardos.codecore.api.command.CommandSender;
import com.mrleonardos.codecore.platform.Senders;

public final class CommandContextImpl implements CommandContext {

    private final ICommandSender sender;
    private final Map<String, Object> values;

    CommandContextImpl(ICommandSender sender, Map<String, Object> values) {
        this.sender = sender;
        this.values = values;
    }

    @Override
    public ICommandSender sender() {
        return sender;
    }

    @Override
    public CommandSender caller() {
        return Senders.of(sender);
    }

    @Override
    public EntityPlayerMP player() {
        return sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        if (!values.containsKey(name)) {
            throw new IllegalArgumentException("Command has no argument named " + name);
        }
        return (T) values.get(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String name, T fallback) {
        return values.containsKey(name) ? (T) values.get(name) : fallback;
    }

    @Override
    public boolean has(String name) {
        return values.containsKey(name);
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
}
