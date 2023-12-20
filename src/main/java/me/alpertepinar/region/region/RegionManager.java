package me.alpertepinar.region.region;

import me.alpertepinar.region.database.RegionDatabase;
import me.alpertepinar.region.player.RegionPlayer;
import me.alpertepinar.region.util.Pair;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.TimeUnit;

public final class RegionManager {

    private final RegionDatabase regionDatabase;
    private final Map<UUID, Region> onlineRegionMap;
    private final Map<UUID, Pair<Long, Region>> offlineRegionMap;

    public RegionManager(RegionDatabase regionDatabase) {
        this.regionDatabase = regionDatabase;
        this.onlineRegionMap = new HashMap<>();
        this.offlineRegionMap = new HashMap<>();
    }

    public void addRegion(Region region) {
        onlineRegionMap.put(region.getUuid(), region);
        saveRegion(region);
    }

    public void removeRegion(Region region) {
        onlineRegionMap.remove(region.getUuid());
    }

    public void saveRegion(Region region) {
        regionDatabase.saveRegion(region);
    }

    public void addCacheRegion(Region region) {
        offlineRegionMap.put(region.getUuid(), new Pair<>(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30L), region));
    }

    public boolean isCacheRegion(Region region) {
        return offlineRegionMap.containsKey(region.getUuid());
    }

    public Region checkIntersection(RegionCuboid cuboid) {
        for (Region region : getAllRegionsFromCache()) {
            if (region.getCuboid().intersects(cuboid)) {
                if (isCacheRegion(region)) addCacheRegion(region);
                return region;
            }
        }
        List<Region> databaseRegions = regionDatabase.findRegionByChunk(cuboid.getChunksInCuboid());
        for (Region region : databaseRegions) {
            if (region.getCuboid().intersects(cuboid)) {
                addCacheRegion(region);
                return region;
            }
        }
        return null;
    }

    public Region getRegionByLocation(Location location) {
        for (Region region : getAllRegionsFromCache()) {
            if (region.getCuboid().isIn(location)) {
                if (isCacheRegion(region)) addCacheRegion(region);
                return region;
            }
        }
        List<Region> databaseRegions = regionDatabase.findRegionByChunk(Collections.singletonList(location.getChunk()));
        for (Region region : databaseRegions) {
            if (region.getCuboid().isIn(location)) {
                addCacheRegion(region);
                return region;
            }
        }
        return null;
    }

    public Optional<Region> getPlayerRegionByName(RegionPlayer player, String name) {
        return player.getRegions().stream()
                .map(onlineRegionMap::get)
                .filter(Objects::nonNull)
                .filter(region -> region.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public void loadPlayerAllRegions(RegionPlayer player) {
        player.getRegions().forEach(uuid -> {
            regionDatabase.findRegionByUniqueId(uuid).ifPresent(this::addRegion);
            offlineRegionMap.remove(uuid);
        });
    }

    public List<Region> getPlayerAllRegions(RegionPlayer regionPlayer) {
        return getAllRegionsFromCache().stream().filter(region -> regionPlayer.getRegions().contains(region.getUuid())).toList();
    }

    public List<Region> getAllRegionsFromCache() {
        List<Region> regions = new ArrayList<>(onlineRegionMap.values());
        offlineRegionMap.values().forEach(pair -> regions.add(pair.getValue()));
        return regions;
    }

    public void removeExpiredCacheRegions() {
        offlineRegionMap.entrySet().removeIf(entry -> entry.getValue().getKey() <= System.currentTimeMillis());
    }

    public void clearCache() {
        onlineRegionMap.clear();
        offlineRegionMap.clear();
    }
}
