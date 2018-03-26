package io.github.ibcodin.ibshop;

import io.github.ibcodin.ibshop.commands.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class IBShop extends JavaPlugin {

    private final Map<UUID, Object> userCache = new HashMap<>();
    private Economy economy;
    private Settings settings;
    private ItemLookup itemLookup;
    private BlackList blackList;
    private SalesList salesList;

    public IBShop() {
    }

    @Override
    public void onEnable() {
        log(Level.INFO, "initializing");

        // If the plugin directory does not exist, create it
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        loadMessages();

        // Find the economy
        RegisteredServiceProvider<Economy> rsp = getServer()
                .getServicesManager()
                .getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        }

        if (getEconomy() == null) {
            log(Level.SEVERE, "Economy not found");
        }

        settings = new Settings(this);

        itemLookup = new ItemLookup(this);

        blackList = new BlackList(this);

        salesList = new SalesList(this);

        log(Level.INFO, "Registering commands");
        new CommandBase(this);

        log(Level.INFO, "enabled");
    }

    @Override
    public void onDisable() {
    }

    public final Economy getEconomy() {
        return economy;
    }

    public final ItemLookup getItemLookup() {
        return itemLookup;
    }

    public final SalesList getSalesList() {
        return salesList;
    }

    public final Settings getSettings() {
        return settings;
    }

    public final BlackList getBlackList() {
        return blackList;
    }

    public final String prefix(String message) {
        return ChatColor.translateAlternateColorCodes('&',
                String.format("%s&r %s", getSettings().getMessagePrefix(), message));
    }

    public void send(final CommandSender sender, final String message) {
        if ((sender == null) || (message == null) || message.isEmpty())
            return;

        sender.sendMessage(prefix(message));
    }

    public void send(final CommandSender sender, final IBShopMessages message, final Object... args) {
        send(sender, message.format(args));
    }

    public void log(Level level, String message) {
        getLogger().log(level, message);
    }

    public void log(Level level, String message, Exception exception) {
        getLogger().log(level, message, exception);
    }

    public void log(Level level, String message, Object... args) {
        getLogger().log(level, MessageFormat.format(message, args));
    }


    // Support for an item associated with a player
    // Primarily used for confirmation messages
    public Object getCachedItem(UUID playerId) {
        if (userCache.containsKey(playerId))
            return userCache.get(playerId);
        return null;
    }

    public void setCachedItem(UUID playerId, Object toCache) {
        userCache.put(playerId, toCache);
    }

    public void clearCachedItem(UUID playerId) {
        if (userCache.containsKey(playerId))
            userCache.remove(playerId);
    }

    // Reload all of the configuration
    public void reloadAll() {
        getSettings().reload();
        getItemLookup().reload();
        getBlackList().reload();
        getSalesList().reload();
    }



    private void loadMessages() {
        // Save the resource file as the message file if it does not exist
        saveResource("messages.yml", false);

        try {
            File saveFile = new File(getDataFolder(), "messages.yml");
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.load(saveFile);

            // Message conversion if necessary
            updateOldMessages(yaml);

            // Default values
            YamlConfiguration msgYaml = IBShopMessages.toYaml();

            // List of existing messages and plugin messages
            Set<String> existing = yaml.getKeys(true);
            Set<String> messages = msgYaml.getKeys(true);

            // For each plugin message, if it does not already exist, set it
            for (String message : messages) {
                if (! existing.remove(message)) {
                    yaml.set(message, msgYaml.get(message));
                }
            }

            // For any existing messages that did not match plugin messages, remove them
            for (String toremove : existing) {
                yaml.set(toremove, null);
            }

            yaml.save(saveFile);

            IBShopMessages.load(yaml);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateOldMessages(YamlConfiguration yaml) {
        // If we have the old configuration section
        if (yaml.isConfigurationSection("Messages")) {
            ConfigurationSection config = yaml.getConfigurationSection("Messages");

            for (String key : config.getKeys(false)) {
                // build the new key
                String newKey = key.toLowerCase().replace('_', '-');
                if (newKey.startsWith("msg-")) {
                    newKey = newKey.substring(4);
                }
                // if the new key does not already exist, set it from the old key value
                if (! yaml.contains(newKey)) {
                    yaml.set(newKey, config.get(key));
                }
                // remove the old key value
                config.set(key, null);
            }
        }
    }
}
