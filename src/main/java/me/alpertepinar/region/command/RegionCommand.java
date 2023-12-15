package me.alpertepinar.region.command;

import me.alpertepinar.region.RegionPlugin;
import me.alpertepinar.region.gui.RegionListGUI;
import me.alpertepinar.region.language.RegionLanguage;
import me.alpertepinar.region.player.RegionPlayer;
import me.alpertepinar.region.player.RegionPlayerManager;
import me.alpertepinar.region.region.Region;
import me.alpertepinar.region.region.RegionCuboid;
import me.alpertepinar.region.region.RegionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class RegionCommand implements CommandExecutor {

    private final RegionPlugin plugin;
    private final RegionPlayerManager playerManager;
    private final RegionManager regionManager;

    private final List<String> subCommands = List.of("help", "create", "wand", "add", "remove", "whitelist");

    public RegionCommand(RegionPlugin plugin, RegionPlayerManager playerManager, RegionManager regionManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.regionManager = regionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player p) {
            RegionPlayer regionPlayer = playerManager.getPlayer(p.getUniqueId());
            if (args.length == 0) {
                if (!p.hasPermission("region.menu")) {
                    sendNoPermissionMessage(p);                    return true;
                }
                if (regionPlayer.getRegions().isEmpty()) {
                    p.sendMessage(RegionLanguage.getMessage("gui-has-no-region"));
                    return true;
                }
                new RegionListGUI(plugin, regionManager, regionPlayer, regionManager.getPlayerAllRegions(regionPlayer), p).openInventory();
                return true;
            }
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("wand")) {
                    p.getInventory().addItem(plugin.getWandItemstack());
                    p.sendMessage(RegionLanguage.getMessage("wand-receive"));
                    return true;
                }
                String regionName = args[0];
                if (subCommands.contains(regionName)) {
                    sendHelpMessage(p);
                    return true;
                }
                if (!p.hasPermission("region.whitelist")) {
                    sendNoPermissionMessage(p);                    return true;
                }
                p.sendMessage("looking for region named");
                return true;
            }
            if (args.length == 2) {
                String regionName = args[1];
                if (args[0].equalsIgnoreCase("create")) {
                    if (!p.hasPermission("region.create")) {
                        sendNoPermissionMessage(p);                        return true;
                    }
                    if (!regionPlayer.getWandSelection().isPresent()) {
                        p.sendMessage(RegionLanguage.getMessage("wand-selection-warning"));
                        return true;
                    }
                    Region intersectionRegion = regionManager.checkIntersection(regionPlayer.getSelectionCuboid());
                    if (intersectionRegion != null) {
                        p.sendMessage(String.format(RegionLanguage.getMessage("region-intersection"), intersectionRegion.getName()));
                        for (Location cuboidCorner : intersectionRegion.getCuboid().getCuboidCorners()) {
                            p.sendBlockChange(cuboidCorner, Material.GOLD_BLOCK.createBlockData());
                        }
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            for (Location cuboidCorner : intersectionRegion.getCuboid().getCuboidCorners()) {
                                cuboidCorner.getBlock().getState().update();
                            }
                        }, 20 * 10L);
                        return true;
                    }
                    if (regionManager.getPlayerRegionByName(regionPlayer, regionName).isEmpty()) {
                        RegionCuboid cuboid = regionPlayer.getSelectionCuboid();
                        Region region = new Region(UUID.randomUUID(), p.getUniqueId(), cuboid.getWorld(), cuboid, regionName);
                        regionPlayer.getRegions().add(region.getUuid());
                        regionManager.addRegion(region);
                        p.sendMessage(String.format(RegionLanguage.getMessage("region-create"), regionName));
                        return true;
                    }
                    p.sendMessage(String.format(RegionLanguage.getMessage("region-name-warning"), regionName));
                    return true;
                }
                if (args[0].equalsIgnoreCase("whitelist")) {
                    if (!p.hasPermission("region.whitelist")) {
                        sendNoPermissionMessage(p);                        return true;
                    }
                    regionManager.getPlayerRegionByName(regionPlayer, regionName).ifPresentOrElse(region -> {
                        String members = String.join(",", region.getMembers().stream().map(uuid -> Bukkit.getOfflinePlayer(uuid).getName()).toList());
                        p.sendMessage(String.format(RegionLanguage.getMessage("region-whitelist"), members));
                    }, () -> p.sendMessage(String.format(RegionLanguage.getMessage("region-not-found"), regionName)));
                    return true;
                }
                sendHelpMessage(p);
                return true;
            }
            if (args.length == 3) {
                String regionName = args[1];
                String userName = args[2];
                if (!p.hasPermission("region." + args[0].toLowerCase())) {
                    sendNoPermissionMessage(p);                    return true;
                }
                if (userName.equalsIgnoreCase(p.getName())) {
                    p.sendMessage(RegionLanguage.getMessage("region-add-remove-warning"));
                    return true;
                }
                Player target = Bukkit.getPlayer(userName);
                if (target == null) {
                    p.sendMessage(String.format(RegionLanguage.getMessage("target-not-found"), userName));
                    return true;
                }
                Optional<Region> region = regionManager.getPlayerRegionByName(regionPlayer, regionName);
                if (region.isEmpty()) {
                    p.sendMessage(String.format(RegionLanguage.getMessage("region-not-found"), regionName));
                    return true;
                }
                if (args[0].equalsIgnoreCase("add")) {
                    if (region.get().addMember(target.getUniqueId())) {
                        p.sendMessage(String.format(RegionLanguage.getMessage("region-add"), userName));
                        return true;
                    }
                    p.sendMessage(String.format(RegionLanguage.getMessage("region-add-warning"), userName));
                    return true;
                }
                if (args[0].equalsIgnoreCase("remove")) {
                    if (region.get().removeMember(target.getUniqueId())) {
                        p.sendMessage(String.format(RegionLanguage.getMessage("region-remove"), userName));
                        return true;
                    }
                    p.sendMessage(String.format(RegionLanguage.getMessage("region-remove-warning"), userName));
                    return true;
                }
                sendHelpMessage(p);
                return true;
            }
            sendHelpMessage(p);
            return true;
        }
        return false;
    }

    private void sendHelpMessage(Player player) {
        for (String s : RegionLanguage.getHelpMessage()) {
            player.sendMessage(s);
        }
    }
    
    private void sendNoPermissionMessage(Player player) {
        player.sendMessage(RegionLanguage.getMessage("no-permission"));
    }

}
