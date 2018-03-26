package io.github.ibcodin.ibshop.commands;

import io.github.ibcodin.ibshop.IBShop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

import static io.github.ibcodin.ibshop.IBShopMessages.*;

public class CommandList extends CommandHandler {

    public static final String CommandName = "ibshoplist";

    public CommandList(IBShop plugin) {
        super(plugin, CommandName);
    }

    @Override
    public void sendHelp(CommandSender sender, String label, boolean detailHelp) {
        if (senderHasPermission(sender)) {
            send(sender, CMD_LIST_HELP_1, label );
            send(sender, CMD_LIST_HELP_2);
            send(sender, CMD_LIST_HELP_3);

            if (!detailHelp) return;

            send(sender, CMD_LIST_DETAIL_HELP_1);
            send(sender, CMD_LIST_DETAIL_HELP_2);
            send(sender, CMD_LIST_DETAIL_HELP_3);
            send(sender, CMD_LIST_DETAIL_HELP_4);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if ((args.length == 1) && (args[0].equals("?"))) {
            sendHelp(sender, label, true);
            return true;
        }

        int page = 1;
        if (args.length == 0) {
            return plugin.getSalesList().showAllSalesSummary(sender, page);
        }

        if (args.length == 1) {
            try {
                page = Integer.parseInt(args[0]);
                return plugin.getSalesList().showAllSalesSummary(sender, page);
            } catch (NumberFormatException ee) {
                List<String> preferredNames = plugin.getItemLookup().matchString(args[0]);
                return plugin.getSalesList().showSelectedSalesSummary(sender, preferredNames, page);
            }
        }

        if (args.length == 2) {
            try {
                List<String> preferredNames = plugin.getItemLookup().matchString(args[0]);
                page = Integer.parseInt(args[1]);
                return plugin.getSalesList().showSelectedSalesSummary(sender, preferredNames, page);
            } catch (NumberFormatException ee) {
                send(sender, INVALID_PAGE_NUMBER, args[1]);
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        return null;
    }
}
