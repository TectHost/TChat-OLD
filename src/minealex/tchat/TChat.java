package minealex.tchat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import me.clip.placeholderapi.PlaceholderAPI;
import minealex.tchat.blocked.AntiAdvertising;
import minealex.tchat.blocked.AntiUnicode;
import minealex.tchat.blocked.BannedCommands;
import minealex.tchat.blocked.BannedWords;
import minealex.tchat.bot.AutoBroadcast;
import minealex.tchat.bot.ChatBot;
import minealex.tchat.bot.ChatGames;
import minealex.tchat.commands.AnnouncementCommand;
import minealex.tchat.commands.BroadcastCommand;
import minealex.tchat.commands.ChatColorCommand;
import minealex.tchat.commands.ClearChatCommand;
import minealex.tchat.commands.Commands;
import minealex.tchat.commands.ListCommand;
import minealex.tchat.commands.MeCommand;
import minealex.tchat.commands.MsgCommand;
import minealex.tchat.commands.PingCommand;
import minealex.tchat.commands.ReplyCommand;
import minealex.tchat.commands.RulesCommand;
import minealex.tchat.commands.StaffChatCommand;
import minealex.tchat.commands.WarningCommand;
import minealex.tchat.listener.ChatEventListener;
import minealex.tchat.listener.JoinListener;
import minealex.tchat.listener.PlayerMoveListener;
import minealex.tchat.placeholders.Placeholders;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import me.clip.placeholderapi.PlaceholderAPI;

@SuppressWarnings("unused")
public class TChat extends JavaPlugin implements CommandExecutor, Listener {
    private String defaultPrefix;
    private String defaultSuffix;
    private Map<String, ChatGroup> groups;
    private ChatListener chatListener;
    private String customFormat;
    private Map<String, String> messages;
    private BannedWords bannedWords;
    private Map<UUID, Location> lastKnownLocations = new HashMap<>();
    private Set<UUID> playersWhoMoved = new HashSet<>();
    private Map<UUID, Boolean> playerMovementStatus = new HashMap<>();
    private boolean anticapEnabled;
	private int chatCooldownSeconds;
	private String version;
	private BannedCommands bannedCommands;
	private Map<UUID, UUID> lastConversations = new HashMap<>();
	private AntiUnicode antiUnicode;
	private ChatGames chatGames;
	private ChatBot chatBot;
	private AutoBroadcast autoBroadcast;
	private Set<UUID> staffChatPlayers = new HashSet<>();

    public Location getLastPlayerLocation(Player player) {
        return lastKnownLocations.get(player.getUniqueId());
    }

    public void setLastPlayerLocation(Player player, Location location) {
        lastKnownLocations.put(player.getUniqueId(), location);
    }
    
