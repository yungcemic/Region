package me.alpertepinar.region.listener;

import me.alpertepinar.region.region.Region;
import me.alpertepinar.region.region.RegionManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public final class RegionAccessListener implements Listener {

    private final RegionManager regionManager;

    public RegionAccessListener(RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        canAccess(e, e.getPlayer(), e.getBlock().getLocation());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        canAccess(e, e.getPlayer(), e.getBlock().getLocation());
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent e) {
        canAccess(e, e.getPlayer(), e.getItem().getLocation());
    }

    private void canAccess(Cancellable event, Player player, Location location) {
        if (player.hasPermission("region.bypass")) return;
        Region region = regionManager.getRegionByLocation(location);
        if (region == null) return;
        if (!region.canAccess(player.getUniqueId())) event.setCancelled(true);
    }
}
