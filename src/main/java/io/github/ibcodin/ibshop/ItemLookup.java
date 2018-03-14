package io.github.ibcodin.ibshop;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class ItemLookup {

    // Record for each material / variant
    // - Material
    // - Variant Info

    // Lookup
    // - First Name
    // - All Names

    // Class to return record from any of the name alternates (common names)
    // Class to return record from an ItemStack with matching data
    //     so we can retrieve the First Name
    // Class to create ItemStack from record
    // Class to find names that match but are not equal
    //     to help users find the actual name they want

    // Create ItemStack from 'common name'
    // Lookup FirstName from ItemStack

    private final IBShop plugin;

    private final transient File saveFile;

    private final transient Map<ItemData,String> PreferredNames = new HashMap<>();
    private final transient Map<String, ItemData> Aliases = new HashMap<>();
    private final transient Map<String, String> nbtData = new HashMap<>();

    private final transient Pattern csvSplit = Pattern.compile("\\s*,\\s*");


    public ItemLookup(IBShop plugin) {
        this.plugin = plugin;
        this.saveFile = new File(plugin.getDataFolder(), "items.csv");

        this.reload();
    }


    public ItemStack get(final String id, final int quantity) {
        final ItemData itemData = Aliases.get(id.toLowerCase(Locale.ENGLISH));
        if (itemData != null) {
            return new ItemStack(itemData.getMaterial(), quantity, itemData.getItemData());
        }
        return null;
    }

    public ItemStack get(final String id) {
        final ItemData itemData = Aliases.get(id.toLowerCase(Locale.ENGLISH));
        if (itemData != null) {
            return new ItemStack(itemData.getMaterial(), 1, itemData.getItemData());
        }
        return null;
    }

    public String preferredName(final ItemStack itemStack) {
        final ItemData itemData = new ItemData(itemStack.getType(), itemStack.getDurability());
        return PreferredNames.get(itemData);
    }

    public List<String> matchString(final String arg) {
        String search = arg.toLowerCase(Locale.ENGLISH);

        // Build list of candidate items
        List<ItemData> matches = new ArrayList<>();

        // Translate to Preferred Names
        List<String> names = new ArrayList<>();

        if (search.length() < 2) {
            return names;
        }

        for (String alias : Aliases.keySet()){
            if (alias.contains(search)) {
                ItemData item = Aliases.get(alias);
//                log(Level.INFO, search + " => " + alias + " (" + item.getMaterial() + ")");
                if (! matches.contains(item)) {
                    matches.add(item);
                }
            }
        }

        for (ItemData item : matches) {
            String prefName = PreferredNames.get(item);
            if (! names.contains(prefName)) {
                names.add(prefName);
            }
        }

        return names;
    }

    public void reload() {
        log(Level.INFO, "Loading lookup items");

        if (! saveFile.exists()){
            // write the items file if it does not exist
            plugin.saveResource("items.csv", false);
        }

        if (! saveFile.exists()){
            log(Level.SEVERE, "Item List items.csv not found.");
            return;
        }

        PreferredNames.clear();
        Aliases.clear();

        try {
            try(BufferedReader br = new BufferedReader(new FileReader(saveFile))){
                for (String line; (line = br.readLine()) != null; ){
                    if (line.length() > 0 && line.charAt(0) == '#')
                        continue;

                    String[] fields = csvSplit.split(line);

                    if (fields.length < 2)
                        continue;

                    String itemName = fields[0].toLowerCase(Locale.ENGLISH);
                    int numeric = Integer.parseInt(fields[1]);
                    short data = (fields.length > 2) ? Short.parseShort(fields[2]) : 0;
                    String nbt = (fields.length > 3) ? StringUtils.stripToNull(fields[3]) : null;

                    Material material = Material.matchMaterial(itemName);
                    if (material == null) material = Material.matchMaterial(fields[1]);

                    if (material == null) {
                        log(Level.WARNING, "Failed to find material for " + itemName);
                        continue;
                    }

                    ItemData itemData = new ItemData(material, data);

                    if (!PreferredNames.containsKey(itemData)) {
                        PreferredNames.put(itemData, itemName);
                    }
                    Aliases.put(itemName, itemData);

                    if (nbt != null) {
                        nbtData.put(itemName, nbt);
                    }
                }
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    private void log(Level level, String message) {
        plugin.getLogger().log(level, message);
    }

    static class ItemData {
        final private Material material;
        final private short itemData;

        ItemData(Material material, short itemData) {
            this.material = material;
            this.itemData = itemData;
        }

        Material getMaterial() {
            return material;
        }

        short getItemData() {
            return itemData;
        }

        @Override
        public int hashCode() {
            return (31 * material.hashCode()) ^ itemData;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null)
                return false;
            if (! (o instanceof ItemData))
                return false;

            ItemData other = (ItemData) o;
            return this.material == other.getMaterial() && this.itemData == other.getItemData();
        }
    }

}
