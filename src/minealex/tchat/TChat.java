package minealex.tchat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import me.clip.placeholderapi.PlaceholderAPI;
import minealex.tchat.blocked.BannedWords;
import minealex.tchat.commands.ClearChatCommand;
import minealex.tchat.commands.TChatReloadCommand;
import minealex.tchat.placeholders.Placeholders;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import me.clip.placeholderapi.PlaceholderAPI;

@SuppressWarnings("unused")
public class TChat extends JavaPlugin implements CommandExecutor {
    private String defaultPrefix;
    private String defaultSuffix;
    private Map<String, ChatGroup> groups;
    private ChatListener chatListener;
    private String customFormat;
    private Map<String, String> messages;
    private BannedWords bannedWords;

    @Override
    public void onEnable() {
        // Registrar el comando /chat reload
        getCommand("chat").setExecutor(new TChatReloadCommand(this));

        // Registrar el comando /chat clear
        getCommand("chatclear").setExecutor(new ClearChatCommand(this));

        // Cargar la configuración
        loadConfigFile();

        // Load the banned words list
        loadBannedWordsList();

        // Registrar el evento del chat
        registerChatListener();
        
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(this).register();
        }
    }

    @Override
    public void onDisable() {
        // Desregistrar el evento del chat al desactivar el plugin
        HandlerList.unregisterAll(chatListener);
    }

    private void loadConfigFile() {
        // Si no existe el archivo, se crea con la configuración predeterminada
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File configFile = new File(getDataFolder(), "format_config.json");

        // Si no existe el archivo, se copia desde los recursos del plugin
        if (!configFile.exists()) {
            saveResource("format_config.json", false);
        }

        // Cargar la configuración desde el archivo JSON
        try {
            Gson gson = new Gson();
            JsonObject jsonObject = (JsonObject) new JsonParser().parse(new FileReader(configFile));

            defaultPrefix = ChatColor.translateAlternateColorCodes('&', jsonObject.get("defaultPrefix").getAsString());
            defaultSuffix = ChatColor.translateAlternateColorCodes('&', jsonObject.get("defaultSuffix").getAsString());

            customFormat = jsonObject.get("format").getAsString();

            groups = new HashMap<>();
            JsonObject groupsObject = jsonObject.getAsJsonObject("groups");
            for (Entry<String, JsonElement> entry : groupsObject.entrySet()) {
                String groupName = entry.getKey();
                JsonObject groupObject = entry.getValue().getAsJsonObject();
                String prefix = ChatColor.translateAlternateColorCodes('&', groupObject.get("prefix").getAsString());
                String suffix = ChatColor.translateAlternateColorCodes('&', groupObject.get("suffix").getAsString());
                groups.put(groupName, new ChatGroup(prefix, suffix));
            }

            messages = new HashMap<>();
            JsonObject messagesObject = jsonObject.getAsJsonObject("messages");
            for (Entry<String, JsonElement> entry : messagesObject.entrySet()) {
                String messageKey = entry.getKey();
                String message = ChatColor.translateAlternateColorCodes('&', entry.getValue().getAsString());
                messages.put(messageKey, message);
            }

        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Error loading format_config.json, the default configuration will be used.", e);
            defaultPrefix = "[Tect.host] ";
            defaultSuffix = "";
            customFormat = "<prefix> &r<player>&r<suffix> &e>> &7";
            groups = new HashMap<>();
            messages = new HashMap<>();
        } catch (JsonSyntaxException e) {
            getLogger().log(Level.WARNING, "Error parsing format_config.json, the default configuration will be used.", e);
            defaultPrefix = "[Tect.host] ";
            defaultSuffix = "";
            customFormat = "<prefix> &r<player>&r<suffix> &e>> &7";
            groups = new HashMap<>();
            messages = new HashMap<>();
        }
    }

    private void loadBannedWordsList() {
        bannedWords = new BannedWords(this);
    }

    public String formatMessage(String message, CommandSender sender) {
        String prefix = defaultPrefix;
        String suffix = defaultSuffix;

        if (sender != null) {
            for (Entry<String, ChatGroup> entry : groups.entrySet()) {
                String groupName = entry.getKey();
                ChatGroup chatGroup = entry.getValue();

                if (sender.hasPermission("tchat.group." + groupName)) {
                    prefix = chatGroup.getPrefix();
                    suffix = chatGroup.getSuffix();
                    break;
                }
            }
        }

        String format;
        if (customFormat != null && !customFormat.isEmpty()) {
            format = customFormat;
        } else {
            format = "<prefix><player><suffix>";
        }

        // SetPlaceholders utilizando PlaceholderAPI
        if (sender instanceof Player) {
            Player player = (Player) sender;
            format = PlaceholderAPI.setPlaceholders(player, format);
        }

        format = format.replace("<prefix>", prefix).replace("<player>", "%1$s").replace("<suffix>", suffix);
        return ChatColor.translateAlternateColorCodes('&', format) + message;
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "&cMessage not found: " + key);
    }

    // Método para recargar el archivo format_config.json
    public void reloadFormatConfig() {
        File configFile = new File(getDataFolder(), "format_config.json");
        if (!configFile.exists()) {
            getLogger().warning("The format_config.json file does not exist, it cannot be reloaded.");
            return;
        }

        // Cargar la configuración desde el archivo JSON nuevamente
        try {
            Gson gson = new Gson();
            JsonObject jsonObject = (JsonObject) new JsonParser().parse(new FileReader(configFile));

            defaultPrefix = ChatColor.translateAlternateColorCodes('&', jsonObject.get("defaultPrefix").getAsString());
            defaultSuffix = ChatColor.translateAlternateColorCodes('&', jsonObject.get("defaultSuffix").getAsString());

            customFormat = jsonObject.get("format").getAsString();

            groups.clear();
            JsonObject groupsObject = jsonObject.getAsJsonObject("groups");
            for (Entry<String, JsonElement> entry : groupsObject.entrySet()) {
                String groupName = entry.getKey();
                JsonObject groupObject = entry.getValue().getAsJsonObject();
                String prefix = ChatColor.translateAlternateColorCodes('&', groupObject.get("prefix").getAsString());
                String suffix = ChatColor.translateAlternateColorCodes('&', groupObject.get("suffix").getAsString());
                groups.put(groupName, new ChatGroup(prefix, suffix));
            }

            messages.clear();
            JsonObject messagesObject = jsonObject.getAsJsonObject("messages");
            for (Entry<String, JsonElement> entry : messagesObject.entrySet()) {
                String messageKey = entry.getKey();
                String message = ChatColor.translateAlternateColorCodes('&', entry.getValue().getAsString());
                messages.put(messageKey, message);
            }

            getLogger().info("The format_config.json file has been successfully reloaded.");

            // Volver a registrar el evento del chat con la nueva configuración
            unregisterChatListener();
            registerChatListener();
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Error loading format_config.json, the current configuration will be kept.", e);
        } catch (JsonSyntaxException e) {
            getLogger().log(Level.WARNING, "Error parsing format_config.json, the current configuration will be kept.", e);
        }
    }

    private void registerChatListener() {
        chatListener = new ChatListener(this);
        getServer().getPluginManager().registerEvents(chatListener, this);
    }

    private void unregisterChatListener() {
        HandlerList.unregisterAll(chatListener);
    }

    // Clase ChatGroup
    public static class ChatGroup {
        private String prefix;
        private String suffix;

        public ChatGroup(String prefix, String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getSuffix() {
            return suffix;
        }
    }

    // Clase TChatReloadCommand
    // (El código para esta clase se mantiene igual)

    // Clase ChatListener
    // (El código para esta clase se mantiene igual)

    public BannedWords getBannedWords() {
        return bannedWords;
    }

    public ChatGroup getDefaultChatGroup() {
        return new ChatGroup(defaultPrefix, defaultSuffix);
    }

    public Map<String, ChatGroup> getGroups() {
        return groups;
    }
}
