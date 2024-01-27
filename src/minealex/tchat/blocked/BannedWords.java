package minealex.tchat.blocked;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import minealex.tchat.TChat;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class BannedWords {
    private Set<String> bannedWords;
    private TChat plugin;
    private String blockedMessage;
    private boolean enableTitles;
    private String title;
    private String subtitle;
    private boolean soundEnabled;
    private String sound;
    private List<String> consoleCommands;
    private FileConfiguration config;

    public BannedWords(TChat plugin) {
        this.plugin = plugin;
        this.bannedWords = new HashSet<>();
        loadConfiguration();
        consoleCommands = config.getStringList("consoleCommands");
    }

    private void loadConfiguration() {
        File configFile = new File(plugin.getDataFolder(), "banned_words.yml");
        if (!configFile.exists()) {
            plugin.saveResource("banned_words.yml", false);
        }

        // Usa la variable de instancia, no declares una nueva variable local
        this.config = YamlConfiguration.loadConfiguration(configFile);

        bannedWords.clear();
        bannedWords.addAll(config.getStringList("bannedWords"));

        blockedMessage = ChatColor.translateAlternateColorCodes('&', config.getString("blockedMessage"));

        enableTitles = config.getBoolean("enableTitles", true);
        title = ChatColor.translateAlternateColorCodes('&', config.getString("title", "&4Blocked Word"));
        subtitle = ChatColor.translateAlternateColorCodes('&', config.getString("subtitle", "&cPlease refrain from using inappropriate language"));

        soundEnabled = config.getBoolean("soundEnabled", false);
        sound = config.getString("sound", "ENTITY_ENDERMAN_TELEPORT");
    }
    
    public void executeConsoleCommands(Player player) {
        for (String command : consoleCommands) {
            // Reemplaza las variables según sea necesario
            command = command.replace("%player%", player.getName());

            // Ejecuta el comando en la consola
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
        }
    }

    private void playSound(Player player) {
        try {
            Sound soundType = Sound.valueOf(sound.toUpperCase());
            player.playSound(player.getLocation(), soundType, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, "Invalid sound specified in the configuration.", e);
        }
    }

    @SuppressWarnings("deprecation")
    public void sendBlockedMessage(CommandSender sender, String blockedWord) {
        if (enableTitles && sender instanceof Player) {
            Player player = (Player) sender;
            player.sendTitle(title, subtitle);
        }

        sender.sendMessage(blockedMessage.replace("{word}", blockedWord));

        if (soundEnabled && sender instanceof Player) {
            Player player = (Player) sender;
            playSound(player);
        }
        
        plugin.getLogger().warning("Player " + sender.getName() + " attempted to send a message containing blocked word: " + blockedWord);
    }

    public boolean isWordBanned(String word) {
        return bannedWords.contains(word.toLowerCase());
    }

    public void reloadBannedWordsList() {
        bannedWords.clear();
        loadConfiguration();
    }

    public boolean canBypassBannedWords(Player player) {
        return player.hasPermission("tchat.bypass.bannedwords");
    }
}
