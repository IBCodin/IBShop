package io.github.ibcodin.ibshop.commands;

import io.github.ibcodin.ibshop.CommandHandler;
import io.github.ibcodin.ibshop.IBShop;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

import static io.github.ibcodin.ibshop.MessageLookup.IBShopMessages.*;

public class CommandList extends CommandHandler {

    public static final String CommandName = "ibshoplist";

    public CommandList(IBShop plugin) {
        super(plugin, CommandName);
    }

    @Override
    public void sendHelp(CommandSender sender, String label) {
        if (senderHasPermission(sender)) {
            sendMessage(sender, "/" + label + " [item | page]");
            sendMessage(sender, ChatColor.YELLOW + "  List sales by item match or page");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        int page = 1;
        if (args.length == 0) {
            return plugin.getSalesList().showAllSalesSummary(sender, page);
        }

        if (args.length == 1) {
            try {
                page = Integer.parseInt(args[0]);
                return plugin.getSalesList().showAllSalesSummary(sender, page);
            }
            catch (NumberFormatException ee) {
                List<String> preferredNames = plugin.getItemLookup().matchString(args[0]);
                return plugin.getSalesList().showSelectedSalesSummary(sender, preferredNames, page);
            }
        }

        if (args.length == 2) {
            try {
                List<String> preferredNames = plugin.getItemLookup().matchString(args[0]);
                page = Integer.parseInt(args[1]);
                return plugin.getSalesList().showSelectedSalesSummary(sender, preferredNames, page);
            }
            catch (NumberFormatException ee) {
                sendMessage(sender, MSG_INVALID_PAGE_NUMBER, args[1]);
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
