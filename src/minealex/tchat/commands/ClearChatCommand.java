package minealex.tchat.commands;

import minealex.tchat.TChat;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class ClearChatCommand implements CommandExecutor {

    private static final int CLEAR_CHAT_LINES = 100;
    private File messagesFile;
    private FileConfiguration messagesConfig;

    public ClearChatCommand(TChat plugin) {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verify if the command was executed by a player
        if (!(sender instanceof Player)) {
        	sender.sendMessage(getMessages("messages.onlyPlayer"));
            return true;
        }

        Player player = (Player) sender; // Now it's safe to cast

        // Verify if the player has the required permission to clear the chat
        if (!player.hasPermission("tchat.admin.chatclear")) {
        	sender.sendMessage(getMessages("messages.noPermission"));
            return true;
        }

        // Send empty messages to clear the chat
        for (int i = 0; i < CLEAR_CHAT_LINES; i++) {
            player.sendMessage("");
        }

        // Notify that the chat has been cleared
        sender.sendMessage(getMessages("messages.clearChatMessage"));

        // Notify success
        sender.sendMessage(getMessages("messages.clearChatSuccess"));

        return true;
    }
    
    private String getMessages(String formatKey) {
        if (messagesConfig.contains(formatKey)) {
            return ChatColor.translateAlternateColorCodes('&', messagesConfig.getString(formatKey));
        } else {
            return "Invalid05";
        }
    }
}
