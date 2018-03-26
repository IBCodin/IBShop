package io.github.ibcodin.ibshop;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class BlackList {

    private final IBShop plugin;

    private final File saveFile;
    private final List<String> BlackListStrings = new ArrayList<>();
    private final transient List<Material> blackListItems = new ArrayList<>();
    private FileConfiguration config;

    public BlackList(IBShop plugin) {
        this.plugin = plugin;
        saveFile = new File(plugin.getDataFolder(), "blacklist.yml");

        reload();
    }

    public boolean canSellItem(ItemStack test) {
        Material material = test.getType();

//        log(Level.INFO, "Checking BlackList for Material " + material.name());

        if (!material.isItem()) {
//            log(Level.INFO, "   Not an Item");
            return false;
        }

        if (material.getMaxStackSize() < 16) {
//            log(Level.INFO, "   Not stackable (" + material.getMaxStackSize() + ")");
            return false;
        }

        if (test.getItemMeta().hasDisplayName()) {
//            log(Level.INFO, "   Has DisplayName");
            return false;
        }

        if (test.getItemMeta().hasEnchants()) {
//            log(Level.INFO, "   Has Enchants");
            return false;
        }

        if (test.getItemMeta().hasLore()) {
//            log(Level.INFO, "   Has Lore");
            return false;
        }


        if (blackListItems.contains(material)) {
//            log(Level.INFO, "   Is BlackListed");
            return false;
        }

        return true;
    }

    public void reload() {
        log(Level.INFO, "Loading Blacklist");

        // write the default whitelist if it does not exist
        if (!saveFile.exists()) plugin.saveResource("blacklist.yml", false);

        if (!saveFile.exists()) {
            log(Level.SEVERE, "Could not find blacklist.yml");
            return;
        }

        BlackListStrings.clear();
        blackListItems.clear();

        config = YamlConfiguration.loadConfiguration(saveFile);
        BlackListStrings.addAll(config.getStringList("BlackList"));

        for (String item : BlackListStrings) {
            final Material material = Material.getMaterial(item);
            if (material == null) {
                log(Level.WARNING, "BlackList " + item + " not Material");
                continue;
            }
            blackListItems.add(material);
        }
    }

    private void log(Level level, String message) {
        plugin.log(level, message);
    }
}
