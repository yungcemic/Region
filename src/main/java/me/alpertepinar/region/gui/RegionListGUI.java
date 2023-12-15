package me.alpertepinar.region.gui;

import me.alpertepinar.region.RegionPlugin;
import me.alpertepinar.region.player.RegionPlayer;
import me.alpertepinar.region.region.Region;
import me.alpertepinar.region.region.RegionManager;
import me.alpertepinar.region.util.ItemStackUtil;
import me.alpertepinar.region.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RegionListGUI implements InventoryHolder {

    private static final String CFG_PATH = "gui-design.region-list";

    private final RegionPlugin plugin;
    private final RegionManager regionManager;
    private final RegionPlayer regionPlayer;
    private final List<Region> regions;
    private final List<RegionGUIButton> buttons;
    private final Player player;
    private List<Integer> regionSlots;
    private Inventory inventory;

    private int page = 0;

    public RegionListGUI(RegionPlugin plugin, RegionManager regionManager, RegionPlayer regionPlayer, List<Region> regions, Player player) {
        this.plugin = plugin;
        this.regionManager = regionManager;
        this.regionPlayer = regionPlayer;
        this.regions = regions;
        this.player = player;
        this.buttons = new ArrayList<>();
    }

    public void openInventory() {
        inventory = Bukkit.createInventory(this,
                getConfig().getInt(CFG_PATH + ".size"),
                StringUtil.colorize(getConfig().getString(CFG_PATH + ".title")));
        player.openInventory(inventory);
        regionSlots = getConfig().getIntegerList(CFG_PATH + ".region-slot-list");
        for (String buttonKey : getConfig().getConfigurationSection(CFG_PATH + ".button-list").getKeys(false)) {
            buttons.add(RegionGUIButton.fromConfig(getConfig().getConfigurationSection(CFG_PATH + ".button-list." + buttonKey)));
        }
        updateInventory();
    }

    public void updateInventory() {
        inventory.clear();
        int startIndex = page * regionSlots.size();
        int endIndex = Math.min(startIndex + regionSlots.size(), regions.size());
        for (int i = startIndex; i < endIndex; i++) {
            Region region = regions.get(i);
            ItemStack regionItem = ItemStackUtil.getItemStackFromConfig(getConfig().getConfigurationSection(CFG_PATH + ".button-list.region-item"));
            ItemMeta regionItemMeta = regionItem.getItemMeta();
            regionItemMeta.setDisplayName(String.format(regionItemMeta.getDisplayName(), region.getName()));
            regionItem.setItemMeta(regionItemMeta);
            inventory.setItem(regionSlots.get(i - startIndex), regionItem);
        }
        for (RegionGUIButton button : buttons) {
            if (button.name().equals("previous-page") && page == 0) return;
            if (button.name().equals("next-page") && Math.max(page * regionSlots.size(), regionSlots.size()) >= regions.size())
                continue;
            ItemStack itemStack = button.itemStack().clone();
            ItemMeta itemMeta = updateItemMeta(itemStack);
            itemStack.setItemMeta(itemMeta);
            button.slotList().forEach(integer -> inventory.setItem(integer, itemStack));
        }
    }

    public void handleClick(InventoryClickEvent e) {
        if (e.getClickedInventory() != this.inventory) return;
        if (e.getCurrentItem() == null) return;
        e.setCancelled(true);
        int slot = e.getSlot();
        Optional<RegionGUIButton> button = buttons.stream().filter(guiButton -> guiButton.slotList().contains(slot)).findFirst();
        button.ifPresent(guiButton -> {
            if (guiButton.name().equals("next-page")) {
                page++;
                updateInventory();
            }
            if (guiButton.name().equals("previous-page")) {
                page--;
                updateInventory();
            }
        });
        if (regionSlots.contains(slot)) {
            Region region = regions.get(page * regionSlots.size() + regionSlots.indexOf(slot));
            if (region != null) {
                player.closeInventory();
                new RegionGUI(plugin, regionManager, region, regionPlayer, player).openInventory();
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    private FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    private ItemMeta updateItemMeta(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta.hasDisplayName()) {
            itemMeta.setDisplayName(itemMeta.getDisplayName().replace("%next_page%", String.valueOf(page + 2)).replace("%previous_page%", String.valueOf(page)));
        }
        if (itemMeta.hasLore()) {
            List<String> newLore = new ArrayList<>();
            for (String s : itemMeta.getLore()) {
                newLore.add(s.replace("%next_page%", String.valueOf(page + 2)).replace("%previous_page%", String.valueOf(page)));
            }
            itemMeta.setLore(newLore);
        }
        return itemMeta;
    }
}
