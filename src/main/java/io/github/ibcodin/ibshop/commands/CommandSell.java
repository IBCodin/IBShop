package io.github.ibcodin.ibshop.commands;

import io.github.ibcodin.ibshop.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static io.github.ibcodin.ibshop.MessageLookup.IBShopMessages.*;

public class CommandSell extends CommandHandler {
    public static final String CommandName = "ibshopsell";

    public CommandSell(IBShop plugin) {
        super(plugin, CommandName);
    }

    @Override
    public void sendHelp(CommandSender sender, String label) {
        if (senderHasPermission(sender)) {
            sendMessage(sender,"/" + label + " item quantity [each_price]");
            sendMessage(sender, ChatColor.YELLOW + "  Place quantity items up for sale at the each_price");
            sendMessage(sender,ChatColor.YELLOW + "  The listing fee is paid when you list the sale");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // TODO: implement command
        // sell item quantity each_price

        if (! (sender instanceof Player)) {
            sendMessage(sender, MSG_NOT_PLAYER);
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2 || args.length > 3) {
            sendMessage(player, MSG_BAD_SELL_ARGS);
            sendMessage(player, MSG_SELL_USAGE, label);
            return true;
        }

        String itemName = args[0];
        String strQty = args[1];
        String strEach = (args.length > 2) ? args[2] : "";

        try {
            int itemQty = Integer.parseInt(strQty);
            double itemEach = (args.length > 2) ? Double.parseDouble(strEach) : 0.0;

            ItemStack findMat = null;
            if (itemName.equalsIgnoreCase("hand")) {
                final ItemStack handStack = player.getInventory().getItemInMainHand();
                findMat = new ItemStack(handStack.getType(), itemQty, handStack.getDurability());
                itemName = plugin.getItemLookup().preferredName(findMat);
            } else {
                findMat = plugin.getItemLookup().get(itemName, itemQty);
            }

            if (findMat == null) {
                sendMessage(player, MSG_NOT_MATERIAL, itemName);
                return true;
            }

            if (!plugin.getBlackList().onWhiteList(findMat)) {
                sendMessage(player, MSG_NOT_WHITELIST, itemName);
                return true;
            }

//            if (!player.getInventory().containsAtLeast(findMat, itemQty)) {
//                messageLookup.sendMessage(player, MSG_TOO_FEW_ITEMS, itemQty, itemName);
//                return true;
//            }

            return plugin.getSalesList().addSalesListing(player, findMat, itemName, itemQty, itemEach);

        } catch (Exception ee) {
            ee.printStackTrace();
        }

        sender.sendMessage("Sale Failed");

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
