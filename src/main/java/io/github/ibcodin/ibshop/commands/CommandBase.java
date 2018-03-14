package io.github.ibcodin.ibshop.commands;

import io.github.ibcodin.ibshop.CommandHandler;
import io.github.ibcodin.ibshop.IBShop;
import org.bukkit.ChatColor;
import org.bukkit.command.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandBase extends CommandHandler {

    public static final String CommandName = "ibshop";

    public CommandBase(IBShop plugin) {
        super(plugin, CommandName);
    }

    @Override
    public void sendHelp(CommandSender sender, String label) {
        sendMessage(sender, "General Interface");

        // Ask each child command to send their help
        for (AlternateCommand kid : kids) {
            kid.sendHelp(sender, label);
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // TODO: implement base command
        // no args is 'help'
        // otherwise first arg is target command

        if (args.length < 1) {
            sendHelp(sender, label);
            return true;
        }

        for (AlternateCommand kid : kids){
            if (kid.wantsCommand(sender, args[0])) {
                return kid.sendCommand(sender, label, args);
            }
        }

        sendMessage(sender,"unrecognized sub command " + args[0]);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {


        if (args.length < 1) {
            List<String> subs = new ArrayList<>();
            for (AlternateCommand kid : kids) {
                if (kid.canCommand(sender)) {
                    subs.add(kid.getExtra());
                }
            }
            return subs;
        }

        if (args.length > 0) {
            // Check for full match
            for (AlternateCommand kid : kids) {
                if (kid.wantsCommand(sender, args[0])) {
                    return kid.getTabComplete(sender, label, args);
                }
            }

            // Return list of partial matches
            List<String> subs = new ArrayList<>();
            for (AlternateCommand kid : kids) {
                if (kid.canCommand(sender) && kid.getExtra().startsWith(args[0].toLowerCase())) {
                    subs.add(kid.getExtra());
                }
            }
            return subs;
        }
        return null;
    }

}
