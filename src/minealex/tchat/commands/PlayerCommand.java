package minealex.tchat.commands;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

public class PlayerCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public PlayerCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tchat.player")) {
            sender.sendMessage(getMessage("messages.noPermission"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(getMessage("messages.incorrectUsagePlayer"));
            return false;
        }

        String playerName = args[0];
        Player targetPlayer = Bukkit.getPlayerExact(playerName);

        if (targetPlayer == null) {
            sender.sendMessage(getMessage("messages.noPlayerOnline"));
            return true;
        }

        try {
            File configFile = new File(plugin.getDataFolder(), "messages.yml");

            if (configFile.exists()) {
                Yaml yaml = new Yaml();
                Iterable<Object> rulesArray = yaml.loadAll(new FileReader(configFile));

                // Enviar cada regla con placeholders
                for (Object ruleObj : rulesArray) {
                    if (ruleObj instanceof String) {
                        String rule = (String) ruleObj;
                        String formattedRule = formatPlaceholder(rule, targetPlayer);
                        String coloredRule = ChatColor.translateAlternateColorCodes('&', formattedRule);
                        sender.sendMessage(coloredRule);
                    }
                }
            } else {
                sender.sendMessage("File 'messages.yml' was not found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private String formatPlaceholder(String rule, Player targetPlayer) {
        InetAddress address = targetPlayer.getAddress().getAddress();
        String ipAddress = address.getHostAddress();
        UUID playerUUID = targetPlayer.getUniqueId();

        rule = rule.replace("%ip%", ipAddress);
        rule = rule.replace("%uuid%", playerUUID.toString());
        rule = rule.replace("%player%", targetPlayer.getName());
        rule = PlaceholderAPI.setPlaceholders(targetPlayer, rule);

        return rule;
    }

    @SuppressWarnings("unchecked")
	private String getMessage(String key) {
        File configFile = new File(plugin.getDataFolder(), "messages.yml");

        try {
            if (configFile.exists()) {
                Yaml yaml = new Yaml();
                Iterable<Object> messages = yaml.loadAll(new FileReader(configFile));

                for (Object messageObj : messages) {
                    if (messageObj instanceof Map) {
                        Map<String, String> messageMap = (Map<String, String>) messageObj;
                        if (messageMap.containsKey(key)) {
                            return ChatColor.translateAlternateColorCodes('&', messageMap.get(key));
                        }
                    }
                }

                return "Message key '" + key + "' not found in 'messages.yml'.";
            } else {
                return "File 'messages.yml' was not found.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred while fetching the message.";
        }
    }
}
