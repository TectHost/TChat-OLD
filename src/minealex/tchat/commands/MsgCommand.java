package minealex.tchat.commands;

import java.io.FileReader;

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
            if (args.length >= 2) {
                Player recipient = sender.getServer().getPlayer(args[0]);
                if (recipient != null) {
                    String message = String.join(" ", args).substring(args[0].length() + 1);

                    String msgSentFormat = getConfiguredFormat("msgSent");
                    String msgReceivedFormat = getConfiguredFormat("msgReceived");

                    String formattedMessageSent = msgSentFormat
                            .replace("<sender>", sender.getName())
                            .replace("<recipient>", recipient.getName())
                            .replace("<message>", message);

                    String formattedMessageReceived = msgReceivedFormat
                            .replace("<sender>", sender.getName())
                            .replace("<message>", message);

                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', formattedMessageSent));
                    recipient.sendMessage(ChatColor.translateAlternateColorCodes('&', formattedMessageReceived));
                } else {
                    sender.sendMessage("The specified player is not online.");
                }
            } else {
                sender.sendMessage("Incorrect use. You must use /msg <player> <message>");
            }
        } else {
            sender.sendMessage("Only players can use this command.");
        }
        return true;
    }

    private String getConfiguredFormat(String formatKey) {
        try {
            String filePath = plugin.getDataFolder().getPath() + "/format_config.json";
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(filePath));
            JSONObject jsonObject = (JSONObject) obj;

            return (String) ((JSONObject) jsonObject.get("msgFormats")).get(formatKey);
        } catch (Exception e) {
            e.printStackTrace();
            return "<sender> whispers to <recipient>: <message>";
        }
    }
}
