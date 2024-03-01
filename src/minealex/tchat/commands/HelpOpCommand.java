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

import me.clip.placeholderapi.PlaceholderAPI;

public class HelpOpCommand implements CommandExecutor {
    private final TChat plugin;
    private File messagesFile;
    private FileConfiguration messagesConfig;

    public HelpOpCommand(TChat plugin) {
        this.plugin = plugin;
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("tchat.helpop")) {
                if (args.length > 0) {
                    String message = String.join(" ", args);
                    String helpopFormat = plugin.getConfig().getString("Help.helpopFormat");
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
                    	player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessages("messages.helpopFormatError")));
                    }
                } else {
                	player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessages("messages.helpopUsage")));
                }
            } else {
            	player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessages("messages.noPermission")));
            }
        } else {
        	Player player = (Player) sender;
        	player.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessages("messages.onlyPlayer")));
        }
        return true;
    }
    
    private String getMessages(String formatKey) {
        if (messagesConfig.contains(formatKey)) {
            return ChatColor.translateAlternateColorCodes('&', messagesConfig.getString(formatKey));
        } else {
            return "<sender> whispers to <recipient>: <message>";
        }
    }
}
