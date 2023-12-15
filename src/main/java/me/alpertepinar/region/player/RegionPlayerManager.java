package me.alpertepinar.region.player;

import me.alpertepinar.region.database.RegionDatabase;

import java.util.*;

public final class RegionPlayerManager {

    private final RegionDatabase regionDatabase;
    private final Map<UUID, RegionPlayer> playerMap;

    public RegionPlayerManager(RegionDatabase regionDatabase) {
        this.regionDatabase = regionDatabase;
        this.playerMap = new HashMap<>();
    }

    public boolean addPlayer(RegionPlayer player) {
        if (getPlayer(player.getUuid()) != null) return false;
        playerMap.put(player.getUuid(), player);
        return true;
    }

    public RegionPlayer getPlayer(UUID uniqueId) {
        if (playerMap.get(uniqueId) == null) {
            regionDatabase.findPlayerByUniqueId(uniqueId)
                    .ifPresent(regionPlayer -> playerMap.put(uniqueId, regionPlayer));
        }
        return playerMap.get(uniqueId);
    }

    public void savePlayer(RegionPlayer player) {
        regionDatabase.savePlayer(player);
    }

    public void removePlayerFromCache(UUID uniqueId) {
        playerMap.remove(uniqueId);
    }

    public List<RegionPlayer> getAllPlayersInCache() {
        return new ArrayList<>(playerMap.values());
    }

    public void clearCache() {
        playerMap.clear();
    }
}
