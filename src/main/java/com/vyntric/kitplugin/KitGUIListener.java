package com.vyntric.kitplugin;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KitGUIListener implements Listener {

    private final KitPlugin plugin;

    public KitGUIListener(KitPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        UUID uuid = player.getUniqueId();

        String kitName = plugin.getOpenGuis().get(uuid);
        if (kitName == null) {
            return; // eita amader kit-editor GUI na
        }

        List<ItemStack> items = new ArrayList<>();
        for (ItemStack item : event.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                items.add(item);
            }
        }

        FileConfiguration kits = plugin.getKitsConfig();
        kits.set("kits." + kitName, items);
        plugin.saveKitsConfig();

        plugin.getOpenGuis().remove(uuid);
        player.sendMessage("§aKit §e" + kitName + " §aautomatic save hoye geche! (" + items.size() + " ta item)");
    }
}
