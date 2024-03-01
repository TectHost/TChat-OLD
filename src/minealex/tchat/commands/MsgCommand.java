package minealex.tchat.commands;

import java.io.File;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import minealex.tchat.TChat;

public class MsgCommand implements CommandExecutor {
    private TChat plugin;
    private File savesFile;
    private FileConfiguration savesConfig;

    public MsgCommand(TChat plugin) {
        this.plugin = plugin;
        savesFile = new File(plugin.getDataFolder(), "saves.yml");
        savesConfig = YamlConfiguration.loadConfiguration(savesFile);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("tchat.msg")) {
                if (args.length >= 2) {
                    Player recipient = sender.getServer().getPlayer(args[0]);
                    if (recipient != null) {
                        String message = String.join(" ", args).substring(args[0].length() + 1);

                        // Actualizar el último remitente de mensajes para ambos jugadores
                        plugin.setLastMessageSender(player.getUniqueId(), recipient.getUniqueId());
                        plugin.setLastMessageSender(recipient.getUniqueId(), player.getUniqueId());

                        // Acceder a la configuración del archivo saves.yml
                        List<String> ignoredPlayers = getSavesConfig().getStringList("ignore");

                        if (ignoredPlayers.contains(recipient.getName())) {
                            String ignoredMessage = plugin.getMessagesYML("messages.cannotMessageIgnored");
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
                        player.sendMessage(plugin.getMessagesYML("messages.noPlayerOnline"));
                    }
                } else {
                    player.sendMessage(plugin.getMessagesYML("messages.incorrectUsage"));
                }
            } else {
                player.sendMessage(plugin.getMessagesYML("messages.noPermission"));
            }
        } else {
            sender.sendMessage(plugin.getMessagesYML("messages.playersOnly"));
        }
        return true;
    }

    private String getConfiguredFormat(String formatKey) {
        String defaultFormat = "<sender> whispers to <recipient>: <message>";
        try {
            if (plugin.getConfig().contains("Msg." + formatKey)) {
                return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Msg." + formatKey));
            } else {
                return defaultFormat;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return defaultFormat;
        }
    }

    private FileConfiguration getSavesConfig() {
        return savesConfig;
    }
}
