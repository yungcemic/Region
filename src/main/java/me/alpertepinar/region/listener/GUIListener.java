package me.alpertepinar.region.listener;

import me.alpertepinar.region.gui.RegionGUI;
import me.alpertepinar.region.gui.RegionListGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public final class GUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() instanceof RegionListGUI listGUI) {
            listGUI.handleClick(e);
        }
        if (e.getInventory().getHolder() instanceof RegionGUI regionGUI) {
            regionGUI.handleClick(e);
        }
    }
}
