package io.github.ibcodin.ibshop;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static io.github.ibcodin.ibshop.MessageLookup.IBShopMessages.*;

public class MessageLookup {

    private final static String configSectionName = "Messages";
    private final IBShop plugin;
    private final Settings settings;
    private final File saveFile;
    private FileConfiguration config;

    private Map<IBShopMessages, String> messageMap = new HashMap<>();

    public MessageLookup(IBShop plugin) {
        this.plugin = plugin;
        this.settings = plugin.getSettings();

        saveFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!saveFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }


        reload();


    }

    public void sendMessage(CommandSender sender, MessageLookup.IBShopMessages msg, Object... args) {
//        log(Level.INFO, "sendMessage to " + sender.getName() + ": " + msg.name() + " #args=" + args.length);
//        for (Object arg : args) {
//            log(Level.INFO, "  " + arg.toString());
//        }

        if (messageMap.containsKey(msg)) {
            final String msgFmt = messageMap.get(msg);
//            log(Level.INFO, msg.name() + " => " + msgFmt);
            final String msgTxt = new MessageFormat(msgFmt).format(args);
//            log(Level.INFO, msgFmt + " => " + msgTxt);
            sender.sendMessage(settings.prefixMessage(msgTxt));
            return;
        }
        log(Level.WARNING, "Failed to send " + msg.name());
    }

    public void reload() {
        log(Level.INFO, "Loading Messages");

        if (!saveFile.exists()) {
            log(Level.SEVERE, "Could not open messages.yml");
        }

        // Initialize with default messages (will create any new defaults)
        config = YamlConfiguration.loadConfiguration(saveFile);

        if (!config.isConfigurationSection(configSectionName)) {
            config.createSection(configSectionName);
        }

        ConfigurationSection messages = config.getConfigurationSection(configSectionName);

        addDefaultMessage(messages, MSG_BAD_SELL_ARGS, "Sales listings require an item to sell, quantity and each price.");
        addDefaultMessage(messages, MSG_SELL_USAGE, "{0} <itemname|hand> <quantity> <each_price>");
        addDefaultMessage(messages, MSG_SELL_BAD_PRICE, "Sales listings must have a non-zero sales price");
        addDefaultMessage(messages, MSG_BAD_FIND_ARGS, "Find requires an item to search for.");
        addDefaultMessage(messages, MSG_FIND_USAGE, "{0} <itemname>");
        addDefaultMessage(messages, MSG_BAD_CANCEL_ARGS, "To cancel items from a listing, specify the item and how many to cancel.");
        addDefaultMessage(messages, MSG_CANCEL_USAGE, "{0} <itemname> <quantity>");
        addDefaultMessage(messages, MSG_BAD_BUY_ARGS, "Purchases require an item to buy, quantity and maximum purchase price.");
        addDefaultMessage(messages, MSG_BUY_USAGE, "{0} <itemname> <quantity> <max_each_price>");
        addDefaultMessage(messages, MSG_NOT_PLAYER, "You must be a player to buy or sell items.");
        addDefaultMessage(messages, MSG_NOT_MATERIAL, "We don''t recognize {0} as an item.");
        addDefaultMessage(messages, MSG_NOT_WHITELIST, "We don''t accept {0} for sale.");
        addDefaultMessage(messages, MSG_NO_LISTINGS, "No listings found");
        addDefaultMessage(messages, MSG_NO_MORE_LISTINGS, "No more listings");
        addDefaultMessage(messages, MSG_TOO_FEW_ITEMS, "You do not have {0} x {1} to sell.");
        addDefaultMessage(messages, MSG_ECON_LISTING_FAIL, "You can''t afford the total listing fee of {0}");
        addDefaultMessage(messages, MSG_LISTINGS_FULL, "You don''t have enough space to sell that many more items");
        addDefaultMessage(messages, MSG_ECON_BUY_FAIL, "The total purchase price could be {0} and you don''t have it");
        addDefaultMessage(messages, MSG_SUPPLY_BUY_FAIL, "We could not find {0} x {1} at {2} or less");
        addDefaultMessage(messages, MSG_INV_FAIL, "You don''t have enough room in your inventory for {0} x {1}");
        addDefaultMessage(messages, MSG_STOCK_NO_ITEMS, "You don''t appear to have any items for sale");
        addDefaultMessage(messages, MSG_CANCEL_NO_ITEMS, "You aren''t selling {0}");
        addDefaultMessage(messages, MSG_INVALID_PAGE_NUMBER, "The value {0} is not a valid page number");
        addDefaultMessage(messages, MSG_PAGE_OF, "  page {0} of {1}");
        addDefaultMessage(messages, MSG_PRIOR_LISTING, "You already have {0} x {1} for sale at {2}");
        addDefaultMessage(messages, MSG_CHANGE_PRICE, "This sale will update the price from {0} to {1}");
        addDefaultMessage(messages, MSG_HIGHER_FEE, "There will be a listing fee of {0} for this increase");
        addDefaultMessage(messages, MSG_LISTING_TOTAL, "The total price for {0} x {1} at {2} will be {3}");
        addDefaultMessage(messages, MSG_LISTING_FEE, "There will be a listing fee of {0} for this new listing");
        addDefaultMessage(messages, MSG_FIND_LISTING, "Searching for listings for {0}");
        addDefaultMessage(messages, MSG_FOUND_LISTING, "   {0} at {1} each");
        addDefaultMessage(messages, MSG_STOCK_LISTING, "  {0} x {1} at {2} each");
        addDefaultMessage(messages, MSG_LISTING_ADDED, "You are now selling {0} x {1} at {2} total value is {3}");
        addDefaultMessage(messages, MSG_ITEMS_BOUGHT, "You paid {0} for {1} x {2} at {3}");
        addDefaultMessage(messages, MSG_ITEMS_CANCELLED, "Returned {0} x {1} from your sale to your inventory");
        addDefaultMessage(messages, MSG_ITEMS_SOLD, "You received {0} for selling {1} x {2} at {3}");
        addDefaultMessage(messages, MSG_SALES_SUMMARY, "  {0} {1}");
        addDefaultMessage(messages, MSG_RESEND_TO_CONFIRM, "Resend command to confirm");
        addDefaultMessage(messages, MSG_PREFIX_CHANGED, "Changed message prefix from {0} to {1}");
        addDefaultMessage(messages, MSG_NEGATIVE_LISTING_FEE, "The listing fee cannot be < 0.0");
        addDefaultMessage(messages, MSG_EXCESSIVE_LISTING_FEE, "The listing fee cannot exceed 50.0");
        addDefaultMessage(messages, MSG_LISTING_FEE_CHANGED, "Changed listing fee from {0} to {1}");
        addDefaultMessage(messages, MSG_NEGATIVE_SALES_FEE, "The sales fee cannot be < 0.0");
        addDefaultMessage(messages, MSG_EXCESSIVE_SALES_FEE, "The sales fee cannot exceed 75.0");
        addDefaultMessage(messages, MSG_SALES_FEE_CHANGED, "Changed sales fee from {0} to {1}");
        addDefaultMessage(messages, MSG_CHEST_COUNT_INVALID, "The chest count cannot be < 0");
        addDefaultMessage(messages, MSG_CHEST_COUNT_CHANGED, "The chest count changed from {0} to {1}");
        addDefaultMessage(messages, MSG_CHEST_STACK_LIMIT, "The new stack limit is {0}");
        addDefaultMessage(messages, MSG_CHEST_STACK_NO_LIMIT, "There is no stack limit for any seller");

        config.options().copyDefaults(true);
        try {
            config.save(saveFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        messageMap.clear();

        for (IBShopMessages msg : IBShopMessages.values()) {
            final String msgName = msg.name();
            final String configString = messages.getString(msgName);
            if (configString != null) {
                messageMap.put(msg, configString);
            } else {
                log(Level.WARNING, "No message definition for " + msgName);
            }
        }
    }

//    public String cfgMessage( IBShopMessages msg, Object ... args) {
//        if (messageMap.containsKey(msg)) {
//            return new MessageFormat(settings.prefixMessage(messageMap.get(msg))).format(args);
//        }
//        log(Level.WARNING, "Failed to find " + msg.name());
//        return "";
//    }

    protected void addDefaultMessage(ConfigurationSection section, IBShopMessages message, String messageText) {
        section.addDefault(message.name(), messageText);
    }

    protected void log(Level level, String message) {
        plugin.getLogger().log(level, message);
    }

    public enum IBShopMessages {
        MSG_BAD_SELL_ARGS,
        MSG_SELL_USAGE,
        MSG_SELL_BAD_PRICE,
        MSG_BAD_FIND_ARGS,
        MSG_FIND_USAGE,
        MSG_BAD_CANCEL_ARGS,
        MSG_CANCEL_USAGE,
        MSG_BAD_BUY_ARGS,
        MSG_BUY_USAGE,

        MSG_NOT_PLAYER,
        MSG_NOT_MATERIAL,
        MSG_NOT_WHITELIST,
        MSG_NO_LISTINGS,
        MSG_NO_MORE_LISTINGS,
        MSG_TOO_FEW_ITEMS,
        MSG_ECON_LISTING_FAIL,
        MSG_LISTINGS_FULL,
        MSG_ECON_BUY_FAIL,
        MSG_SUPPLY_BUY_FAIL,
        MSG_INV_FAIL,
        MSG_STOCK_NO_ITEMS,
        MSG_CANCEL_NO_ITEMS,
        MSG_INVALID_PAGE_NUMBER,
        MSG_PAGE_OF,

        MSG_PRIOR_LISTING,
        MSG_CHANGE_PRICE,
        MSG_HIGHER_FEE,
        MSG_LISTING_TOTAL,
        MSG_LISTING_FEE,
        MSG_FIND_LISTING,
        MSG_FOUND_LISTING,
        MSG_STOCK_LISTING,
        MSG_LISTING_ADDED,
        MSG_ITEMS_BOUGHT,
        MSG_ITEMS_CANCELLED,
        MSG_ITEMS_SOLD,
        MSG_SALES_SUMMARY,
        MSG_RESEND_TO_CONFIRM,

        MSG_PREFIX_CHANGED,
        MSG_NEGATIVE_LISTING_FEE,
        MSG_EXCESSIVE_LISTING_FEE,
        MSG_LISTING_FEE_CHANGED,
        MSG_NEGATIVE_SALES_FEE,
        MSG_EXCESSIVE_SALES_FEE,
        MSG_SALES_FEE_CHANGED,
        MSG_CHEST_COUNT_INVALID,
        MSG_CHEST_COUNT_CHANGED,
        MSG_CHEST_STACK_LIMIT,
        MSG_CHEST_STACK_NO_LIMIT
    }

}
