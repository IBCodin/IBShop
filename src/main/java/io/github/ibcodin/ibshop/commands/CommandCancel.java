package io.github.ibcodin.ibshop.commands;

import io.github.ibcodin.ibshop.CommandHandler;
import io.github.ibcodin.ibshop.IBShop;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static io.github.ibcodin.ibshop.MessageLookup.IBShopMessages.*;

public class CommandCancel extends CommandHandler {
    public static final String CommandName = "ibshopcancel";

    public CommandCancel(IBShop plugin) {
        super(plugin, CommandName);
    }

    @Override
    public void sendHelp(CommandSender sender, String label) {
        if (senderHasPermission(sender)) {
            sendMessage(sender,"/" + label + " item quantity");
            sendMessage(sender, ChatColor.YELLOW + "  Return items from your active sales");
        }
    }


//    ibshopcancel:
//    description: Cancel items from a previous listing.
//    usage: /<command> item_name quantity
//    aliases: [ibscancel, cancel]
//    permission: ibshop.cancel

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (! (sender instanceof Player)) {
            sendMessage(sender, MSG_NOT_PLAYER);
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 2) {
            sendMessage(player, MSG_BAD_CANCEL_ARGS);
            sendMessage(player, MSG_CANCEL_USAGE, label);
            return true;
        }

        String itemName = args[0];
        String strQty = args[1];

        try {
            int itemQty = Integer.parseInt(strQty);

            ItemStack findMat = null;

            findMat = plugin.getItemLookup().get(itemName, itemQty);

            if (findMat == null) {
                sendMessage(player, MSG_NOT_MATERIAL, itemName);
                return true;
            }

            if (!plugin.getBlackList().onWhiteList(findMat)) {
                sendMessage(player, MSG_NOT_WHITELIST, itemName);
                return true;
            }

            return plugin.getSalesList().cancelSale(player, findMat, itemName, itemQty);

        } catch (Exception ee) {
            ee.printStackTrace();
        }

        // TODO: implement command
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