    public void markPlayerAsMoved(UUID playerId) {
        playerMovementStatus.put(playerId, true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Cuando un jugador se une, marca su estado de movimiento como falso.
        UUID playerId = event.getPlayer().getUniqueId();
        playerMovementStatus.put(playerId, false);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Marcar al jugador como que se ha movido cuando se mueve.
        UUID playerId = event.getPlayer().getUniqueId();
        playerMovementStatus.put(playerId, true);
    }

    // Agrega este método para verificar si un jugador se ha movido.
    public boolean hasPlayerMoved(UUID playerId) {
        return playerMovementStatus.getOrDefault(playerId, false);
    }

    @Override
    public void onEnable() {
        getCommand("chat").setExecutor(new Commands(this));
        
        MsgCommand msgCommand = new MsgCommand(this);
        getCommand("msg").setExecutor(msgCommand);
        
        this.version = getDescription().getVersion();
        
        getCommand("reply").setExecutor(new ReplyCommand(this));
        
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        
        chatGames = new ChatGames(this);
        getServer().getPluginManager().registerEvents(new ChatEventListener(chatGames), this);
        
        // Registrar el comando /chat clear
        getCommand("chatclear").setExecutor(new ClearChatCommand(this));
        
        getCommand("chatcolor").setExecutor(new ChatColorCommand(this));
        
        getCommand("rules").setExecutor(new RulesCommand(this));
        
        getCommand("list").setExecutor(new ListCommand(this));
        
        getCommand("broadcast").setExecutor(new BroadcastCommand());
        
        getCommand("warning").setExecutor(new WarningCommand());
        
        getCommand("announcement").setExecutor(new AnnouncementCommand());
        
        getCommand("ping").setExecutor(new PingCommand(this));
        
        getCommand("me").setExecutor(new MeCommand());
        
        getCommand("checkcommand").setExecutor(new BannedCommands(this));
        
        getCommand("staffchat").setExecutor(new StaffChatCommand(this));
        
        // Cargar la configuración
        loadConfigFile();
        
        chatBot = new ChatBot(this);
        
        boolean isUnicodeBlocked = isUnicodeBlocked();
        
        antiUnicode = new AntiUnicode(isUnicodeBlocked);

        // Load the banned words list
        loadBannedWordsList();
        
        anticapEnabled = isAnticapEnabled();

        // Habilitar la función antibot si está habilitada en la configuración
        if (isAntibotEnabled()) {
            getLogger().info("Antibot is enabled. Players will need to move to chat.");
        }

        // Registrar el evento del chat
        registerChatListener();
        bannedCommands = new BannedCommands(this);

        // Registrar el evento de movimiento
        getServer().getPluginManager().registerEvents(this, this);
        
        loadAndSetChatCooldownSeconds();
        
        autoBroadcast = new AutoBroadcast(this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(this).register();
        }
    }

	private void loadAndSetChatCooldownSeconds() {
        File configFile = new File(getDataFolder(), "format_config.json");
        if (!configFile.exists()) {
            getLogger().warning("The format_config.json file does not exist.");
            return;
        }

        try {
            Gson gson = new Gson();
            JsonObject jsonObject = (JsonObject) new JsonParser().parse(new FileReader(configFile));

            chatCooldownSeconds = jsonObject.get("chatCooldownSeconds").getAsInt();  // Guardar el valor en la variable
            getLogger().info("chatCooldownSeconds set to " + chatCooldownSeconds + " seconds.");
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Error loading format_config.json: " + e.getMessage());
        }
    }

    // Agrega un método para establecer chatCooldownSeconds en tu plugin
    public int getChatCooldownSeconds() {
        return chatCooldownSeconds;  // Devolver el valor almacenado en la variable
    }
    
    public boolean isUnicodeBlocked() {
        File configFile = new File(getDataFolder(), "format_config.json");
        if (!configFile.exists()) {
            return false; // Si el archivo no existe, asumimos que la función está deshabilitada por defecto.
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(reader).getAsJsonObject();

            return jsonObject.get("anti_unicode").getAsBoolean(); // Leer anti_unicode del JSON
        } catch (IOException e) {
            getLogger().warning("Error reading format_config.json: " + e.getMessage());
        } catch (Exception e) {
            getLogger().warning("Error reading anti_unicode from format_config.json: " + e.getMessage());
        }

        return false; // En caso de error, asumimos que la función está deshabilitada.
    }
    
    private boolean isAntispamEnabled() {
        File configFile = new File(getDataFolder(), "format_config.json");
        if (!configFile.exists()) {
            return false;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(reader).getAsJsonObject();
            boolean antiSpamEnabled = jsonObject.get("antiSpamEnabled").getAsBoolean();
            getLogger().info("antiSpamEnabled value: " + antiSpamEnabled); // Agrega este registro para depuración

            return antiSpamEnabled;
        } catch (IOException e) {
            getLogger().warning("Error reading format_config.json: " + e.getMessage());
        } catch (Exception e) {
            getLogger().warning("Error reading antiSpamEnabled from format_config.json: " + e.getMessage());
        }

        return false;
    }

	// Agrega un método para verificar si la función antibot está habilitada
    boolean isAntibotEnabled() {
        File configFile = new File(getDataFolder(), "format_config.json");
        if (!configFile.exists()) {
            return false; // Si el archivo no existe, la función está deshabilitada por defecto.
        }

        try {
            Gson gson = new Gson();
            JsonObject jsonObject = (JsonObject) new JsonParser().parse(new FileReader(configFile));

            return jsonObject.get("antibotEnabled").getAsBoolean();
        } catch (IOException | JsonSyntaxException e) {
            getLogger().log(Level.WARNING, "Error reading antibotEnabled from format_config.json.", e);
        }

        return false; // En caso de error, asumimos que la función está deshabilitada.
    }
    
    private boolean isAnticapEnabled() {
        File configFile = new File(getDataFolder(), "format_config.json");
        if (!configFile.exists()) {
            return false; // Si el archivo no existe, la función está deshabilitada por defecto.
        }

        try {
            Gson gson = new Gson();
            JsonObject jsonObject = (JsonObject) new JsonParser().parse(new FileReader(configFile));
            JsonObject anticapSettings = jsonObject.getAsJsonObject("anticap_settings");

            return anticapSettings != null && anticapSettings.get("anticap_enabled").getAsBoolean();
        } catch (IOException | JsonSyntaxException e) {
            getLogger().log(Level.WARNING, "Error reading anticap_enabled from format_config.json.", e);
        }

        return false; // En caso de error, asumimos que la función está deshabilitada.
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
    
    public boolean isAntiAdvertisingEnabled() {
        File configFile = new File(getDataFolder(), "format_config.json");
        if (!configFile.exists()) {
            return false; // Si el archivo no existe, la función está deshabilitada por defecto.
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(reader).getAsJsonObject();

            return jsonObject.get("antiAdvertisingEnabled").getAsBoolean(); // Leer antiAdvertisingEnabled del JSON
        } catch (IOException e) {
            getLogger().warning("Error reading format_config.json: " + e.getMessage());
        } catch (Exception e) {
            getLogger().warning("Error reading antiAdvertisingEnabled from format_config.json: " + e.getMessage());
        }

        return false; // En caso de error, asumimos que la función está deshabilitada.
    }

    public boolean isIPv4Blocked() {
        File configFile = new File(getDataFolder(), "format_config.json");
        if (!configFile.exists()) {
            return false;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(reader).getAsJsonObject();
            JsonObject antiAdvertisingSettings = jsonObject.getAsJsonObject("antiAdvertisingSettings");

            return antiAdvertisingSettings != null && antiAdvertisingSettings.get("ipv4_blocked").getAsBoolean();
        } catch (IOException | JsonSyntaxException e) {
            getLogger().log(Level.WARNING, "Error reading ipv4_blocked from format_config.json.", e);
        }

        return false;
    }

    public boolean isDomainBlocked() {
        File configFile = new File(getDataFolder(), "format_config.json");
        if (!configFile.exists()) {
            return false;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(reader).getAsJsonObject();
            JsonObject antiAdvertisingSettings = jsonObject.getAsJsonObject("antiAdvertisingSettings");

            return antiAdvertisingSettings != null && antiAdvertisingSettings.get("domain_blocked").getAsBoolean();
        } catch (IOException | JsonSyntaxException e) {
            getLogger().log(Level.WARNING, "Error reading domain_blocked from format_config.json.", e);
        }

        return false;
    }

    public boolean isLinkBlocked() {
        File configFile = new File(getDataFolder(), "format_config.json");
        if (!configFile.exists()) {
            return false;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(reader).getAsJsonObject();
            JsonObject antiAdvertisingSettings = jsonObject.getAsJsonObject("antiAdvertisingSettings");

            return antiAdvertisingSettings != null && antiAdvertisingSettings.get("link_blocked").getAsBoolean();
        } catch (IOException | JsonSyntaxException e) {
            getLogger().log(Level.WARNING, "Error reading link_blocked from format_config.json.", e);
        }

        return false;
    }

    public void handleBlockedIPv4(Player player) {
		String message = this.getMessage("antiAdvertisingIPv4Blocked");
        player.sendMessage(message);
    }
    
    public UUID getLastConversationalist(UUID playerUUID) {
        if (lastConversations.containsKey(playerUUID)) {
            return lastConversations.get(playerUUID);
        } else {
            return null; // Devuelve null si no hay una última conversación registrada
        }
    }

    public void handleBlockedDomain(Player player) {
        String message = this.getMessage("antiAdvertisingDomainBlocked");
        player.sendMessage(message);
    }

    public void handleBlockedLink(Player player) {
        String message = this.getMessage("antiAdvertisingLinkBlocked");
        player.sendMessage(message);
    }

    public boolean isAdvertisingBlocked(String message) {
        AntiAdvertising antiAdvertising = new AntiAdvertising(this);

        if (antiAdvertising.isIPv4Blocked() && message.matches(".*\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b.*")) {
            return true;
        }

        if (antiAdvertising.isDomainBlocked() && message.matches(".*\\b[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\\b.*")) {
            return true;
        }

        if (antiAdvertising.isLinkBlocked() && message.matches(".*\\bhttps?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}/?.*")) {
            return true;
        }

        return false;
    }
    
    public String getVersion() {
        return this.version;
    }
    
    public void writeJsonToFile(File file, JSONArray games) {
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(games.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String getConfiguredFormat(String formatKey) {
        try {
            String filePath = getDataFolder().getPath() + "/format_config.json";
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(filePath));
            JSONObject jsonObject = (JSONObject) obj;

            return (String) ((JSONObject) jsonObject.get("msgFormats")).get(formatKey);
        } catch (Exception e) {
            e.printStackTrace();
            return "<sender> sends a message to <recipient>: <message>";
        }
    }

    public void updateLastConversationalist(UUID sender, UUID recipient) {
        lastConversations.put(sender, recipient);
    }
    
    public Set<UUID> getStaffChatPlayers() {
        return staffChatPlayers;
    }

    public void addPlayerToStaffChat(Player player) {
        staffChatPlayers.add(player.getUniqueId());
    }

    public void removePlayerFromStaffChat(Player player) {
        staffChatPlayers.remove(player.getUniqueId());
    }
    
    public ChatBot getChatBot() {
        return chatBot;
    }
    
    public ChatGames getChatGames() {
        return chatGames;
    }
    
    public BannedCommands getBannedCommands() {
        return bannedCommands;
    }
}