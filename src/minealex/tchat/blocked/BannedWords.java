package minealex.tchat.blocked;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import minealex.tchat.TChat;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
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

    public BannedWords(TChat plugin) {
        this.plugin = plugin;
        this.bannedWords = new HashSet<>();
        loadConfiguration();
    }

    private void loadConfiguration() {
        File configFile = new File(plugin.getDataFolder(), "banned_words.json");

        if (!configFile.exists()) {
            plugin.saveResource("banned_words.json", false);
        }

        try {
            JsonObject jsonObject = (JsonObject) new JsonParser().parse(new FileReader(configFile));
            JsonArray wordsArray = jsonObject.getAsJsonArray("bannedWords");

            for (JsonElement element : wordsArray) {
                String word = element.getAsString();
                bannedWords.add(word.toLowerCase());
            }

            String blockedMessage = jsonObject.get("blockedMessage").getAsString();
            this.blockedMessage = ChatColor.translateAlternateColorCodes('&', blockedMessage);

            this.enableTitles = jsonObject.has("enableTitles") && jsonObject.get("enableTitles").getAsBoolean();
            this.title = jsonObject.has("title") ? jsonObject.get("title").getAsString() : "Blocked Word";
            this.subtitle = jsonObject.has("subtitle") ? jsonObject.get("subtitle").getAsString() : "Please refrain from using inappropriate language";

        } catch (IOException | JsonSyntaxException e) {
            plugin.getLogger().log(Level.WARNING, "Error loading banned_words.json, the default list will be used.", e);
        }
    }

    @SuppressWarnings("deprecation")
	public void sendBlockedMessage(CommandSender sender, String blockedWord) {
        if (enableTitles && sender instanceof Player) {
            Player player = (Player) sender;
            player.sendTitle(ChatColor.translateAlternateColorCodes('&', title), ChatColor.translateAlternateColorCodes('&', subtitle));
        }

        sender.sendMessage(blockedMessage.replace("{word}", blockedWord));
    }

    public boolean isWordBanned(String word) {
        return bannedWords.contains(word.toLowerCase());
    }

    public void reloadBannedWordsList() {
        bannedWords.clear();
        // Load configuration again
        loadConfiguration();
    }

    public boolean canBypassBannedWords(Player player) {
        return player.hasPermission("tchat.bypass.bannedwords");
    }
}
