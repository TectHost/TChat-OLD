package minealex.tchat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import minealex.tchat.TChat;

import java.io.FileReader;
import java.util.UUID;

public class ReplyCommand implements CommandExecutor {
    private final TChat plugin;

    public ReplyCommand(TChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (sender.hasPermission("tchat.reply")) {
                Player player = (Player) sender;

                UUID lastMessageSender = plugin.getLastConversationalist(player.getUniqueId());
                if (lastMessageSender != null) {
                    Player recipient = plugin.getServer().getPlayer(lastMessageSender);

                    if (recipient != null) {
                        if (args.length > 0) {
                            String message = String.join(" ", args);

                            String replySentFormat = getConfiguredFormat("replySent");
                            String replyReceivedFormat = getConfiguredFormat("replyReceived");

                            String formattedMessageSent = replySentFormat
                                    .replace("<sender>", player.getName())
                                    .replace("<recipient>", recipient.getName())
                                    .replace("<message>", message);

                            String formattedMessageReceived = replyReceivedFormat
                                    .replace("<sender>", player.getName())
                                    .replace("<recipient>", recipient.getName())
                                    .replace("<message>", message);

                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', formattedMessageSent));
                            recipient.sendMessage(ChatColor.translateAlternateColorCodes('&', formattedMessageReceived));
                        } else {
                            player.sendMessage(getConfiguredFormat("incorrectUsage"));
                        }
                    } else {
                        player.sendMessage(getConfiguredFormat("noPlayerOnline"));
                    }
                } else {
                    player.sendMessage(getConfiguredFormat("noLastConversationalist"));
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

            JSONObject replyFormats = (JSONObject) jsonObject.get("replyFormats");
            JSONObject messages = (JSONObject) jsonObject.get("messages");

            if (replyFormats.containsKey(formatKey)) {
                return ChatColor.translateAlternateColorCodes('&', (String) replyFormats.get(formatKey));
            } else if (messages.containsKey(formatKey)) {
                return ChatColor.translateAlternateColorCodes('&', (String) messages.get(formatKey));
            } else {
                return "<sender> replies to <recipient>: <message>";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "<sender> replies to <recipient>: <message>";
        }
    }
}
