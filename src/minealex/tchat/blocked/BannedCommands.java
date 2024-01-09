package minealex.tchat.blocked;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import minealex.tchat.TChat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class BannedCommands implements CommandExecutor {
    private Set<String> bannedCommands;
    private TChat plugin;
    private String blockedMessage;
    private boolean titleEnabled;
    private String title;
    private String subtitle;
    private boolean soundEnabled;
    private String sound;

    public BannedCommands(TChat plugin) {
        this.plugin = plugin;
        this.bannedCommands = new HashSet<>();
        loadBannedCommands();
        this.blockedMessage = loadBlockedMessage();
        loadTitleOptions();
    }

    private void loadBannedCommands() {
        File configFile = new File(plugin.getDataFolder(), "banned_commands.json");

        if (!configFile.exists()) {
            plugin.saveResource("banned_commands.json", false);
        }

        try {
            JsonObject jsonObject = (JsonObject) new JsonParser().parse(new FileReader(configFile));
            JsonArray commandsArray = jsonObject.getAsJsonArray("bannedCommands");

            for (JsonElement element : commandsArray) {
                String command = element.getAsString();
                bannedCommands.add(command.toLowerCase());
            }

        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Error loading banned_commands.json, the default list will be used.", e);
        } catch (JsonSyntaxException e) {
            plugin.getLogger().log(Level.WARNING, "Error parsing banned_commands.json, the default list will be used.", e);
        }
    }
    
    public String loadBlockedMessage() {
        File configFile = new File(plugin.getDataFolder(), "banned_commands.json");

        if (!configFile.exists()) {
            plugin.saveResource("banned_commands.json", false);
        }

        try {
            JsonObject jsonObject = (JsonObject) new JsonParser().parse(new FileReader(configFile));
            String blockedMessage = jsonObject.get("blockedMessage").getAsString();
            return ChatColor.translateAlternateColorCodes('&', blockedMessage);
        } catch (IOException | JsonSyntaxException e) {
            plugin.getLogger().log(Level.WARNING, "Error loading blockedMessage from banned_commands.json, using default message.", e);
            return ChatColor.RED + "No permissions.";
        }
    }
    
    public boolean isCommandBanned(String command) {
        return bannedCommands.contains(command.toLowerCase());
    }
    
    private void loadTitleOptions() {
        File configFile = new File(plugin.getDataFolder(), "banned_commands.json");

        if (!configFile.exists()) {
            plugin.saveResource("banned_commands.json", false);
        }

        try {
            JsonObject jsonObject = (JsonObject) new JsonParser().parse(new FileReader(configFile));
            this.titleEnabled = jsonObject.get("titleEnabled").getAsBoolean();
            this.title = ChatColor.translateAlternateColorCodes('&', jsonObject.get("title").getAsString());
            this.subtitle = ChatColor.translateAlternateColorCodes('&', jsonObject.get("subtitle").getAsString());
            this.soundEnabled = jsonObject.get("soundEnabled").getAsBoolean();
            this.sound = jsonObject.get("sound").getAsString();
        } catch (IOException | JsonSyntaxException e) {
            plugin.getLogger().log(Level.WARNING, "Error loading title options from banned_commands.json, using default values.", e);
            this.titleEnabled = true;
            this.title = "&cBanned Command";
            this.subtitle = "&7You are not allowed to use this command.";
            this.soundEnabled = true;
            this.sound = "entity.ender_dragon.growl";
        }
    }

    // Add these methods to your BannedCommands class
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public String getSound() {
        return sound;
    }
    
    private String getMessages(String formatKey) {
        try {
            String filePath = plugin.getDataFolder().getPath() + "/format_config.json";
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(filePath));
            JSONObject jsonObject = (JSONObject) obj;

            JSONObject messages = (JSONObject) jsonObject.get("messages");

            if (messages.containsKey(formatKey)) {
                return ChatColor.translateAlternateColorCodes('&', (String) messages.get(formatKey));
            } else {
                // If the formatKey is not found, return a default message or handle it as needed
                return ChatColor.RED + "Message not found for key: " + formatKey;
            }
        } catch (Exception e) {
            // Handle the exception appropriately (log it, return a default value, etc.)
            plugin.getLogger().log(Level.WARNING, "Error loading message from format_config.json", e);
            return ChatColor.RED + "Error loading message";
        }
    }

    public void playSound(Player player) {
        if (soundEnabled) {
            player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
        }
    }

    public boolean isTitleEnabled() {
        return titleEnabled;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    @SuppressWarnings("deprecation")
    public void sendTitle(Player player) {
        if (titleEnabled) {
            player.sendTitle(title, subtitle);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("checkcommand")) {
            if (!sender.hasPermission("tchat.checkcommand")) {
                sender.sendMessage(getMessages("noPermission"));
                return true;
            }

            if (args.length != 1) {
                sender.sendMessage(getMessages("incorrectUsageBannedCommands"));
                return true;
            }

            String commandToCheck = args[0].toLowerCase();
            if (isCommandBanned(commandToCheck)) {
                sender.sendMessage(getMessages("commandBanned").replace("%command%", commandToCheck));
            } else {
                sender.sendMessage(getMessages("commandAllowed").replace("%command%", commandToCheck));
            }
            return true;
        }
        return false;
    }
    
    public boolean canBypassCommandBlocker(Player player) {
        return player.hasPermission("tchat.bypass.commandblocker");
    }
}
