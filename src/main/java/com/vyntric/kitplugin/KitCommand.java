package com.vyntric.kitplugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class KitCommand implements CommandExecutor, TabCompleter {

    private final KitPlugin plugin;

    public KitCommand(KitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Sudhu Op ra eita use korte parbe
        if (!sender.isOp()) {
            sender.sendMessage("§cTomar eita use korar permission nai (Op lagbe).");
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "set":
                handleSet(sender, args);
                return true;
            case "add":
                handleAdd(sender, args);
                return true;
            case "remove":
                handleRemove(sender, args);
                return true;
            default:
                sendUsage(sender);
                return true;
        }
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§eUsage:");
        sender.sendMessage("§7/kit set <player> <kitname>  §8- player ke kit dao");
        sender.sendMessage("§7/kit add <kitname>  §8- notun/existing kit editor GUI khulo");
        sender.sendMessage("§7/kit remove <kitname>  §8- kit delete koro");
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§eUsage: /kit set <player> <kitname>");
            return;
        }

        String targetName = args[1];
        String kitName = args[2];

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            sender.sendMessage("§cEi naam er player online nai: " + targetName);
            return;
        }

        FileConfiguration kits = plugin.getKitsConfig();
        String path = "kits." + kitName;

        if (!kits.contains(path)) {
            sender.sendMessage("§cEmon kono kit nai: §e" + kitName);
            return;
        }

        List<?> rawItems = kits.getList(path);
        if (rawItems == null || rawItems.isEmpty()) {
            sender.sendMessage("§cKit ta khali ache, age item add koro (/kit add " + kitName + ")");
            return;
        }

        int given = 0;
        for (Object obj : rawItems) {
            if (obj instanceof ItemStack) {
                ItemStack item = ((ItemStack) obj).clone();
                if (item.getType() != Material.AIR) {
                    target.getInventory().addItem(item);
                    given++;
                }
            }
        }

        target.sendMessage("§aTumi §e" + kitName + " §akit peyecho! (" + given + " ta item)");
        sender.sendMessage("§a" + target.getName() + " ke §e" + kitName + " §akit dewa hoyeche.");
    }

    private void handleAdd(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§eUsage: /kit add <kitname>");
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEi command sudhu player theke run kora jabe.");
            return;
        }

        String kitName = args[1];
        Player player = (Player) sender;

        Inventory gui = Bukkit.createInventory(null, 54, "Kit Editor: " + kitName);

        // Age theke kit thakle, existing item gula GUI te dekhabe (edit korar jonno)
        FileConfiguration kits = plugin.getKitsConfig();
        String path = "kits." + kitName;
        if (kits.contains(path)) {
            List<?> rawItems = kits.getList(path);
            if (rawItems != null) {
                int slot = 0;
                for (Object obj : rawItems) {
                    if (obj instanceof ItemStack && slot < 54) {
                        gui.setItem(slot, (ItemStack) obj);
                        slot++;
                    }
                }
            }
        }

        plugin.getOpenGuis().put(player.getUniqueId(), kitName);
        player.openInventory(gui);
        player.sendMessage("§eKit editor (§b" + kitName + "§e) khule geche.");
        player.sendMessage("§7Jaja item rakhte chao boshao, tarpor inventory close (§7X§7) korle automatic save hoye jabe.");
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§eUsage: /kit remove <kitname>");
            return;
        }

        String kitName = args[1];
        FileConfiguration kits = plugin.getKitsConfig();
        String path = "kits." + kitName;

        if (!kits.contains(path)) {
            sender.sendMessage("§cEmon kono kit nai: §e" + kitName);
            return;
        }

        kits.set(path, null);
        plugin.saveKitsConfig();

        sender.sendMessage("§aKit §e" + kitName + " §adelete kore deya hoyeche.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.isOp()) {
            return new ArrayList<>();
        }

        List<String> options = new ArrayList<>();

        if (args.length == 1) {
            options.addAll(Arrays.asList("set", "add", "remove"));
            return filter(options, args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("set")) {
                // Online player der naam suggest korbe
                for (Player p : Bukkit.getOnlinePlayers()) {
                    options.add(p.getName());
                }
            } else if (sub.equals("add") || sub.equals("remove")) {
                // Existing kit-er naam suggest korbe
                options.addAll(getKitNames());
            }
            return filter(options, args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            // /kit set <player> <kitname> -> kitname suggest korbe
            options.addAll(getKitNames());
            return filter(options, args[2]);
        }

        return options;
    }

    private List<String> getKitNames() {
        ConfigurationSection section = plugin.getKitsConfig().getConfigurationSection("kits");
        if (section == null) {
            return new ArrayList<>();
        }
        Set<String> keys = section.getKeys(false);
        return new ArrayList<>(keys);
    }

    private List<String> filter(List<String> options, String typed) {
        String lower = typed.toLowerCase();
        return options.stream()
                .filter(o -> o.toLowerCase().startsWith(lower))
                .collect(Collectors.toList());
    }
}
