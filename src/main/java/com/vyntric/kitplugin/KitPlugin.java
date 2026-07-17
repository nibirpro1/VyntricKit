package com.vyntric.kitplugin;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KitPlugin extends JavaPlugin {

    private File kitsFile;
    private FileConfiguration kitsConfig;

    // Track which player has which kit-editor GUI open (playerUUID -> kitName)
    private final Map<UUID, String> openGuis = new HashMap<>();

    @Override
    public void onEnable() {
        setupKitsFile();
        createDefaultKitIfMissing();

        KitCommand kitCommand = new KitCommand(this);
        getCommand("kit").setExecutor(kitCommand);
        getCommand("kit").setTabCompleter(kitCommand);
        getServer().getPluginManager().registerEvents(new KitGUIListener(this), this);

        getLogger().info("VyntricKit chalu hoyeche!");
    }

    @Override
    public void onDisable() {
        saveKitsConfig();
    }

    private void setupKitsFile() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        kitsFile = new File(getDataFolder(), "kits.yml");
        if (!kitsFile.exists()) {
            try {
                kitsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
    }

    private void createDefaultKitIfMissing() {
        if (!kitsConfig.contains("kits.VyntricUhc")) {
            List<ItemStack> items = new ArrayList<>();
            items.add(new ItemStack(Material.BOOKSHELF, 64));
            items.add(new ItemStack(Material.APPLE, 128));
            items.add(new ItemStack(Material.ANVIL, 64));
            items.add(new ItemStack(Material.ENCHANTING_TABLE, 64));
            items.add(new ItemStack(Material.GRINDSTONE, 16));

            kitsConfig.set("kits.VyntricUhc", items);
            saveKitsConfig();
            getLogger().info("Default kit 'VyntricUhc' toiri kora hoyeche.");
        }
    }

    public FileConfiguration getKitsConfig() {
        return kitsConfig;
    }

    public void saveKitsConfig() {
        try {
            kitsConfig.save(kitsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<UUID, String> getOpenGuis() {
        return openGuis;
    }
}
