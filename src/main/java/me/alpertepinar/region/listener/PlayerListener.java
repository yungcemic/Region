package me.alpertepinar.region.listener;

import me.alpertepinar.region.player.RegionPlayer;
import me.alpertepinar.region.player.RegionPlayerManager;
import me.alpertepinar.region.region.RegionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public final class PlayerListener implements Listener {

    private final RegionPlayerManager playerManager;
    private final RegionManager regionManager;

    public PlayerListener(RegionPlayerManager playerManager, RegionManager regionManager) {
        this.playerManager = playerManager;
        this.regionManager = regionManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        UUID uniqueId = e.getPlayer().getUniqueId();
        RegionPlayer regionPlayer = playerManager.getPlayer(uniqueId);
        if (regionPlayer == null) {
            playerManager.addPlayer(new RegionPlayer(uniqueId));
            return;
        }
        regionManager.loadPlayerAllRegions(regionPlayer);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID uniqueId = e.getPlayer().getUniqueId();
        RegionPlayer regionPlayer = playerManager.getPlayer(uniqueId);
        if (regionPlayer != null) {
            playerManager.savePlayer(regionPlayer);
            regionManager.getPlayerAllRegions(regionPlayer).forEach(region -> {
                regionManager.removeRegion(region);
                regionManager.addCacheRegion(region);
                regionManager.saveRegion(region);
            });
        }
        playerManager.removePlayerFromCache(uniqueId);
    }

}
