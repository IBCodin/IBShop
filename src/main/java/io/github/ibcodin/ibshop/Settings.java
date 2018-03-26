package io.github.ibcodin.ibshop;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Level;

public class Settings {

    private final IBShop plugin;

//    private FileConfiguration config;

    private String messagePrefix = "&b[ibshop]:";
    private double listingFee = 5.00;
    private double salesFee = 10.00;
    private int maxChestCount = 10;

    public Settings(IBShop plugin) {
        this.plugin = plugin;

        reload();

        // make sure all known configuration items exist in the file
        save();
    }

    public void reload() {
        log(Level.INFO, "Loading settings");
        FileConfiguration config = plugin.getConfig();
        messagePrefix = config.getString("MessagePrefix", messagePrefix);
        listingFee = config.getDouble("ListingFee", listingFee);
        salesFee = config.getDouble("SalesFee", salesFee);
        maxChestCount = config.getInt("MaxChestCount", maxChestCount);
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
        return ChatColor.translateAlternateColorCodes('&', String.format("%s&r %s", messagePrefix, message));
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
        plugin.log(level, message);
    }
}
