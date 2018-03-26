package io.github.ibcodin.ibshop.commands;

import io.github.ibcodin.ibshop.IBShop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static io.github.ibcodin.ibshop.IBShopMessages.*;

public class CommandBuy extends CommandHandler {

    public static final String CommandName = "ibshopbuy";

    public CommandBuy(IBShop plugin) {
        super(plugin, CommandName);
    }

    @Override
    public void sendHelp(CommandSender sender, String label, boolean detailHelp) {
        if (senderHasPermission(sender)) {
            send(sender, CMD_BUY_HELP_1,  label );
            send(sender, CMD_BUY_HELP_2);
            send(sender, CMD_BUY_HELP_3);
            send(sender, CMD_BUY_HELP_4);
            send(sender, CMD_BUY_HELP_5);
            send(sender, CMD_BUY_HELP_6);
            send(sender, CMD_BUY_HELP_7);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            send(sender, NOT_PLAYER, label);
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 3) {
            sendHelp(sender, label, true);
            return true;
        }

        String itemName = args[0];
        String strQty = args[1];
        String strEach = args[2];

        try {
            int itemQty = Integer.parseInt(strQty);
            double itemEach = Double.parseDouble(strEach);

            ItemStack findMat = plugin.getItemLookup().get(itemName);

            if (findMat == null) {
                send(player, NOT_MATERIAL, itemName);
                return true;
            }

            if (!plugin.getBlackList().canSellItem(findMat)) {
                send(player, NOT_WHITELIST, itemName);
                return true;
            }

            return plugin.getSalesList().buyItems(player, findMat, itemName, itemQty, itemEach);

        } catch (Exception ee) {
            ee.printStackTrace();
        }

        send(sender, CMD_BUY_FAIL);

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        if ((args.length < 1) || args[0].equals("")) {
            // Build the list of buyable items
            return plugin.getSalesList().getSalesItemNames();
        }

        if (args.length == 1) {
            String lowerArg = args[0].toLowerCase();
            List<String> returnList = new ArrayList<>();
            for (String name : plugin.getSalesList().getSalesItemNames()) {
                if (name.toLowerCase().startsWith(lowerArg)) {
                    returnList.add(name);
                }
            }
            return returnList;
        }

        return null;
    }
}
