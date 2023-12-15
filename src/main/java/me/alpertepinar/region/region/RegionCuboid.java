package me.alpertepinar.region.region;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RegionCuboid {

    private final int xMin;
    private final int xMax;
    private final int yMin;
    private final int yMax;
    private final int zMin;
    private final int zMax;
    private final World world;

    public RegionCuboid(int xMin, int xMax, int yMin, int yMax, int zMin, int zMax, World world) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.zMin = zMin;
        this.zMax = zMax;
        this.world = world;
    }

    public RegionCuboid(final Location point1, final Location point2) {
        this.xMin = Math.min(point1.getBlockX(), point2.getBlockX());
        this.xMax = Math.max(point1.getBlockX(), point2.getBlockX());
        this.yMin = Math.min(point1.getBlockY(), point2.getBlockY());
        this.yMax = Math.max(point1.getBlockY(), point2.getBlockY());
        this.zMin = Math.min(point1.getBlockZ(), point2.getBlockZ());
        this.zMax = Math.max(point1.getBlockZ(), point2.getBlockZ());
        this.world = point1.getWorld();
    }

    public boolean isIn(final Location loc) {
        return loc.getWorld() == this.world && loc.getBlockX() >= this.xMin && loc.getBlockX() <= this.xMax && loc.getBlockY() >= this.yMin && loc.getBlockY() <= this.yMax && loc
                .getBlockZ() >= this.zMin && loc.getBlockZ() <= this.zMax;
    }

    public List<Chunk> getChunksInCuboid() {
        List<Chunk> chunks = new ArrayList<>();
        int minChunkX = xMin >> 4;
        int maxChunkX = xMax >> 4;
        int minChunkZ = zMin >> 4;
        int maxChunkZ = zMax >> 4;
        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                Chunk chunk = world.getChunkAt(x, z);
                chunks.add(chunk);
            }
        }
        return chunks;
    }

    public boolean intersects(RegionCuboid other) {
        return this.getWorld() == other.getWorld() &&
                this.xMin <= other.xMax && this.xMax >= other.xMin &&
                this.yMin <= other.yMax && this.yMax >= other.yMin &&
                this.zMin <= other.zMax && this.zMax >= other.zMin;
    }

    public List<Location> getCuboidCorners() {
        List<Location> corners = new ArrayList<>();
        World w = this.getWorld();
        corners.add(new Location(w, this.xMin, this.yMin, this.zMin));
        corners.add(new Location(w, this.xMin, this.yMin, this.zMax));
        corners.add(new Location(w, this.xMin, this.yMax, this.zMin));
        corners.add(new Location(w, this.xMin, this.yMax, this.zMax));
        corners.add(new Location(w, this.xMax, this.yMin, this.zMin));
        corners.add(new Location(w, this.xMax, this.yMin, this.zMax));
        corners.add(new Location(w, this.xMax, this.yMax, this.zMin));
        corners.add(new Location(w, this.xMax, this.yMax, this.zMax));
        return corners;
    }

    public int getxMin() {
        return xMin;
    }

    public int getxMax() {
        return xMax;
    }

    public int getyMin() {
        return yMin;
    }

    public int getyMax() {
        return yMax;
    }

    public int getzMin() {
        return zMin;
    }

    public int getzMax() {
        return zMax;
    }

    public World getWorld() {
        return world;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegionCuboid cuboid = (RegionCuboid) o;
        return xMin == cuboid.xMin && xMax == cuboid.xMax && yMin == cuboid.yMin && yMax == cuboid.yMax && zMin == cuboid.zMin && zMax == cuboid.zMax && Objects.equals(world, cuboid.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(xMin, xMax, yMin, yMax, zMin, zMax, world);
    }
}