package com.mrleonardos.codecore.internal.permission;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import com.mrleonardos.codecore.api.config.ConfigFile;
import com.mrleonardos.codecore.api.service.PermissionService;

/**
 * Права по группам из json.
 *
 * <p>
 * Порядок разбора идёт от частного к общему: личные правила игрока, затем его группа, затем то, что она
 * наследует. Первый источник, давший ответ, его и определяет, поэтому личный запрет невозможно перебить
 * групповым разрешением.
 *
 * <p>
 * Внутри источника выигрывает самое точное правило, а при равной точности — запрет. Это привычная логика:
 * выдать {@code codechat.*} и отдельно отобрать {@code codechat.create}.
 */
public final class JsonPermissionService implements PermissionService {

    private final ConfigFile<PermissionFile> config;
    private final OperatorCheck operators;

    public JsonPermissionService(ConfigFile<PermissionFile> config, OperatorCheck operators) {
        this.config = config;
        this.operators = operators;
    }

    @Override
    public boolean has(UUID player, String node) {
        PermissionFile file = config.get();
        PlayerEntry entry = file.players.get(player.toString());

        if (entry != null) {
            Boolean personal = evaluate(entry.nodes, node);
            if (personal != null) {
                return personal;
            }
        }

        for (GroupEntry group : groupChain(file, groupNameOf(file, entry, player))) {
            Boolean granted = evaluate(group.nodes, node);
            if (granted != null) {
                return granted;
            }
        }
        return false;
    }

    @Override
    public boolean has(ICommandSender sender, String node) {
        if (!(sender instanceof EntityPlayer)) {
            return true;
        }
        return has(((EntityPlayer) sender).getUniqueID(), node);
    }

    @Override
    public String group(UUID player) {
        PermissionFile file = config.get();
        return groupNameOf(file, file.players.get(player.toString()), player);
    }

    @Override
    public String meta(UUID player, String key, String fallback) {
        PermissionFile file = config.get();
        PlayerEntry entry = file.players.get(player.toString());

        if (entry != null) {
            String personal = entry.meta.get(key);
            if (personal != null) {
                return personal;
            }
        }

        for (GroupEntry group : groupChain(file, groupNameOf(file, entry, player))) {
            String value = group.meta.get(key);
            if (value != null) {
                return value;
            }
        }
        return fallback;
    }

    private String groupNameOf(PermissionFile file, PlayerEntry entry, UUID player) {
        if (entry != null && entry.group != null && !entry.group.isEmpty()) {
            return entry.group;
        }
        if (operators.isOperator(player)) {
            return file.opGroup;
        }
        return file.defaultGroup;
    }

    private List<GroupEntry> groupChain(PermissionFile file, String groupName) {
        List<GroupEntry> chain = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Deque<String> pending = new ArrayDeque<>();
        pending.add(groupName);

        while (!pending.isEmpty()) {
            String current = pending.poll();
            if (!visited.add(current)) {
                continue;
            }
            GroupEntry group = file.groups.get(current);
            if (group == null) {
                continue;
            }
            chain.add(group);
            pending.addAll(group.inherits);
        }
        return chain;
    }

    private Boolean evaluate(List<String> rules, String node) {
        int bestScore = -1;
        Boolean result = null;

        for (String rule : rules) {
            boolean denied = !rule.isEmpty() && rule.charAt(0) == PermissionKeys.DENY_PREFIX;
            String pattern = denied ? rule.substring(1) : rule;
            if (!NodeMatcher.matches(pattern, node)) {
                continue;
            }
            int score = NodeMatcher.specificity(pattern);
            if (score > bestScore || (score == bestScore && denied)) {
                bestScore = score;
                result = !denied;
            }
        }
        return result;
    }
}
