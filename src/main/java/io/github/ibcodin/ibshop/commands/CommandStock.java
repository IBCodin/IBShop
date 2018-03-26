package io.github.ibcodin.ibshop.commands;

import io.github.ibcodin.ibshop.IBShop;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static io.github.ibcodin.ibshop.IBShopMessages.*;

public class CommandStock extends CommandHandler {
    public static final String CommandName = "ibshopstock";

    public CommandStock(IBShop plugin) {
        super(plugin, CommandName);
    }

    @Override
    public void sendHelp(CommandSender sender, String label, boolean detailHelp) {
        if (!senderHasPermission(sender)) return;

        send(sender, CMD_STOCK_HELP_1, label);
        send(sender, CMD_STOCK_HELP_2);
        send(sender, CMD_STOCK_HELP_3);

//        if (!detailHelp) return;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            send(sender, NOT_PLAYER, label);
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
