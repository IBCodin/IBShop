package io.github.ibcodin.ibshop;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class MessageLookup {

    private final IBShop plugin;
    private final Settings settings;

    private final File saveFile;
    private FileConfiguration config;

    private Map<IBShopMessages, String> messageMap = new HashMap<>();

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


    public MessageLookup(IBShop plugin) {
        this.plugin = plugin;
        this.settings = plugin.getSettings();

        saveFile = new File(plugin.getDataFolder(), "messages.yml");

        reload();


    }

    public void sendMessage(CommandSender sender, MessageLookup.IBShopMessages msg, Object ... args) {
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

//    public String cfgMessage( IBShopMessages msg, Object ... args) {
//        if (messageMap.containsKey(msg)) {
//            return new MessageFormat(settings.prefixMessage(messageMap.get(msg))).format(args);
//        }
//        log(Level.WARNING, "Failed to find " + msg.name());
//        return "";
//    }

    public void reload() {
        log(Level.INFO,"Loading Messages");
        if (!saveFile.exists()) plugin.saveResource("messages.yml", false);

        if (!saveFile.exists()) {
            log(Level.SEVERE, "Could not open messages.yml");
        }

        config = YamlConfiguration.loadConfiguration(saveFile);

        if (config.isConfigurationSection("Messages")) {
            ConfigurationSection messages = config.getConfigurationSection("Messages");

            messageMap.clear();

            for (IBShopMessages msg : IBShopMessages.values()) {
                final String msgName = msg.name();
                final String configString = messages.getString(msgName);
                if (configString != null) {
                    messageMap.put(msg, configString);
//                    log(Level.INFO, "Found " + msgName + ": " + configString);
                } else {
                    log(Level.WARNING, "No message definition for " + msgName);
                }
            }
        }
    }

    protected void log(Level level, String message) {
        plugin.getLogger().log(level, message);
    }

}
