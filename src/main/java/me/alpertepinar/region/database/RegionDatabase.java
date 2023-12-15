package me.alpertepinar.region.database;

import me.alpertepinar.region.player.RegionPlayer;
import me.alpertepinar.region.region.Region;
import org.bukkit.Chunk;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegionDatabase {

    Optional<RegionPlayer> findPlayerByUniqueId(UUID uniqueId);
    void savePlayer(RegionPlayer player);
    Optional<Region> findRegionByUniqueId(UUID uniqueId);
    List<Region> findRegionByChunk(List<Chunk> chunks);
    void saveRegion(Region region);

}