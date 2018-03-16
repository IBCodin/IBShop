package io.github.ibcodin.ibshop.commands;

import io.github.ibcodin.ibshop.IBShop;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import static io.github.ibcodin.ibshop.MessageLookup.IBShopMessages.*;

public class CommandConfig extends CommandHandler {
    public static final String CommandName = "ibshopconfig";

    public CommandConfig(IBShop plugin) {
        super(plugin, CommandName);
    }

    @Override
    public void sendHelp(CommandSender sender, String label, boolean detailHelp) {
        if (!senderHasPermission(sender)) return;

        sendMessage(sender, "/" + label + " parameter value");
        sendMessage(sender, ChatColor.YELLOW + "  Change global shop parameter value");

        if (!detailHelp) return;
        sendMessage(sender, ChatColor.YELLOW + "Valid Parameters:");
        sendMessage(sender, ChatColor.AQUA + "  MessagePrefix " + ChatColor.YELLOW + "- prefix added to messages from this plugin");
        sendMessage(sender, ChatColor.AQUA + "  ListingFee " + ChatColor.YELLOW + "- % charge to sellers to list items");
        sendMessage(sender, ChatColor.AQUA + "  SalesFee " + ChatColor.YELLOW + "- % charge to purchasers of items");
        sendMessage(sender, ChatColor.AQUA + "  MaxChestCount " + ChatColor.YELLOW + "- Maximum number of chests for any player");
        sendMessage(sender, ChatColor.YELLOW + "     If set to zero, no seller has any limits");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sendHelp(sender, label, true);
            return true;
        }

        if (args[0].equalsIgnoreCase("MessagePrefix")) {
            String oldPrefix = plugin.getSettings().getMessagePrefix();

            if (args.length < 2) {
                sendMessage(sender, ChatColor.YELLOW + "Changes the message prefix for messages sent from this plugin. Currently " + oldPrefix);
                return true;
            }

            plugin.getSettings().setMessagePrefix(args[1]);
            sendMessage(sender, MSG_PREFIX_CHANGED, oldPrefix, args[1]);
            log(Level.INFO, sender.getName() + " changed message prefix from " + oldPrefix + " to " + args[1]);
            return true;
        }

        if (args[0].equalsIgnoreCase("ListingFee")) {
            double oldFee = plugin.getSettings().getListingFee();

            if (args.length < 2) {
                sendMessage(sender, ChatColor.YELLOW + "Changes the percentage fee charged to the seller to list a sale.");
                sendMessage(sender, ChatColor.YELLOW + "Must be > 0.0 and <= 50.0, currently " + oldFee);
                return true;
            }

            double listingFee = Double.parseDouble(args[1]);

            if (listingFee < 0.0) {
                sendMessage(sender, MSG_NEGATIVE_LISTING_FEE);
                return true;
            }

            if (listingFee > 50.0) {
                sendMessage(sender, MSG_EXCESSIVE_LISTING_FEE);
                return true;
            }

            plugin.getSettings().setListingFee(listingFee);
            sendMessage(sender, MSG_LISTING_FEE_CHANGED, oldFee, listingFee);
            plugin.getLogger().log(Level.INFO, sender.getName() + " changed listing fee from " + oldFee + " to " + listingFee);
            return true;
        }

        if (args[0].equalsIgnoreCase("SalesFee")) {
            double oldFee = plugin.getSettings().getSalesFee();

            if (args.length < 2) {
                sendMessage(sender, ChatColor.YELLOW + "Changes the percentage fee charged to the buyer of items for sale.");
                sendMessage(sender, ChatColor.YELLOW + "Must be > 0.0 and <= 75.0, currently " + oldFee);
                return true;
            }

            double salesFee = Double.parseDouble(args[1]);

            if (salesFee < 0.0) {
                sendMessage(sender, MSG_NEGATIVE_SALES_FEE);
                return true;
            }

            if (salesFee > 75.0) {
                sendMessage(sender, MSG_EXCESSIVE_SALES_FEE);
                return true;
            }

            plugin.getSettings().setSalesFee(salesFee);
            sendMessage(sender, MSG_SALES_FEE_CHANGED, oldFee, salesFee);
            log(Level.INFO, sender.getName() + " changed sales fee from " + oldFee + " to " + salesFee);
            return true;
        }

        if (args[0].equalsIgnoreCase("MaxChestCount")) {
            int oldCount = plugin.getSettings().getMaxChestCount();

            if (args.length < 2) {
                sendMessage(sender, ChatColor.YELLOW + "Changes the maximum space a seller can use.");
                sendMessage(sender, ChatColor.ITALIC + "" + ChatColor.AQUA + "If set to zero, no seller has any limits.");
                sendMessage(sender, ChatColor.YELLOW + "If not zero, used to build permission names like:");
                sendMessage(sender, ChatColor.BLUE + "  ibshop.quantity.1, ibshop.quantity.2");
                sendMessage(sender, ChatColor.YELLOW + "Users will be limited to the number of chests");
                sendMessage(sender, ChatColor.YELLOW + "for the highest permission number assigned");
                sendMessage(sender, ChatColor.YELLOW + "(but no more than this value)");
                sendMessage(sender, ChatColor.YELLOW + "Must be >= 0, currently " + oldCount);
                return true;
            }

            int chestCount = Integer.parseInt(args[1]);

            if (chestCount < 0) {
                sendMessage(sender, MSG_CHEST_COUNT_INVALID);
                return true;
            }

            plugin.getSettings().setMaxChestCount(chestCount);

            sendMessage(sender, MSG_CHEST_COUNT_CHANGED, oldCount, chestCount);
            log(Level.INFO, sender.getName() + " changed chest count from " + oldCount + " to " + chestCount);

            if (chestCount > 0) {
                sendMessage(sender, MSG_CHEST_STACK_LIMIT, chestCount * 27);
            } else {
                sendMessage(sender, MSG_CHEST_STACK_NO_LIMIT);
            }

            return true;
        }

        sendMessage(sender, ChatColor.RED + "Invalid configuration setting.");
        sendMessage(sender, ChatColor.YELLOW + "Valid options are: MessagePrefix, ListingFee, SalesFee, MaxChestCount");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {

        if (args.length == 0)
            return null;

        if (args.length != 1)
            return null;

        if (args[0].equals(""))
            return configs();

        String lowerArg = args[0].toLowerCase();
        List<String> returnList = new ArrayList<>();
        for (String config : configs()) {
            if (config.toLowerCase().startsWith(lowerArg)) {
                returnList.add(config);
            }
        }

        return returnList;
    }

    protected List<String> configs() {
        List<String> rval = new ArrayList<>();
        rval.add("MessagePrefix");
        rval.add("ListingFee");
        rval.add("SalesFee");
        rval.add("MaxChestCount");
        Collections.sort(rval);
        return rval;
    }

}
