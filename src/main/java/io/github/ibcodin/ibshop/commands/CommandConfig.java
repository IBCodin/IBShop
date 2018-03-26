package io.github.ibcodin.ibshop.commands;

import io.github.ibcodin.ibshop.IBShop;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import static io.github.ibcodin.ibshop.IBShopMessages.*;

public class CommandConfig extends CommandHandler {
    public static final String CommandName = "ibshopconfig";

    public CommandConfig(IBShop plugin) {
        super(plugin, CommandName);
    }

    @Override
    public void sendHelp(CommandSender sender, String label, boolean detailHelp) {
        if (!senderHasPermission(sender)) return;

        send(sender, CMD_CONFIG_HELP_1, label);
        send(sender, CMD_CONFIG_HELP_2);
        send(sender, CMD_CONFIG_HELP_3);

        if (!detailHelp) return;
        send(sender, CMD_CONFIG_DETAIL_HELP_1);
        send(sender, CMD_CONFIG_DETAIL_HELP_2);
        send(sender, CMD_CONFIG_DETAIL_HELP_3);
        send(sender, CMD_CONFIG_DETAIL_HELP_4);
        send(sender, CMD_CONFIG_DETAIL_HELP_5);
        send(sender, CMD_CONFIG_DETAIL_HELP_6);
        send(sender, CMD_CONFIG_DETAIL_HELP_7);
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
                send(sender, PARAM_PREFIX_HELP_1, oldPrefix);
                send(sender, PARAM_PREFIX_HELP_2, oldPrefix);
                send(sender, ChatColor.YELLOW + "&eChanges the message prefix for messages sent from this plugin. Currently {0}" + oldPrefix);
                return true;
            }

            plugin.getSettings().setMessagePrefix(args[1]);
            send(sender, PREFIX_CHANGED, oldPrefix, args[1]);
            log(Level.INFO, sender.getName() + " changed message prefix from " + oldPrefix + " to " + args[1]);
            return true;
        }

        if (args[0].equalsIgnoreCase("ListingFee")) {
            double oldListingFee = plugin.getSettings().getListingFee();

            if (args.length < 2) {
                send(sender, PARAM_LISTING_FEE_HELP_1, oldListingFee);
                send(sender, PARAM_LISTING_FEE_HELP_2, oldListingFee);
                send(sender, PARAM_LISTING_FEE_HELP_3, oldListingFee);
                send(sender, PARAM_LISTING_FEE_HELP_4, oldListingFee);
                return true;
            }

            double listingFee = Double.parseDouble(args[1]);

            if (listingFee < 0.0) {
                send(sender, NEGATIVE_LISTING_FEE);
                return true;
            }

            if (listingFee > 50.0) {
                send(sender, EXCESSIVE_LISTING_FEE);
                return true;
            }

            plugin.getSettings().setListingFee(listingFee);
            send(sender, LISTING_FEE_CHANGED, oldListingFee, listingFee);
            plugin.log(Level.INFO, "{0} changed listing fee from {1} to {2}", sender.getName(), oldListingFee, listingFee);
            return true;
        }

        if (args[0].equalsIgnoreCase("SalesFee")) {
            double oldSalesFee = plugin.getSettings().getSalesFee();

            if (args.length < 2) {
                send(sender, PARAM_SALES_FEE_HELP_1, oldSalesFee);
                send(sender, PARAM_SALES_FEE_HELP_2, oldSalesFee);
                send(sender, PARAM_SALES_FEE_HELP_3, oldSalesFee);
                send(sender, PARAM_SALES_FEE_HELP_4, oldSalesFee);
                send(sender, "&eChanges the percentage fee charged to the buyer of items for sale.");
                send(sender, "&eMust be > 0.0 and <= 75.0, currently {0}" + oldSalesFee);
                return true;
            }

            double salesFee = Double.parseDouble(args[1]);

            plugin.log(Level.INFO, "oldSalesFee={0} arg={1} newFee={2}", oldSalesFee, args[1], salesFee);

            if (salesFee < 0.0) {
                send(sender, NEGATIVE_SALES_FEE);
                return true;
            }

            if (salesFee > 75.0) {
                send(sender, EXCESSIVE_SALES_FEE);
                return true;
            }

            plugin.getSettings().setSalesFee(salesFee);
            send(sender, SALES_FEE_CHANGED, oldSalesFee, salesFee);
            log(Level.INFO, sender.getName() + " changed sales fee from " + oldSalesFee + " to " + salesFee);
            return true;
        }

        if (args[0].equalsIgnoreCase("MaxChestCount")) {
            int oldCount = plugin.getSettings().getMaxChestCount();

            if (args.length < 2) {
                send(sender, PARAM_MAX_CHESTS_HELP_1, oldCount);
                send(sender, PARAM_MAX_CHESTS_HELP_2, oldCount);
                send(sender, PARAM_MAX_CHESTS_HELP_3, oldCount);
                send(sender, PARAM_MAX_CHESTS_HELP_4, oldCount);
                send(sender, PARAM_MAX_CHESTS_HELP_5, oldCount);
                send(sender, PARAM_MAX_CHESTS_HELP_6, oldCount);
                send(sender, PARAM_MAX_CHESTS_HELP_7, oldCount);
                send(sender, PARAM_MAX_CHESTS_HELP_8, oldCount);
                send(sender, PARAM_MAX_CHESTS_HELP_9, oldCount);
                return true;
            }

            int chestCount = Integer.parseInt(args[1]);

            if (chestCount < 0) {
                send(sender, CHEST_COUNT_INVALID);
                return true;
            }

            plugin.getSettings().setMaxChestCount(chestCount);

            send(sender, CHEST_COUNT_CHANGED, oldCount, chestCount);
            log(Level.INFO, sender.getName() + " changed chest count from " + oldCount + " to " + chestCount);

            if (chestCount > 0) {
                send(sender, CHEST_STACK_LIMIT, chestCount * 27);
            } else {
                send(sender, CHEST_STACK_NO_LIMIT);
            }

            return true;
        }

        send(sender, CMD_CONFIG_BAD_PARAM_1);
        send(sender, CMD_CONFIG_BAD_PARAM_2);
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
