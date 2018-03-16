package io.github.ibcodin.ibshop;

import io.github.ibcodin.ibshop.commands.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class IBShop extends JavaPlugin {

    private final Map<UUID, Object> userCache = new HashMap<>();
    private Economy economy;
    private Settings settings;
    private MessageLookup messageLookup;
    private ItemLookup itemLookup;
    private BlackList blackList;
    private SalesList salesList;
    private CommandHandler base;
    private CommandHandler list;
    private CommandHandler find;
    private CommandHandler buy;
    private CommandHandler sell;
    private CommandHandler stock;
    private CommandHandler cancel;
    private CommandHandler config;
    private CommandHandler reload;

    public IBShop() {
    }

    @Override
    public void onEnable() {
        log(Level.INFO, "initializing");

        // If the plugin directory does not exist, create it
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

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

        // Initialize the settings
        settings = new Settings(this);

        // Initialize MessageLookup
        messageLookup = new MessageLookup(this);

        // Load the Item list for lookups
        itemLookup = new ItemLookup(this);

        blackList = new BlackList(this);

        salesList = new SalesList(this);

        log(Level.INFO, "Registering commands");
        base = new CommandBase(this);
        list = new CommandList(this);
        find = new CommandFind(this);
        buy = new CommandBuy(this);
        sell = new CommandSell(this);
        stock = new CommandStock(this);
        cancel = new CommandCancel(this);
        config = new CommandConfig(this);
        reload = new CommandReload(this);

        base.addKid(list, "list");
        base.addKid(find, "find");
        base.addKid(buy, "buy");
        base.addKid(sell, "sell");
        base.addKid(stock, "stock");
        base.addKid(cancel, "cancel");
        base.addKid(config, "config");
        base.addKid(reload, "reload");

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

    public final MessageLookup getMessageLookup() {
        return messageLookup;
    }

    private void log(Level level, String message) {
        getLogger().log(level, message);
    }

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
}
