package minealex.tchat.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import minealex.tchat.TChat;

public class Commands implements CommandExecutor {
    private final TChat plugin;

    public Commands(TChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("chat")) {
        	if (args.length == 0) {
            	List<String> customMessages = plugin.getMessagesYMLList("Chat-help.message");
                for (String message : customMessages) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                }
                return true;
            } else if (args[0].equalsIgnoreCase("reload")) {
                    if (!(sender instanceof Player) || sender.hasPermission("tchat.reload")) {
                        plugin.reloadFormatConfig();
                        plugin.getBannedWords().reloadBannedWordsList();
                        plugin.reloadWorldsConfig();
                        plugin.reloadBannedCommandsConfig();
                        plugin.reloadChatGamesConfig();
                        plugin.reloadChatBotConfig();
                        plugin.reloadAutoBroadcastConfig();
                        String reloadSuccessMessage = plugin.getMessagesYML("messages.reloadSuccess");
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', reloadSuccessMessage));
                    } else {
                        String noPermissionMessage = plugin.getMessagesYML("messages.noPermission");
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermissionMessage));
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("version")) {
                    if (!(sender instanceof Player) || sender.hasPermission("tchat.version")) {
                        String versionMessage = plugin.getMessagesYML("messages.versionMessage");
                        versionMessage = versionMessage.replace("{version}", plugin.getVersion());
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', versionMessage));
                    } else {
                        String noPermissionMessage = plugin.getMessagesYML("messages.noPermission");
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermissionMessage));
                    }
                    return true;
                }
            }
        return false;
    }
}
