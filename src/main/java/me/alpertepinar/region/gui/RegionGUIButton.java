package me.alpertepinar.region.gui;

import me.alpertepinar.region.util.ItemStackUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public record RegionGUIButton(String name, ItemStack itemStack, List<Integer> slotList) {

    public static RegionGUIButton fromConfig(ConfigurationSection section) {
        String name = section.getName();
        ItemStack itemStack = ItemStackUtil.getItemStackFromConfig(section);
        List<Integer> slotList = section.getIntegerList("slot-list");
        if (slotList.isEmpty()) slotList = Collections.singletonList(section.getInt("slot"));
        return new RegionGUIButton(name, itemStack, slotList);
    }
}
