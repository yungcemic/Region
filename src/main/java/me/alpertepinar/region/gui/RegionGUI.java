package me.alpertepinar.region.gui;

import me.alpertepinar.region.RegionPlugin;
import me.alpertepinar.region.language.RegionLanguage;
import me.alpertepinar.region.player.RegionPlayer;
import me.alpertepinar.region.region.Region;
import me.alpertepinar.region.region.RegionManager;
import me.alpertepinar.region.util.ChatInputUtil;
import me.alpertepinar.region.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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

public final class RegionGUI implements InventoryHolder {

    private static final String CFG_PATH = "gui-design.region";

    private final RegionPlugin plugin;
    private final RegionManager regionManager;
    private final Region region;
    private final RegionPlayer regionPlayer;
    private final Player player;
    private final List<RegionGUIButton> buttons;
    private Inventory inventory;

    public RegionGUI(RegionPlugin plugin, RegionManager regionManager, Region region, RegionPlayer regionPlayer, Player player) {
        this.plugin = plugin;
        this.regionManager = regionManager;
        this.region = region;
        this.regionPlayer = regionPlayer;
        this.player = player;
        this.buttons = new ArrayList<>();
    }

    public void openInventory() {
        inventory = Bukkit.createInventory(this,
                getConfig().getInt(CFG_PATH + ".size"),
                StringUtil.colorize(String.format(getConfig().getString(CFG_PATH + ".title"), region.getName())));
        player.openInventory(inventory);
        for (String buttonKey : getConfig().getConfigurationSection(CFG_PATH + ".button-list").getKeys(false)) {
            buttons.add(RegionGUIButton.fromConfig(getConfig().getConfigurationSection(CFG_PATH + ".button-list." + buttonKey)));
        }
        for (RegionGUIButton button : buttons) {
            ItemStack itemStack = button.itemStack().clone();
            ItemMeta itemMeta = updateItemMeta(itemStack);
            itemStack.setItemMeta(itemMeta);
            button.slotList().forEach(integer -> inventory.setItem(integer, itemStack));
        }
    }

    public void handleClick(InventoryClickEvent e) {
        if (e.getClickedInventory() != this.inventory) return;
        if (e.getCurrentItem() == null) return;
        int slot = e.getSlot();
        e.setCancelled(true);
        Optional<RegionGUIButton> button = buttons.stream().filter(guiButton -> guiButton.slotList().contains(slot)).findFirst();
        button.ifPresent(guiButton -> {
            player.closeInventory();
            if (guiButton.name().equals("rename")) {
                player.sendMessage(RegionLanguage.getMessage("region-rename-define"));
                ChatInputUtil.getChatInput(player, name -> {
                    if (regionManager.getPlayerRegionByName(regionPlayer, name).isPresent()) {
                        player.sendMessage(String.format(RegionLanguage.getMessage("region-name-warning"), name));
                        return;
                    }
                    region.setName(name);
                    player.sendMessage(String.format(RegionLanguage.getMessage("region-rename"), name));
                });
            }
            if (guiButton.name().equals("whitelist-add")) {
                player.sendMessage(RegionLanguage.getMessage("region-add-define"));
                ChatInputUtil.getChatInput(player, name -> {
                    if (name.equalsIgnoreCase(player.getName())) {
                        player.sendMessage(RegionLanguage.getMessage("region-add-remove-warning"));
                        return;
                    }
                    Player target = Bukkit.getPlayer(name);
                    if (target == null) {
                        player.sendMessage(String.format(RegionLanguage.getMessage("target-not-found"), name));
                        return;
                    }
                    if (region.addMember(target.getUniqueId())) {
                        player.sendMessage(String.format(RegionLanguage.getMessage("region-add"), name));
                        return;
                    }
                    player.sendMessage(String.format(RegionLanguage.getMessage("region-add-warning"), name));
                });
            }
            if (guiButton.name().equals("whitelist-remove")) {
                player.sendMessage(RegionLanguage.getMessage("region-remove-define"));
                ChatInputUtil.getChatInput(player, name -> {
                    if (name.equalsIgnoreCase(player.getName())) {
                        player.sendMessage(RegionLanguage.getMessage("region-add-remove-warning"));
                        return;
                    }
                    Player target = Bukkit.getPlayer(name);
                    if (target == null) {
                        player.sendMessage(String.format(RegionLanguage.getMessage("target-not-found"), name));
                        return;
                    }
                    if (region.removeMember(target.getUniqueId())) {
                        player.sendMessage(String.format(RegionLanguage.getMessage("region-remove"), name));
                        return;
                    }
                    player.sendMessage(String.format(RegionLanguage.getMessage("region-remove-warning"), name));
                });
            }
            if (guiButton.name().equals("redefine-cuboid")) {
                if (region.getCuboid().equals(regionPlayer.getSelectionCuboid())) {
                    player.sendMessage(RegionLanguage.getMessage("region-redefine-same"));
                    return;
                }
                Region intersectionRegion = regionManager.checkIntersection(regionPlayer.getSelectionCuboid());
                if (intersectionRegion != null) {
                    player.sendMessage(String.format(RegionLanguage.getMessage("region-intersection"), intersectionRegion.getName()));
                    for (Location cuboidCorner : intersectionRegion.getCuboid().getCuboidCorners()) {
                        player.sendBlockChange(cuboidCorner, Material.GOLD_BLOCK.createBlockData());
                    }
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        for (Location cuboidCorner : intersectionRegion.getCuboid().getCuboidCorners()) {
                            cuboidCorner.getBlock().getState().update();
                        }
                    }, 20 * 10L);
                    return;
                }
                region.setCuboid(regionPlayer.getSelectionCuboid());
                player.sendMessage(RegionLanguage.getMessage("region-redefine"));
            }
            regionManager.saveRegion(region);
        });
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
        String members = String.join(",", region.getMembers().stream().map(uuid -> Bukkit.getOfflinePlayer(uuid).getName()).toList());
        if (itemMeta.hasDisplayName()) {
            itemMeta.setDisplayName(itemMeta.getDisplayName().replace("%name%", region.getName()).replace("%members%", members));
        }
        if (itemMeta.hasLore()) {
            List<String> newLore = new ArrayList<>();
            for (String s : itemMeta.getLore()) {
                newLore.add(s.replace("%name%", region.getName()).replace("%members%", members));
            }
            itemMeta.setLore(newLore);
        }
        return itemMeta;
    }
}
