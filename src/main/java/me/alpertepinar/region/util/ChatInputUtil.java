package me.alpertepinar.region.util;

import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class ChatInputUtil {

    private static final Map<UUID, Consumer<String>> inputMap = new HashMap<>();

    private ChatInputUtil() {}

    public static boolean getChatInput(Player p, Consumer<String> input) {
        if (inputMap.get(p.getUniqueId()) != null) return false;
        inputMap.put(p.getUniqueId(), input);
        return true;
    }

    public static void processChatInput(AsyncPlayerChatEvent e) {
        UUID uniqueId = e.getPlayer().getUniqueId();
        Consumer<String> inputProcessor = inputMap.get(uniqueId);
        if (inputProcessor == null) return;
        String message = e.getMessage();
        e.setCancelled(true);
        inputMap.remove(uniqueId);
        if (message.equalsIgnoreCase("cancel") || message.equalsIgnoreCase("exit")) return;
        inputProcessor.accept(message);
    }
}
