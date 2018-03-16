package io.github.ibcodin.ibshop.commands;

import io.github.ibcodin.ibshop.IBShop;
import org.bukkit.command.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandBase extends CommandHandler {

    public static final String CommandName = "ibshop";

    public CommandBase(IBShop plugin) {
        super(plugin, CommandName);
    }

    @Override
    public void sendHelp(CommandSender sender, String label, final boolean detailHelp) {
        sendMessage(sender, "General Interface");

        // Ask each child command to send their help
        for (AlternateCommand kid : kids) {
            kid.sendHelp(sender, label, detailHelp);
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // TODO: implement base command
        // no args is 'help'
        // otherwise first arg is target command

        if (args.length < 1) {
            sendHelp(sender, label, false);
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

        if (args.length < 1 || args[0].equals("")) {
            List<String> subs = new ArrayList<>();
            for (AlternateCommand kid : kids) {
                if (kid.canCommand(sender)) {
                    subs.add(kid.getExtra());
                }
            }
            return subs;
        }

        if (args.length > 1) {
            // Hand off tab complete to the appropriate kid
            for (AlternateCommand kid : kids) {
                if (kid.wantsCommand(sender, args[0])) {
                    return kid.getTabComplete(sender, label, args);
                }
            }
            return null;
        }

        // Only one, non-empty arg, build list of matching kids
        List<String> subs = new ArrayList<>();
        for (AlternateCommand kid : kids) {
            if (kid.canCommand(sender) && kid.getExtra().startsWith(args[0].toLowerCase())) {
                subs.add(kid.getExtra());
            }
        }
        Collections.sort(subs);
        return subs;
    }
}
