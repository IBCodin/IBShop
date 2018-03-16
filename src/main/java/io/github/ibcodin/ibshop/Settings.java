package io.github.ibcodin.ibshop;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Level;

public class Settings {

    private final IBShop plugin;

//    private FileConfiguration config;

    private String messagePrefix = "[ibshop]: ";
    private double listingFee = 5.00;
    private double salesFee = 10.00;
    private int maxChestCount = 10;

    public Settings(IBShop plugin) {
        this.plugin = plugin;

        // write the default configuration from the package if config.yml does not exist
        plugin.saveDefaultConfig();

        reload();
    }

    public void reload() {
        log(Level.INFO, "Loading settings");
        // Get config and preload defaults (will create any new defaults)
        FileConfiguration config = plugin.getConfig();
        config.addDefault("MessagePrefix", "[ibshop]: ");
        config.addDefault("ListingFee", 5.00);
        config.addDefault("SalesFee", 10.00);
        config.addDefault("MaxChestCount", 10);
        config.options().copyDefaults(true);
        plugin.saveConfig();

        config = plugin.getConfig();
        messagePrefix = config.getString("MessagePrefix");
        listingFee = config.getDouble("ListingFee");
        salesFee = config.getDouble("SalesFee");
        maxChestCount = config.getInt("MaxChestCount");
    }

    public void save() {
        FileConfiguration config = plugin.getConfig();
        config.set("MessagePrefix", messagePrefix);
        config.set("ListingFee", listingFee);
        config.set("SalesFee", salesFee);
        config.set("MaxChestCount", maxChestCount);
        plugin.saveConfig();
    }

    public String prefixMessage(String message) {
        return String.format("%s%s", messagePrefix, message);
    }

    public String getMessagePrefix() {
        return messagePrefix;
    }

    public void setMessagePrefix(String messagePrefix) {
        this.messagePrefix = messagePrefix;
        save();
    }

    public double getListingFee() {
        return listingFee;
    }

    public void setListingFee(double listingFee) {
        this.listingFee = listingFee;
        save();
    }

    public double getSalesFee() {
        return salesFee;
    }

    public void setSalesFee(double salesFee) {
        this.salesFee = salesFee;
        save();
    }

    public int getMaxChestCount() {
        return maxChestCount;
    }

    public void setMaxChestCount(int maxChestCount) {
        this.maxChestCount = maxChestCount;
        save();
    }

    protected void log(Level level, String message) {
        plugin.getLogger().log(level, message);
    }
}
