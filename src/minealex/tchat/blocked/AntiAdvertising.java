package minealex.tchat.blocked;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import minealex.tchat.ChatListener;
import minealex.tchat.TChat;

public class AntiAdvertising {
    private TChat plugin;
    private File messagesFile;
    private FileConfiguration messagesConfig;

    public AntiAdvertising(TChat plugin) {
        this.plugin = plugin;
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public AntiAdvertising(ChatListener chatListener) {
    }

    public boolean isIPv4Blocked() {
        return plugin.getConfig().getBoolean("antiAdvertisingIPv4Enabled", true);
    }

    public boolean isDomainBlocked() {
        return plugin.getConfig().getBoolean("antiAdvertisingDomainEnabled", true);
    }

    public boolean isLinkBlocked() {
        return plugin.getConfig().getBoolean("antiAdvertisingLinkEnabled", true);
    }

    public boolean isAdvertisingBlocked(String message) {
        if (isIPv4Blocked() && message.matches(".*\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b.*")) {
            return true;
        }

        if (isDomainBlocked() && message.matches(".*\\b[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\\b.*")) {
            return true;
        }

        if (isLinkBlocked() && message.matches(".*\\bhttps?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}/?.*")) {
            return true;
        }

        return false;
    }

    public void handleBlockedIPv4(Player player) {
        String message = getMessages("messages.antiAdvertisingIPv4Blocked");
        player.sendMessage(message);
    }

    public void handleBlockedDomain(Player player) {
        String message = getMessages("messages.antiAdvertisingDomainBlocked");
        player.sendMessage(message);
    }

    public void handleBlockedLink(Player player) {
        String message = getMessages("messages.antiAdvertisingLinkBlocked");
        player.sendMessage(message);
    }
    
    private String getMessages(String formatKey) {
        if (messagesConfig.contains(formatKey)) {
            return ChatColor.translateAlternateColorCodes('&', messagesConfig.getString(formatKey));
        } else {
            return "Invalid06";
        }
    }
}
