package io.github.ibcodin.ibshop;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.text.MessageFormat;

public enum IBShopMessages {
    BAD_SELL_ARGS("Sales listings require an item to sell, quantity and each price."),
    SELL_USAGE("{0} <itemname|hand> <quantity> <each_price>"),
    SELL_BAD_PRICE("Sales listings must have a non-zero sales price"),
    BAD_FIND_ARGS("Find requires an item to search for."),
    FIND_USAGE("{0} <itemname>"),
    BAD_CANCEL_ARGS("To cancel items from a listing, specify the item and how many to cancel."),
    CANCEL_USAGE("{0} <itemname> <quantity>"),
    BAD_BUY_ARGS("Purchases require an item to buy, quantity and maximum purchase price."),
    BUY_USAGE("{0} <itemname> <quantity> <max_each_price>"),
    NOT_PLAYER("&dYou must be a player for the command /{0}."),
    NOT_MATERIAL("&dThe name {0} is not recognized as a material."),
    NOT_WHITELIST("&dThe material {0} can''t be sold or bought."),
    NO_LISTINGS("No listings found"),
    NO_MORE_LISTINGS("No more listings"),
    TOO_FEW_ITEMS("You do not have {0} x {1} to sell."),
    ECON_LISTING_FAIL("You can''t afford the total listing fee of {0}"),
    LISTINGS_FULL("You don''t have enough space to sell that many more items"),
    ECON_BUY_FAIL("The total purchase price could be {0} and you don''t have it"),
    SUPPLY_BUY_FAIL("We could not find {0} x {1} at {2} or less"),
    INV_FAIL("You don''t have enough room in your inventory for {0} x {1}"),
    STOCK_NO_ITEMS("You don''t appear to have any items for sale"),
    CANCEL_NO_ITEMS("You aren''t selling {0}"),
    INVALID_PAGE_NUMBER("The value {0} is not a valid page number"),
    PAGE_OF("  page {0} of {1}"),
    PRIOR_LISTING("You already have {0} x {1} for sale at {2}"),
    CHANGE_PRICE("This sale will update the price from {0} to {1}"),
    HIGHER_FEE("There will be a listing fee of {0} for this increase"),
    LISTING_TOTAL("The total price for {0} x {1} at {2} will be {3}"),
    LISTING_FEE("There will be a listing fee of {0} for this new listing"),
    FIND_LISTING("Searching for listings for {0}"),
    FOUND_LISTING("   {0} at {1} each"),
    STOCK_LISTING("  {0} x {1} at {2} each"),
    LISTING_ADDED("You are now selling {0} x {1} at {2} total value is {3}"),
    ITEMS_BOUGHT("You paid {0} for {1} x {2} at {3}"),
    ITEMS_CANCELLED("Returned {0} x {1} from your sale to your inventory"),
    ITEMS_SOLD("You received {0} for selling {1} x {2} at {3}"),
    SALES_SUMMARY("  {0} {1}"),
    RESEND_TO_CONFIRM("Resend command to confirm"),

    CMD_BASE_HELP_1("General Interface"),
    CMD_BASE_HELP_2(""),
    CMD_BASE_NO_SUB_FOUND("&d unrecognized sub command {0}"),

    CMD_BUY_HELP_1("/{0} <item> <quantity> <max_each_price>"),
    CMD_BUY_HELP_2("&e  Request to purchase quantity of item."),
    CMD_BUY_HELP_3("&e  You must be able to pay for quantity items at max_each_price"),
    CMD_BUY_HELP_4("&e  You will pay no more than max_each_price for each item"),
    CMD_BUY_HELP_5("&e  You will only purchase what can fit in your inventory"),
    CMD_BUY_HELP_6(""),
    CMD_BUY_HELP_7(""),
    CMD_BUY_FAIL("&cPurchase Failed"),

    CMD_CANCEL_HELP_1("/{0} <item> <quantity>"),
    CMD_CANCEL_HELP_2("&e  Return quantity item from your active sale to inventory"),
    CMD_CANCEL_HELP_3(""),
    CMD_CANCEL_HELP_4(""),
    CMD_CANCEL_DETAIL_HELP_1("&e  You can only cancel what you have for sale."),
    CMD_CANCEL_DETAIL_HELP_2("&e  You will NOT be refunded any of the listing fee."),
    CMD_CANCEL_DETAIL_HELP_3("&e  You can only cancel what will fit in your inventory."),
    CMD_CANCEL_DETAIL_HELP_4(""),

    CMD_CONFIG_HELP_1("/{0} <parameter> <value>"),
    CMD_CONFIG_HELP_2("&e  Change global shop <parameter> to <value>"),
    CMD_CONFIG_HELP_3(""),
    CMD_CONFIG_DETAIL_HELP_1("&eValid Parameters:"),
    CMD_CONFIG_DETAIL_HELP_2("&b  MessagePrefix &e- prefix added to messages from this plugin"),
    CMD_CONFIG_DETAIL_HELP_3("&b  ListingFee &e- % charge to sellers to list items"),
    CMD_CONFIG_DETAIL_HELP_4("&b  SalesFee &e- % charge to purchasers of items"),
    CMD_CONFIG_DETAIL_HELP_5("&b  MaxChestCount &e- Maximum number of chests for any player"),
    CMD_CONFIG_DETAIL_HELP_6("&e     If set to zero, no seller has any limits"),
    CMD_CONFIG_DETAIL_HELP_7(""),

    CMD_CONFIG_BAD_PARAM_1("&cInvalid configuration setting."),
    CMD_CONFIG_BAD_PARAM_2("&eValid options are: MessagePrefix, ListingFee, SalesFee, MaxChestCount"),

