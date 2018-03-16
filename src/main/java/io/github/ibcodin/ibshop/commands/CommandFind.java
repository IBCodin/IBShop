package io.github.ibcodin.ibshop.commands;

import io.github.ibcodin.ibshop.IBShop;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static io.github.ibcodin.ibshop.MessageLookup.IBShopMessages.*;

public class CommandFind extends CommandHandler {
    public static final String CommandName = "ibshopfind";

    public CommandFind(IBShop plugin) {
        super(plugin, CommandName);
    }

    @Override
    public void sendHelp(CommandSender sender, String label, boolean detailHelp) {
        if (!senderHasPermission(sender)) return;

        sendMessage(sender, "/" + label + " item");
        sendMessage(sender, ChatColor.YELLOW + "  Find sales of the item");

        if (!detailHelp) return;

        sendMessage(sender, ChatColor.YELLOW + "This will try to match your text to a specific item");
        sendMessage(sender, ChatColor.YELLOW + "and show the details for any sale listing that item");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // item

        if (args.length == 0) {
            sendHelp(sender, label, true);
            return true;
        }

        String itemName = args[0];
        int itemPage = (args.length > 1) ? Integer.parseInt(args[1]) : 1;

        try {
            ItemStack findMat = plugin.getItemLookup().get(itemName);

            if (findMat == null) {
                sendMessage(sender, MSG_NOT_MATERIAL, itemName);
                return true;
            }

//            log(Level.INFO, "The item " + itemName + " matched " + findMat.getType().name());

            if (!plugin.getBlackList().onWhiteList(findMat)) {
                sendMessage(sender, MSG_NOT_WHITELIST, itemName.toString());
                return true;
            }

            String preferredName = plugin.getItemLookup().preferredName(findMat);

            sendMessage(sender, MSG_FIND_LISTING, preferredName);

            return plugin.getSalesList().showSalesDetailsByItemName(sender, preferredName, itemPage);

        } catch (Exception ee) {
            ee.printStackTrace();
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
