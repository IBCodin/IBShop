package io.github.ibcodin.ibshop.commands;

import io.github.ibcodin.ibshop.CommandHandler;
import io.github.ibcodin.ibshop.IBShop;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static io.github.ibcodin.ibshop.MessageLookup.IBShopMessages.*;

public class CommandStock extends CommandHandler {
    public static final String CommandName = "ibshopstock";

    public CommandStock(IBShop plugin) {
        super(plugin, CommandName);
    }

    @Override
    public void sendHelp(CommandSender sender, String label) {
        if (senderHasPermission(sender)) {
            sendMessage(sender,"/" + label + " [page]");
            sendMessage(sender, ChatColor.YELLOW + "  Show the items you have for sale");
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (! (sender instanceof Player)) {
            sendMessage(sender, MSG_NOT_PLAYER);
            return true;
        }

        Player player = (Player) sender;

        int page = 1;
        if (args.length > 0) {
            page = Integer.parseInt(args[0]);
        }

        return plugin.getSalesList().showSenderItemsForSale(player, page);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
