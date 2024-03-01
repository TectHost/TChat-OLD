package minealex.tchat.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import minealex.tchat.TChat;

public class StaffChatCommand implements CommandExecutor {

    private final TChat plugin;
    private String staffChatFormat;
    private String staffChatJoinMessage;
    private String staffChatLeaveMessage;

    public StaffChatCommand(TChat plugin) {
        this.plugin = plugin;
        loadConfig(); // Asegúrate de cargar la configuración cuando se instancia el comando
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("tchat.staffchat")) {
                loadConfig(); // Recarga la configuración para asegurarte de que las variables estén actualizadas

                if (plugin.getStaffChatPlayers().contains(player.getUniqueId())) {
                    removePlayerFromStaffChat(player);
                    String message = String.join(" ", args);

                    // Verifica que las variables no sean nulas antes de usarlas
                    if (staffChatFormat != null) {
                        for (Player staffPlayer : Bukkit.getOnlinePlayers()) {
                            if (staffPlayer.hasPermission("tchat.staffchat")) {
                                staffPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', staffChatFormat
                                        .replace("%player%", player.getName())
                                        .replace("%message%", message)
                                ));
                            }
                        }
                    }
                    return true;
                } else {
                    plugin.addPlayerToStaffChat(player);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', staffChatJoinMessage));
                    return true;
                }
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getOnlyPlayer()));
        }
        return false;
    }

    public void removePlayerFromStaffChat(Player player) {
        if (plugin.getStaffChatPlayers().contains(player.getUniqueId())) {
            plugin.removePlayerFromStaffChat(player);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', staffChatLeaveMessage));
        }
    }

    private String getOnlyPlayer() {
        return ChatColor.translateAlternateColorCodes('&', "&cEste comando solo puede ser ejecutado por jugadores.");
    }

    private void loadConfig() {
        staffChatFormat = plugin.getConfig().getString("Staff.format");
        staffChatJoinMessage = plugin.getConfig().getString("Staff.staff_chat_join_message");
        staffChatLeaveMessage = plugin.getConfig().getString("Staff.staff_chat_leave_message");
    }
}
