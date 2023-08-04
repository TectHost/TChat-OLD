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

    public BannedWords(TChat plugin) {
        this.plugin = plugin;
        this.bannedWords = new HashSet<>();
        loadBannedWords();
        loadBlockedMessage();
    }

    private void loadBannedWords() {
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

        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Error loading banned_words.json, the default list will be used.", e);
        } catch (JsonSyntaxException e) {
            plugin.getLogger().log(Level.WARNING, "Error parsing banned_words.json, the default list will be used.", e);
        }
    }

    private void loadBlockedMessage() {
        File configFile = new File(plugin.getDataFolder(), "banned_words.json");

        if (!configFile.exists()) {
            plugin.saveResource("banned_words.json", false);
        }

        try {
            JsonObject jsonObject = (JsonObject) new JsonParser().parse(new FileReader(configFile));
            String blockedMessage = jsonObject.get("blockedMessage").getAsString();
            this.blockedMessage = ChatColor.translateAlternateColorCodes('&', blockedMessage);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Error loading blockedMessage from banned_words.json, using default message.", e);
            this.blockedMessage = "&cYou are not allowed to use that word.";
        } catch (JsonSyntaxException e) {
            plugin.getLogger().log(Level.WARNING, "Error parsing blockedMessage from banned_words.json, using default message.", e);
            this.blockedMessage = "&cYou are not allowed to use that word.";
        }
    }

    public boolean isWordBanned(String word) {
        return bannedWords.contains(word.toLowerCase());
    }

    public void sendBlockedMessage(CommandSender sender) {
        sender.sendMessage(blockedMessage);
    }

    public boolean canBypassBannedWords(Player player) {
        return player.hasPermission("tchat.bypass.bannedwords");
    }

    public void reloadBannedWordsList() {
        bannedWords.clear();
        loadBannedWords();
    }
}
