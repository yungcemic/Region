package me.alpertepinar.region.database;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import me.alpertepinar.region.player.RegionPlayer;
import me.alpertepinar.region.region.Region;
import me.alpertepinar.region.region.RegionCuboid;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;


import java.util.*;
import java.util.stream.Collectors;

public final class RegionMongoDatabase implements RegionDatabase {

    private MongoClient client;
    private MongoCollection<Document> regionCollection;
    private MongoCollection<Document> playerCollection;

    public void connect(String uri) {
        this.client = MongoClients.create(uri);
        this.playerCollection = client.getDatabase("region").getCollection("players");
        this.regionCollection = client.getDatabase("region").getCollection("regions");
    }

    @Override
    public Optional<RegionPlayer> findPlayerByUniqueId(UUID uniqueId) {
        Document document = playerCollection.find(Filters.eq("uniqueId", uniqueId.toString())).first();
        if (document != null) {
            Set<UUID> regions = document.getList("regions", String.class).stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toSet());
            return Optional.of(new RegionPlayer(uniqueId, regions));
        }
        return Optional.empty();
    }

    @Override
    public void savePlayer(RegionPlayer player) {
        if (player.getRegions().isEmpty()) return;
        Document document = new Document().append("uniqueId", player.getUuid().toString());
        document.append("regions", player.getRegions().stream().map(UUID::toString).toList());
        playerCollection.replaceOne(Filters.eq("uniqueId", player.getUuid().toString()), document, new ReplaceOptions().upsert(true));
    }

    @Override
    public Optional<Region> findRegionByUniqueId(UUID uniqueId) {
        Document document = regionCollection.find(Filters.eq("uniqueId", uniqueId.toString())).first();
        if (document != null) {
            return Optional.of(deserializeRegion(document));
        }
        return Optional.empty();
    }

    @Override
    public List<Region> findRegionByChunk(List<Chunk> chunks) {
        String worldName = chunks.get(0).getWorld().getName();
        List<String> encodedChunks = chunks.stream().map(this::encodeChunk).toList();
        return regionCollection.find(Filters.and(
                Filters.eq("world", worldName),
                Filters.in("chunks", encodedChunks)))
                .into(new ArrayList<>()).stream()
                .map(this::deserializeRegion).toList();
    }

    @Override
    public void saveRegion(Region region) {
        Document document = new Document("uniqueId", region.getUuid().toString())
                .append("ownerId", region.getOwnerId().toString())
                .append("name", region.getName())
                .append("world", region.getWorld().getName())
                .append("members", region.getMembers().stream().map(UUID::toString).toList())
                .append("cuboid", new Document("xMin", region.getCuboid().getxMin())
                        .append("xMax", region.getCuboid().getxMax())
                        .append("yMin", region.getCuboid().getyMin())
                        .append("yMax", region.getCuboid().getyMax())
                        .append("zMin", region.getCuboid().getzMin())
                        .append("zMax", region.getCuboid().getzMax())
                ).append("chunks", region.getCuboid().getChunksInCuboid().stream().map(this::encodeChunk).toList());
        regionCollection.replaceOne(Filters.eq("uniqueId", region.getUuid().toString()), document, new ReplaceOptions().upsert(true));
    }

    private Region deserializeRegion(Document document) {
        UUID uuid = UUID.fromString(document.getString("uniqueId"));
        UUID ownerId = UUID.fromString(document.getString("ownerId"));
        String name = document.getString("name");
        String worldName = document.getString("world");
        Set<UUID> members = document.getList("members", String.class)
                .stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
        Document cuboidDoc = document.get("cuboid", Document.class);
        int xMin = cuboidDoc.getInteger("xMin");
        int xMax = cuboidDoc.getInteger("xMax");
        int yMin = cuboidDoc.getInteger("yMin");
        int yMax = cuboidDoc.getInteger("yMax");
        int zMin = cuboidDoc.getInteger("zMin");
        int zMax = cuboidDoc.getInteger("zMax");
        World world = Bukkit.getWorld(worldName);
        RegionCuboid cuboid = new RegionCuboid(xMin, xMax, yMin, yMax, zMin, zMax, world);
        return new Region(uuid, ownerId, world, members, cuboid, name);
    }

    private String encodeChunk(Chunk chunk) {
        return chunk.getX() + "_" + chunk.getZ();
    }
}
