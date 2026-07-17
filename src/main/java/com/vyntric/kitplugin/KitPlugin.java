package com.vyntric.kitplugin;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        if (!kitExists("VyntricUhc")) {
            List<ItemStack> items = new ArrayList<>();
            items.add(new ItemStack(Material.BOOKSHELF, 64));
            items.add(new ItemStack(Material.APPLE, 128));
            items.add(new ItemStack(Material.ANVIL, 64));
            items.add(new ItemStack(Material.ENCHANTING_TABLE, 64));
            items.add(new ItemStack(Material.GRINDSTONE, 16));

            saveKitItems("VyntricUhc", items);
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

    /**
     * Ekta kit-er item list save kore. ItemStack shorashori YamlConfiguration-e
     * set na kore, age Map-e serialize kore neya hoy - noile notun Paper version-e
     * SnakeYAML representer crash kore (StackOverflow / infinite recursion).
     */
    public void saveKitItems(String kitName, List<ItemStack> items) {
        List<Map<String, Object>> serialized = new ArrayList<>();
        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) {
                serialized.add(item.serialize());
            }
        }
        kitsConfig.set("kits." + kitName, serialized);
        saveKitsConfig();
    }

    /**
     * Ekta kit-er item list load kore. kits.yml-e Map hisebe save kora item gula
     * abar ItemStack-e deserialize kore fere pathay.
     */
    @SuppressWarnings("unchecked")
    public List<ItemStack> getKitItems(String kitName) {
        List<ItemStack> items = new ArrayList<>();
        List<?> rawList = kitsConfig.getList("kits." + kitName);
        if (rawList == null) {
            return items;
        }

        for (Object obj : rawList) {
            try {
                if (obj instanceof ItemStack) {
                    // Purono format-e direct ItemStack save kora thakle (backward compatibility)
                    items.add((ItemStack) obj);
                } else if (obj instanceof Map) {
                    items.add(ItemStack.deserialize((Map<String, Object>) obj));
                }
            } catch (Exception e) {
                getLogger().warning("Kit '" + kitName + "' er ekta item load korte parlam na: " + e.getMessage());
            }
        }
        return items;
    }

    public boolean kitExists(String kitName) {
        return kitsConfig.contains("kits." + kitName);
    }

    public void removeKit(String kitName) {
        kitsConfig.set("kits." + kitName, null);
        saveKitsConfig();
    }

    public Set<String> getKitNames() {
        ConfigurationSection section = kitsConfig.getConfigurationSection("kits");
        if (section == null) {
            return new HashSet<>();
        }
        return section.getKeys(false);
    }
}
