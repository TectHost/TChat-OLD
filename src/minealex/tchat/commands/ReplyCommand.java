package minealex.tchat.commands;

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

public class ReplyCommand implements CommandExecutor {
    private final TChat plugin;
    private File messagesFile;
    private FileConfiguration messagesConfig;

    public ReplyCommand(TChat plugin) {
        this.plugin = plugin;
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
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

                            String replySentFormat = plugin.getConfig().getString("Reply.replySent");
                            String replyReceivedFormat = plugin.getConfig().getString("Reply.replyReceived");

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
                            player.sendMessage(getMessages("messages.incorrectUsage"));
                        }
                    } else {
                        player.sendMessage(getMessages("messages.noPlayerOnline"));
                    }
                } else {
                    player.sendMessage(getMessages("messages.noLastConversationalist"));
                }
            } else {
                sender.sendMessage(getMessages("messages.noPermission"));
            }
        } else {
            sender.sendMessage(getMessages("messages.playersOnly"));
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
