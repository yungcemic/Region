package me.alpertepinar.region.player;

import me.alpertepinar.region.region.RegionCuboid;
import me.alpertepinar.region.util.Pair;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class RegionPlayer {

    private final UUID uuid;
    private final Set<UUID> regions;
    private final Pair<Location, Location> wandSelection;

    public RegionPlayer(UUID uuid, Set<UUID> regions) {
        this.uuid = uuid;
        this.regions = regions;
        this.wandSelection = new Pair<>();
    }

    public RegionPlayer(UUID uuid) {
        this.uuid = uuid;
        this.regions = new HashSet<>();
        this.wandSelection = new Pair<>();
    }

    public UUID getUuid() {
        return uuid;
    }

    public Set<UUID> getRegions() {
        return regions;
    }

    public Pair<Location, Location> getWandSelection() {
        return wandSelection;
    }

    public RegionCuboid getSelectionCuboid() {
        if (!wandSelection.isPresent()) return null;
        if (!wandSelection.getKey().getWorld().equals(wandSelection.getValue().getWorld())) return null;
        return new RegionCuboid(wandSelection.getKey(), wandSelection.getValue());
    }
}
