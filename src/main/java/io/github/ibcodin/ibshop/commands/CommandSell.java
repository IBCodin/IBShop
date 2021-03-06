package io.github.ibcodin.ibshop.commands;

import io.github.ibcodin.ibshop.IBShop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.ibcodin.ibshop.IBShopMessages.*;

public class CommandSell extends CommandHandler {
    public static final String CommandName = "ibshopsell";

    public CommandSell(IBShop plugin) {
        super(plugin, CommandName);
    }

    @Override
    public void sendHelp(CommandSender sender, String label, boolean detailHelp) {
        if (!senderHasPermission(sender))
            return;

        send(sender, CMD_SELL_HELP_1, label);
        send(sender, CMD_SELL_HELP_2);
        send(sender, CMD_SELL_HELP_3);
        send(sender, CMD_SELL_HELP_4);

        if (!detailHelp) return;

        send(sender, CMD_SELL_DETAIL_HELP_1);
        send(sender, CMD_SELL_DETAIL_HELP_2);
        send(sender, CMD_SELL_DETAIL_HELP_3);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // TODO: implement command
        // sell item quantity each_price

        if (!(sender instanceof Player)) {
            send(sender, NOT_PLAYER, label);
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2 || args.length > 3) {
            sendHelp(sender, label, true);
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
                send(player, NOT_MATERIAL, itemName);
                return true;
            }

            if (!plugin.getBlackList().canSellItem(findMat)) {
                send(player, NOT_WHITELIST, itemName);
                return true;
            }

            return plugin.getSalesList().addSalesListing(player, findMat, itemName, itemQty, itemEach);

        } catch (Exception ee) {
            ee.printStackTrace();
        }

        send(sender, CMD_SELL_FAIL);

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {

        if (!(commandSender instanceof Player))
            return null;

        Player player = (Player) commandSender;

        List<String> returnList = new ArrayList<>();

        if ((args.length < 1) || args[0].equals("")) {
            // Build the list of sellable items from the player inventory
            for (ItemStack stack : player.getInventory().getContents()) {
                if (plugin.getBlackList().canSellItem(stack)) {
                    String pname = plugin.getItemLookup().preferredName(stack);
                    if (!returnList.contains(pname)) {
                        returnList.add(pname);
                    }
                }
            }
            Collections.sort(returnList);
            return returnList;
        }

        // If they've started typing, the input is free-form

        return null;
    }
}
