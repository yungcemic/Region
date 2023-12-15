package me.alpertepinar.region.listener;

import me.alpertepinar.region.RegionPlugin;
import me.alpertepinar.region.language.RegionLanguage;
import me.alpertepinar.region.player.RegionPlayer;
import me.alpertepinar.region.player.RegionPlayerManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public final class SelectionWandListener implements Listener {

    private final RegionPlugin plugin;
    private final RegionPlayerManager playerManager;

    public SelectionWandListener(RegionPlugin plugin, RegionPlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();
        if (e.getClickedBlock() == null) return;
        if (item != null && item.isSimilar(plugin.getWandItemstack())) {
            e.setCancelled(true);
            Location location = e.getClickedBlock().getLocation();
            RegionPlayer regionPlayer = playerManager.getPlayer(p.getUniqueId());
            switch (e.getAction()) {
                case LEFT_CLICK_BLOCK -> regionPlayer.getWandSelection().setKey(location);
                case RIGHT_CLICK_BLOCK -> regionPlayer.getWandSelection().setValue(location);
            }
            p.sendMessage(String.format(RegionLanguage.getMessage("wand-selection"), e.getAction().equals(Action.LEFT_CLICK_BLOCK) ? "1" : "2"));
        }
    }
}