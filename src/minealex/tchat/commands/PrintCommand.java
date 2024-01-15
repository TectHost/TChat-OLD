package minealex.tchat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import minealex.tchat.TChat;

public class PrintCommand implements CommandExecutor {

    private final TChat plugin;

    public PrintCommand(TChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the command sender has the required permission
        if (!sender.hasPermission("tchat.print")) {
        	sender.sendMessage(plugin.getMessagesYML("messages.noPermission"));
            return true;
        }

        // Check if there is at least one argument (the message to print)
        if (args.length == 0) {
        	sender.sendMessage(plugin.getMessagesYML("messages.printUsage"));
            return true;
        }

        // Concatenate the arguments to form the message
        String message = String.join(" ", args);

        // Send the raw message to all players
        for (Player player : sender.getServer().getOnlinePlayers()) {
            player.sendMessage(message);
        }

        return true;
    }
}