    CMD_FIND_HELP_1("/{0} <item>"),
    CMD_FIND_HELP_2("&e  Find sales of the item"),
    CMD_FIND_HELP_3(""),
    CMD_FIND_HELP_4(""),
    CMD_FIND_DETAIL_HELP_1("&e This will try to match your text to a specific item"),
    CMD_FIND_DETAIL_HELP_2("&e and show the details for any sale listing that item"),
    CMD_FIND_DETAIL_HELP_3(""),
    CMD_FIND_DETAIL_HELP_4(""),

    CMD_LIST_HELP_1("/{0} [item] [page]"),
    CMD_LIST_HELP_2("&e List sales, optionally filtered by item in pages"),
    CMD_LIST_HELP_3(""),
    CMD_LIST_DETAIL_HELP_1("&e item supports partial matching"),
    CMD_LIST_DETAIL_HELP_2("&e The item &bWO&e would match both &bWOOD&e and &bWOOL"),
    CMD_LIST_DETAIL_HELP_3("&e output will show page # of #"),
    CMD_LIST_DETAIL_HELP_4(""),

    CMD_RELOAD_HELP_1("/{0}"),
    CMD_RELOAD_HELP_2("&e  Rereads plugin configuration from disk"),
    CMD_RELOAD_HELP_3(""),
    CMD_RELOAD_DONE("Reloaded"),

    CMD_SELL_HELP_1("/{0} <item> <quantity> [each_price]"),
    CMD_SELL_HELP_2("&e  Place quantity items up for sale at the each_price"),
    CMD_SELL_HELP_3("&e  The listing fee is paid when you list the sale"),
    CMD_SELL_HELP_4(""),
    CMD_SELL_DETAIL_HELP_1(""),
    CMD_SELL_DETAIL_HELP_2(""),
    CMD_SELL_DETAIL_HELP_3(""),
    CMD_SELL_FAIL("&c Sale Failed"),

    CMD_STOCK_HELP_1("/{0} [page]"),
    CMD_STOCK_HELP_2("&e  Show the items you have for sale"),
    CMD_STOCK_HELP_3(""),


    PARAM_PREFIX_HELP_1("&eChanges the message prefix for messages sent from this plugin. Currently {0}"),
    PARAM_PREFIX_HELP_2(""),
//    PARAM_PREFIX_CHANGE_1(""),
    PREFIX_CHANGED("&eChanged message prefix from {0} to {1}"),
    
    PARAM_LISTING_FEE_HELP_1("&eChanges the percentage fee charged to the seller to list a sale."),
    PARAM_LISTING_FEE_HELP_2("&eMust be >= 0.0 and <= 50.0, currently {0}"),
    PARAM_LISTING_FEE_HELP_3(""),
    PARAM_LISTING_FEE_HELP_4(""),
//    PARAM_LISTING_FEE_CHANGE_1(""),
    NEGATIVE_LISTING_FEE("The listing fee cannot be less than 0.0"),
    EXCESSIVE_LISTING_FEE("The listing fee cannot exceed 50.0"),
    LISTING_FEE_CHANGED("Changed listing fee from {0} to {1}"),

    PARAM_SALES_FEE_HELP_1("&eChanges the percentage fee charged to the buyer of items for sale."),
    PARAM_SALES_FEE_HELP_2("&eMust be >= 0.0 and <= 75.0, currently {0}"),
    PARAM_SALES_FEE_HELP_3(""),
    PARAM_SALES_FEE_HELP_4(""),
//    PARAM_SALES_FEE_CHANGE_1(""),
    NEGATIVE_SALES_FEE("The sales fee cannot be less than 0.0"),
    EXCESSIVE_SALES_FEE("The sales fee cannot exceed 75.0"),
    SALES_FEE_CHANGED("Changed sales fee from {0} to {1}"),
    
    PARAM_MAX_CHESTS_HELP_1("&eChanges the maximum space a seller can use."),
    PARAM_MAX_CHESTS_HELP_2("&b&oIf set to zero, no seller has any limits."),
    PARAM_MAX_CHESTS_HELP_3("&eIf not zero, used to build permission names like:"),
    PARAM_MAX_CHESTS_HELP_4("&d  ibshop.quantity.1, ibshop.quantity.2"),
    PARAM_MAX_CHESTS_HELP_5("&eUsers will be limited to the number of chests"),
    PARAM_MAX_CHESTS_HELP_6("&efor the highest permission number assigned"),
    PARAM_MAX_CHESTS_HELP_7("&e(but no more than this value)"),
    PARAM_MAX_CHESTS_HELP_8("&eMust be >= 0, currently {0}"),
    PARAM_MAX_CHESTS_HELP_9(""),
//    PARAM_MAX_CHESTS_CHANGE_1(""),
    CHEST_COUNT_INVALID("The chest count cannot be < 0"),
    CHEST_COUNT_CHANGED("The chest count changed from {0} to {1}"),
    CHEST_STACK_LIMIT("The new stack limit is {0}"),
    CHEST_STACK_NO_LIMIT("There is no stack limit for any seller"),

    ;


    private String value;

    IBShopMessages(String value) { set(value); }
    
    void set(String value) { this.value = value; }

    public String cfgName() { return this.name().toLowerCase().replace("_", "-"); }
    
    public String toString() { return ChatColor.translateAlternateColorCodes('&', value); }
    
    public String format(Object... args) { return MessageFormat.format(toString(), args); }

    static void load(ConfigurationSection config) {
        for (IBShopMessages msg : IBShopMessages.values()) {
            msg.set(config.getString(msg.cfgName(), ""));
        }
    }

    static YamlConfiguration toYaml() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (IBShopMessages msg : IBShopMessages.values()) {
            yaml.set(msg.cfgName(), msg.value);
        }
        return yaml;
    }
}
