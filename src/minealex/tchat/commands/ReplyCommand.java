package minealex.tchat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import minealex.tchat.TChat;

import java.util.UUID;

public class ReplyCommand implements CommandExecutor {
    private final TChat plugin;

    public ReplyCommand(TChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("tchat.reply")) {
                // Obtener el UUID del último remitente de mensajes desde la memoria
                UUID lastMessageSender = plugin.getLastMessageSender(player.getUniqueId());

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
                            player.sendMessage(plugin.getMessagesYML("messages.incorrectUsage"));
                            return true;
                        }
                    } else {
                        player.sendMessage(plugin.getMessagesYML("messages.noPlayerOnline"));
                        return true;
                    }
                } else {
                    player.sendMessage(plugin.getMessagesYML("messages.noLastConversationalist"));
                    return true;
                }
            } else {
                player.sendMessage(plugin.getMessagesYML("messages.noPermission"));
            }
        } else {
            sender.sendMessage(plugin.getMessagesYML("messages.playersOnly"));
        }
        return true;
    }
}
