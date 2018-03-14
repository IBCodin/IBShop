package io.github.ibcodin.ibshop;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Settings {

    private final IBShop plugin;

    private FileConfiguration config;

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
        config = plugin.getConfig();
        messagePrefix = config.getString("MessagePrefix", "[ibshop]: ");
        listingFee = config.getDouble("ListingFee", 5.00);
        salesFee = config.getDouble("SalesFee", 10.00);
        maxChestCount = config.getInt("MaxChestCount", 10);
    }

    public void save() {
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
