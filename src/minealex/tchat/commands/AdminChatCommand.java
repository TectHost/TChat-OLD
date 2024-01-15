package minealex.tchat.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import minealex.tchat.TChat;

import java.io.File;
import java.util.UUID;

public class AdminChatCommand implements CommandExecutor {

    private final TChat plugin;
    private String adminChatFormat;
    private String adminChatJoinMessage;
    private String adminChatLeaveMessage;
    private File messagesFile;
    private FileConfiguration messagesConfig;

    public AdminChatCommand(TChat plugin) {
        this.plugin = plugin;
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        loadConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("tchat.adminchat")) {
                loadConfig(); // Reload the config to make sure variables are updated

                if (plugin.getStaffChatPlayers().contains(player.getUniqueId())) {
                    removePlayerFromAdminChat(player);
                    String message = String.join(" ", args);

                    // Check if variables are not null before using them
                    if (adminChatFormat != null) {
                        for (UUID uuid : plugin.getStaffChatPlayers()) {
                            Player adminPlayer = Bukkit.getPlayer(uuid);
                            if (adminPlayer != null && adminPlayer.isOnline()) {
                                adminPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', adminChatFormat
                                        .replace("%player%", player.getName())
                                        .replace("%message%", message)
                                ));
                            }
                        }
                    }
                    return true;
                } else {
                    plugin.addPlayerToStaffChat(player);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', adminChatJoinMessage));
                    return true;
                }
            }
        } else {
        	sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessages("messages.playersOnly")));
        }
        return false;
    }

    public void removePlayerFromAdminChat(Player player) {
        if (plugin.getStaffChatPlayers().contains(player.getUniqueId())) {
            plugin.removePlayerFromStaffChat(player);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', adminChatLeaveMessage));
        }
    }

    private String getMessages(String formatKey) {
        if (messagesConfig.contains(formatKey)) {
            return ChatColor.translateAlternateColorCodes('&', messagesConfig.getString(formatKey));
        } else {
            return "<sender> whispers to <recipient>: <message>";
        }
    }
    
    private void loadConfig() {
        adminChatFormat = plugin.getConfig().getString("Admin.format");
        adminChatJoinMessage = plugin.getConfig().getString("Admin.admin_chat_join_message");
        adminChatLeaveMessage = plugin.getConfig().getString("Admin.admin_chat_leave_message");
    }
}
