package minealex.tchat.commands;

import minealex.tchat.TChat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClearChatCommand implements CommandExecutor {

    private static final int CLEAR_CHAT_LINES = 100;
    private final TChat plugin;

    public ClearChatCommand(TChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	Player player = (Player) sender;
        // Verify if the command was executed by a player
        if (!(sender instanceof Player)) {
        	String onlyPlayer = plugin.getMessage("onlyPlayer");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', onlyPlayer));
            return true;
        }

        // Verify if the player has the required permission to clear the chat
        if (!player.hasPermission("tchat.admin.chatclear")) {
        	String noPermission = plugin.getMessage("noPermission");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermission));
            return true;
        }

        // Send empty messages to clear the chat
        for (int i = 0; i < CLEAR_CHAT_LINES; i++) {
            player.sendMessage("");
        }

        // Notify that the chat has been cleared
        String clearMessage = plugin.getMessage("clearChatMessage");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', clearMessage));

        // Notify success
        String successMessage = plugin.getMessage("clearChatSuccess");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', successMessage));

        return true;
    }
}
