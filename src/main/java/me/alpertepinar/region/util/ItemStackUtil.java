package me.alpertepinar.region.util;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ItemStackUtil {

    private ItemStackUtil() {}

    public static ItemStack getItemStackFromConfig(ConfigurationSection section) {
        ItemStack itemStack = new ItemStack(Material.matchMaterial(section.getString("material")));
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (section.isSet("display-name")) {
            itemMeta.setDisplayName(StringUtil.colorize(section.getString("display-name")));
        }
        if (section.isSet("lore")) {
            itemMeta.setLore(StringUtil.colorize(section.getStringList("lore")));
        }
        if (section.isSet("amount")) {
            itemStack.setAmount(section.getInt("amount"));
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

}
