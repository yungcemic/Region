package me.alpertepinar.region.region;

import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class Region {

    private final UUID uuid;
    private final UUID ownerId;
    private final World world;
    private final Set<UUID> members;
    private RegionCuboid cuboid;
    private String name;

    public Region(UUID uuid, UUID ownerId, World world, Set<UUID> members, RegionCuboid cuboid, String name) {
        this.uuid = uuid;
        this.ownerId = ownerId;
        this.world = world;
        this.members = members;
        this.cuboid = cuboid;
        this.name = name;
    }

    public Region(UUID uuid, UUID ownerId, World world, RegionCuboid cuboid, String name) {
        this.uuid = uuid;
        this.ownerId = ownerId;
        this.world = world;
        this.members = new HashSet<>();
        this.cuboid = cuboid;
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public World getWorld() {
        return world;
    }

    public boolean addMember(UUID uniqueId) {
        return members.add(uniqueId);
    }

    public boolean removeMember(UUID uniqueId) {
        return members.remove(uniqueId);
    }

    public Set<UUID> getMembers() {
        return new HashSet<>(members);
    }

    public boolean canAccess(UUID uniqueId) {
        return ownerId.equals(uniqueId) || members.contains(uniqueId);
    }

    public RegionCuboid getCuboid() {
        return cuboid;
    }

    public void setCuboid(RegionCuboid cuboid) {
        this.cuboid = cuboid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}