package minealex.tchat.commands;

import minealex.tchat.TChat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;

public class HelpOpCommand implements CommandExecutor {
    private final TChat plugin;

    public HelpOpCommand(TChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("tchat.helpop")) {
                if (args.length > 0) {
                    String message = String.join(" ", args);
                    String helpopFormat = plugin.getConfiguredFormat("helpopFormat");
                    helpopFormat = PlaceholderAPI.setPlaceholders(player, helpopFormat);
                    helpopFormat = ChatColor.translateAlternateColorCodes('&', helpopFormat);

                    // Comprobar si el formato se cargó correctamente
                    if (helpopFormat != null) {
                        String formattedMessage = helpopFormat
                                .replace("<player>", player.getDisplayName())
                                .replace("<message>", message);

                        // Enviar mensaje a los jugadores con permiso tchat.helpop.receive
                        for (Player recipient : plugin.getServer().getOnlinePlayers()) {
                            if (recipient.hasPermission("tchat.helpop.receive")) {
                                recipient.sendMessage(formattedMessage);
                            }
                        }
                    } else {
                    	player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfiguredFormat("helpopFormatError")));
                    }
                } else {
                	player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfiguredFormat("helpopUsage")));
                }
            } else {
            	player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfiguredFormat("noPermission")));
            }
        } else {
        	Player player = (Player) sender;
        	player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfiguredFormat("onlyPlayer")));
        }
        return true;
    }
}
