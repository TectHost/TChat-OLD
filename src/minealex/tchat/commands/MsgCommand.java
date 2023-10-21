package minealex.tchat.commands;

import java.io.FileReader;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import minealex.tchat.TChat;

public class MsgCommand implements CommandExecutor {
    private final TChat plugin;

    public MsgCommand(TChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
        	Player player = (Player) sender;
            if (sender.hasPermission("tchat.msg")) {
                if (args.length >= 2) {
                    Player recipient = sender.getServer().getPlayer(args[0]);
                    if (recipient != null) {
                        String message = String.join(" ", args).substring(args[0].length() + 1);
                        
                        plugin.updateLastConversationalist(((Player) sender).getUniqueId(), recipient.getUniqueId());
                        
                        List<String> ignoredPlayers = plugin.getConfig().getStringList("ignore");

                        if (ignoredPlayers.contains(recipient.getName())) {
                            String ignoredMessage = getConfiguredFormat("cannotMessageIgnored");
                            ignoredMessage = ignoredMessage.replace("%player%", recipient.getName());
                            player.sendMessage(ChatColor.RED + ignoredMessage);
                            return true;
                        }

                        String msgSentFormat = getConfiguredFormat("msgSent");
                        String msgReceivedFormat = getConfiguredFormat("msgReceived");

                        String formattedMessageSent = msgSentFormat
                                .replace("<sender>", sender.getName())
                                .replace("<recipient>", recipient.getName())
                                .replace("<message>", message);

                        String formattedMessageReceived = msgReceivedFormat
                                .replace("<sender>", sender.getName())
                                .replace("<recipient>", recipient.getName())
                                .replace("<message>", message);

                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', formattedMessageSent));
                        recipient.sendMessage(ChatColor.translateAlternateColorCodes('&', formattedMessageReceived));
                    } else {
                        sender.sendMessage(getConfiguredFormat("noPlayerOnline"));
                    }
                } else {
                    sender.sendMessage(getConfiguredFormat("incorrectUsage"));
                }
            } else {
                sender.sendMessage(getConfiguredFormat("noPermission"));
            }
        } else {
            sender.sendMessage(getConfiguredFormat("playersOnly"));
        }
        return true;
    }

    private String getConfiguredFormat(String formatKey) {
        try {
            String filePath = plugin.getDataFolder().getPath() + "/format_config.json";
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(filePath));
            JSONObject jsonObject = (JSONObject) obj;

            JSONObject msgFormats = (JSONObject) jsonObject.get("msgFormats");
            JSONObject messages = (JSONObject) jsonObject.get("messages");

            if (msgFormats.containsKey(formatKey)) {
                return ChatColor.translateAlternateColorCodes('&', (String) msgFormats.get(formatKey));
            } else if (messages.containsKey(formatKey)) {
                return ChatColor.translateAlternateColorCodes('&', (String) messages.get(formatKey));
            } else {
                return "<sender> whispers to <recipient>: <message>";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "<sender> whispers to <recipient>: <message>";
        }
    }
}
