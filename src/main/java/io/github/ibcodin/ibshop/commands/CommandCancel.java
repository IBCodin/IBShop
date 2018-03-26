package io.github.ibcodin.ibshop.commands;

import io.github.ibcodin.ibshop.IBShop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static io.github.ibcodin.ibshop.IBShopMessages.*;

public class CommandCancel extends CommandHandler {
    public static final String CommandName = "ibshopcancel";

    public CommandCancel(IBShop plugin) {
        super(plugin, CommandName);
    }

    @Override
    public void sendHelp(CommandSender sender, String label, boolean detailHelp) {
        if (!senderHasPermission(sender)) return;

        send(sender, CMD_CANCEL_HELP_1, label);
        send(sender, CMD_CANCEL_HELP_2);
        send(sender, CMD_CANCEL_HELP_3);
        send(sender, CMD_CANCEL_HELP_4);

        if (!detailHelp) return;

        send(sender, CMD_CANCEL_DETAIL_HELP_1);
        send(sender, CMD_CANCEL_DETAIL_HELP_2);
        send(sender, CMD_CANCEL_DETAIL_HELP_3);
        send(sender, CMD_CANCEL_DETAIL_HELP_4);
    }


//    ibshopcancel:
//    description: Cancel items from a previous listing.
//    usage: /<command> item_name quantity
//    aliases: [ibscancel, cancel]
//    permission: ibshop.cancel

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length != 2) {
            sendHelp(sender, label, true);
            return true;
        }

        if (!(sender instanceof Player)) {
            send(sender, NOT_PLAYER, label);
            return true;
        }

        Player player = (Player) sender;

        String itemName = args[0];
        String strQty = args[1];

        try {
            int itemQty = Integer.parseInt(strQty);

            ItemStack findMat = null;

            findMat = plugin.getItemLookup().get(itemName, itemQty);

            if (findMat == null) {
                send(player, NOT_MATERIAL, itemName);
                return true;
            }

            if (!plugin.getBlackList().canSellItem(findMat)) {
                send(player, NOT_WHITELIST, itemName);
                return true;
            }

            return plugin.getSalesList().cancelSale(player, findMat, itemName, itemQty);

        } catch (Exception ee) {
            ee.printStackTrace();
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {

        if (!(commandSender instanceof Player))
            return null;

        Player player = (Player) commandSender;

        if ((args.length < 1) || args[0].equals("")) {
            // Build the list of your items
            return plugin.getSalesList().getStockNames(player);
        }

        if (args.length == 1) {
            String lowerArg = args[0].toLowerCase();
            List<String> returnList = new ArrayList<>();
            for (String name : plugin.getSalesList().getStockNames(player)) {
                if (name.toLowerCase().startsWith(lowerArg)) {
                    returnList.add(name);
                }
            }
            return returnList;
        }

        return null;
    }
}
