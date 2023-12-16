package me.alpertepinar.region;

import me.alpertepinar.region.command.RegionCommand;
import me.alpertepinar.region.database.RegionMongoDatabase;
import me.alpertepinar.region.language.RegionLanguage;
import me.alpertepinar.region.listener.GUIListener;
import me.alpertepinar.region.listener.PlayerListener;
import me.alpertepinar.region.listener.RegionAccessListener;
import me.alpertepinar.region.listener.SelectionWandListener;
import me.alpertepinar.region.player.RegionPlayerManager;
import me.alpertepinar.region.region.RegionManager;
import me.alpertepinar.region.task.ExpiredRegionCleanupTask;
import me.alpertepinar.region.util.ItemStackUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class RegionPlugin extends JavaPlugin {

    private ItemStack wandItemstack;
    private RegionManager regionManager;
    private RegionPlayerManager playerManager;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        RegionLanguage.loadMessages(getConfig().getConfigurationSection("messages"));
        wandItemstack = ItemStackUtil.getItemStackFromConfig(getConfig().getConfigurationSection("settings.wand-item"));
        RegionMongoDatabase database = new RegionMongoDatabase();
        database.connect(getConfig().getString("settings.database.database"), getConfig().getString("settings.database.uri"));
        playerManager = new RegionPlayerManager(database);
        regionManager = new RegionManager(database);
        getCommand("region").setExecutor(new RegionCommand(this, playerManager, regionManager));
        getServer().getPluginManager().registerEvents(new RegionAccessListener(regionManager), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(playerManager, regionManager), this);
        getServer().getPluginManager().registerEvents(new SelectionWandListener(this, playerManager), this);
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        getServer().getScheduler().runTaskTimerAsynchronously(this, new ExpiredRegionCleanupTask(regionManager), 6000L, 6000L);
    }

    @Override
    public void onDisable() {
        regionManager.getAllRegionsFromCache().forEach(region -> regionManager.saveRegion(region));
        regionManager.clearCache();
        playerManager.getAllPlayersInCache().forEach(regionPlayer -> playerManager.savePlayer(regionPlayer));
        playerManager.clearCache();
    }

    public ItemStack getWandItemstack() {
        return wandItemstack;
    }
}
