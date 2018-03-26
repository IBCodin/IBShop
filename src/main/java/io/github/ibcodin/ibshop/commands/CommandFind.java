package io.github.ibcodin.ibshop.commands;

import io.github.ibcodin.ibshop.IBShop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static io.github.ibcodin.ibshop.IBShopMessages.*;

public class CommandFind extends CommandHandler {
    public static final String CommandName = "ibshopfind";

    public CommandFind(IBShop plugin) {
        super(plugin, CommandName);
    }

    @Override
    public void sendHelp(CommandSender sender, String label, boolean detailHelp) {
        if (!senderHasPermission(sender)) return;

        send(sender, CMD_FIND_HELP_1, label);
        send(sender, CMD_FIND_HELP_2);
        send(sender, CMD_FIND_HELP_3);
        send(sender, CMD_FIND_HELP_4);

        if (!detailHelp) return;
        send(sender, CMD_FIND_DETAIL_HELP_1);
        send(sender, CMD_FIND_DETAIL_HELP_2);
        send(sender, CMD_FIND_DETAIL_HELP_3);
        send(sender, CMD_FIND_DETAIL_HELP_4);
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
                send(sender, NOT_MATERIAL, itemName);
                return true;
            }

//            log(Level.INFO, "The item " + itemName + " matched " + findMat.getType().name());

            if (!plugin.getBlackList().canSellItem(findMat)) {
                send(sender, NOT_WHITELIST, itemName);
                return true;
            }

            String preferredName = plugin.getItemLookup().preferredName(findMat);

            send(sender, FIND_LISTING, preferredName);

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
