package me.alpertepinar.region.util;

import org.bukkit.ChatColor;

import java.util.List;

public final class StringUtil {

    private StringUtil() {
    }

    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> colorize(List<String> text) {
        return text.stream().map(StringUtil::colorize).toList();
    }
}
