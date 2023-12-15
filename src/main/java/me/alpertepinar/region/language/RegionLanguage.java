package me.alpertepinar.region.language;

import me.alpertepinar.region.util.StringUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RegionLanguage {

    private static final Map<String, String> language = new HashMap<>();
    private static final List<String> helpMessage = new ArrayList<>();

    public static synchronized void loadMessages(ConfigurationSection section) {
        helpMessage.clear();
        language.clear();
        section.getKeys(false).forEach(s -> {
            if (s.equals("help")) {
                helpMessage.addAll(StringUtil.colorize(section.getStringList(s)));
            }
            language.put(s, StringUtil.colorize(section.getString(s)));
        });
    }

    public static String getMessage(String name) {
        return language.getOrDefault(name, "");
    }

    public static List<String> getHelpMessage() {
        return helpMessage;
    }

}
