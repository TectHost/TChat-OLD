package minealex.tchat;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
import minealex.tchat.blocked.Replacer;
import minealex.tchat.bot.AutoBroadcast;
import minealex.tchat.bot.ChatBot;
import minealex.tchat.bot.ChatGames;
import minealex.tchat.bot.CommandTimer;
import minealex.tchat.commands.AdminChatCommand;
import minealex.tchat.commands.AnnouncementCommand;
import minealex.tchat.commands.BroadcastCommand;
import minealex.tchat.commands.CalculateCommand;
import minealex.tchat.commands.ChatColorCommand;
import minealex.tchat.commands.ClearChatCommand;
import minealex.tchat.commands.CommandManager;
import minealex.tchat.commands.Commands;
import minealex.tchat.commands.DiscordCommand;
import minealex.tchat.commands.FacebookCommand;
import minealex.tchat.commands.HelpCommand;
import minealex.tchat.commands.HelpOpCommand;
import minealex.tchat.commands.IgnoreCommand;
import minealex.tchat.commands.InfoCommand;
import minealex.tchat.commands.InstagramCommand;
import minealex.tchat.commands.ListCommand;
import minealex.tchat.commands.MeCommand;
import minealex.tchat.commands.MentionCommand;
import minealex.tchat.commands.MsgCommand;
import minealex.tchat.commands.NickCommand;
import minealex.tchat.commands.PingCommand;
import minealex.tchat.commands.PlayerCommand;
import minealex.tchat.commands.PluginCommand;
import minealex.tchat.commands.PrintCommand;
import minealex.tchat.commands.ReplyCommand;
import minealex.tchat.commands.RulesCommand;
import minealex.tchat.commands.SeenCommand;
import minealex.tchat.commands.StaffChatCommand;
import minealex.tchat.commands.StoreCommand;
import minealex.tchat.commands.TeamSpeakCommand;
import minealex.tchat.commands.TikTokCommand;
import minealex.tchat.commands.TwitterCommand;
import minealex.tchat.commands.WarningCommand;
import minealex.tchat.commands.WebsiteCommand;
import minealex.tchat.commands.YoutubeCommand;
import minealex.tchat.config.BannedCommandsConfig;
import minealex.tchat.config.CommandTimerConfig;
import minealex.tchat.config.Config;
import minealex.tchat.config.ConfigManager;
import minealex.tchat.config.DeathConfig;
import minealex.tchat.config.DisableConfig;
import minealex.tchat.config.LevelsConfig;
import minealex.tchat.config.MessagesConfig;
import minealex.tchat.config.ReplacerConfig;
import minealex.tchat.listener.ChatEventListener;
import minealex.tchat.listener.JoinListener;
import minealex.tchat.listener.PlayerMoveListener;
import minealex.tchat.listener.TPSListener;
import minealex.tchat.perworldchat.PerWorldChat;
import minealex.tchat.perworldchat.RadiusChat;
import minealex.tchat.perworldchat.WorldsManager;
import minealex.tchat.placeholders.Placeholders;
import minealex.tchat.utils.DeathMessages;
import minealex.tchat.utils.Levels;
import minealex.tchat.utils.SignColor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
import org.json.simple.parser.ParseException;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

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
	private PerWorldChat perWorldChat;
	private RadiusChat radiusChat;
	private WorldsManager worldsManager;
	private static TChat instance;
	private DeathMessages deathMessages;
	private DeathConfig deathConfig;
	private BannedCommandsConfig bannedCommandsConfig;
	private String blockedMessage;
	private MessagesConfig messagesConfig;
	private Config config;
	private ReplacerConfig replacerConfig;
	private ConfigManager configManager;
	private Map<UUID, UUID> lastMessageSenders;
	private LevelsConfig levelsConfig;
	private Levels levels;
	private CommandTimerConfig commandTimerConfig;
	private CommandTimer commandTimer;

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
        
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        
        this.lastMessageSenders = new HashMap<>();
        
        messages = new HashMap<>();
        
        levels = new Levels(this);
        
        this.version = getDescription().getVersion();
        
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        
        chatGames = new ChatGames(this);
        getServer().getPluginManager().registerEvents(new ChatEventListener(chatGames), this);
        
        // Registrar el comando /chat clear
        getCommand("chatclear").setExecutor(new ClearChatCommand(this));
        
        getCommand("chatcolor").setExecutor(new ChatColorCommand(this));
        
        getCommand("help").setExecutor(new HelpCommand(this));
        
        getCommand("plugin").setExecutor(new PluginCommand(this));
        
        getCommand("broadcast").setExecutor(new BroadcastCommand(this));
        
        getCommand("warning").setExecutor(new WarningCommand(this));
        
        getCommand("player").setExecutor(new PlayerCommand(this));
        
        getCommand("announcement").setExecutor(new AnnouncementCommand(this));
        
        getCommand("ping").setExecutor(new PingCommand(this));
        
        getCommand("me").setExecutor(new MeCommand(this));
        
        getCommand("checkcommand").setExecutor(new BannedCommands(this));
        
        getCommand("discord").setExecutor(new DiscordCommand(this));
        
        getCommand("instagram").setExecutor(new InstagramCommand(this));
        
        getCommand("store").setExecutor(new StoreCommand(this));
        
        getCommand("facebook").setExecutor(new FacebookCommand(this));
        
        getCommand("tiktok").setExecutor(new TikTokCommand(this));
        
        getCommand("website").setExecutor(new WebsiteCommand(this));
        
        getCommand("youtube").setExecutor(new YoutubeCommand(this));
        
        getCommand("twitter").setExecutor(new TwitterCommand(this));
        
        getCommand("teamspeak").setExecutor(new TeamSpeakCommand(this));
        
        getCommand("staffchat").setExecutor(new StaffChatCommand(this));
        
        getCommand("adminchat").setExecutor(new AdminChatCommand(this));
        
        getCommand("seen").setExecutor(new SeenCommand(this));
        
        getCommand("calculate").setExecutor(new CalculateCommand(this));
        
        if (!isOptionEnabled("disable.helpop")) {
            this.getCommand("nick").setExecutor(new NickCommand(this));
        }
        
        if (!isOptionEnabled("disable.helpop")) {
        	getCommand("helpop").setExecutor(new HelpOpCommand(this));
        }
        
        if (!isOptionEnabled("disable.ignore")) {
        	getCommand("ignore").setExecutor(new IgnoreCommand(this));
        }
        
        if (!isOptionEnabled("disable.rules")) {
        	getCommand("rules").setExecutor(new RulesCommand(this));
        }
        
        if (!isOptionEnabled("disable.msg")) {
        	MsgCommand msgCommand = new MsgCommand(this);
            getCommand("msg").setExecutor(msgCommand);
            getCommand("reply").setExecutor(new ReplyCommand(this));
        }
        
        if (!isOptionEnabled("disable.list")) {
        	getCommand("list").setExecutor(new ListCommand(this));
        }
        
        getCommand("info").setExecutor(new InfoCommand(this));
        
        getCommand("print").setExecutor(new PrintCommand(this));
        
        getCommand("mention").setExecutor(new MentionCommand(this));
        
        new TPSListener(this);
        
        loadConfigFile();
        
        new SignColor(this);
        
        CommandManager commandManager = new CommandManager(this);
        
        chatListener = new ChatListener(this);
        perWorldChat = new PerWorldChat(this);

        getServer().getPluginManager().registerEvents(perWorldChat, this);
        
        this.worldsManager = new WorldsManager(new File(getDataFolder(), "worlds.yml"));
        this.radiusChat = new RadiusChat(worldsManager);
        
        DisableConfig disableConfig = new DisableConfig(this);
        disableConfig.createDefaultConfig();
        
        deathConfig = new DeathConfig(this);
        deathConfig.createDefaultConfig();
        
        commandTimerConfig = new CommandTimerConfig(this);
        commandTimerConfig.createDefaultConfig();
        
        levelsConfig = new LevelsConfig(this);
        levelsConfig.createDefaultConfig();
        
        messagesConfig = new MessagesConfig(this);
        messagesConfig.createDefaultConfig();
        
        replacerConfig = new ReplacerConfig(this);
        replacerConfig.createDefaultConfig();
        
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        config = new Config(this);
        config.createDefaultConfig();
        
        bannedCommandsConfig = new BannedCommandsConfig(this);
        bannedCommandsConfig.createDefaultConfig();
        
        deathMessages = new DeathMessages(this);
        
        chatBot = new ChatBot(this);
        
        boolean isUnicodeBlocked = isUnicodeBlocked();
        
        antiUnicode = new AntiUnicode(isUnicodeBlocked);
        
        instance = this;
        
        FileConfiguration replacerConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "replacer.yml"));
        Replacer replacerInstance = Replacer.getInstance(replacerConfig);
        getServer().getPluginManager().registerEvents(replacerInstance, this);
        
        commandTimer = new CommandTimer(this);
        
        loadMessages();

        loadBannedWordsList();
        
        anticapEnabled = isAnticapEnabled();

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
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getLogger().warning("The config.yml file does not exist.");
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Obtener el valor de Other.chatCooldownSeconds de config.yml
        if (config.contains("Other.chatCooldownSeconds")) {
            chatCooldownSeconds = config.getInt("Other.chatCooldownSeconds");
            getLogger().info("chatCooldownSeconds set to " + chatCooldownSeconds + " seconds.");
        } else {
            getLogger().warning("Other.chatCooldownSeconds not found in config.yml.");
        }
    }

    // Agrega un método para establecer chatCooldownSeconds en tu plugin
    public int getChatCooldownSeconds() {
        return chatCooldownSeconds;  // Devolver el valor almacenado en la variable
    }
    
    private boolean isUnicodeBlocked() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            return false; // Si el archivo no existe, asumimos que la función está deshabilitada por defecto.
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Obtener el valor de Other.anti_unicode de config.yml
        if (config.contains("Other.anti_unicode")) {
            return config.getBoolean("Other.anti_unicode");
        } else {
            getLogger().warning("Other.anti_unicode not found in config.yml.");
        }

        return false; // En caso de error, asumimos que la función está deshabilitada.
    }
    
    private boolean isAntispamEnabled() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            return false;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Obtener el valor de antiSpamEnabled de config.yml
        if (config.contains("Other.antiSpamEnabled")) {
            boolean antiSpamEnabled = config.getBoolean("Other.antiSpamEnabled");

            return antiSpamEnabled;
        }

        return false;
    }

    boolean isAntibotEnabled() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            return false; // Si el archivo no existe, la función está deshabilitada por defecto.
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Obtener el valor de antibotEnabled del archivo config.yml
        return config.getBoolean("Other.antibotEnabled", true);
    }
    
    private boolean isAnticapEnabled() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            return false; // Si el archivo no existe, la función está deshabilitada por defecto.
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Obtener el valor de anticap_enabled de config.yml
        if (config.contains("Other.anticap_enabled")) {
            return config.getBoolean("Other.anticap_enabled");
        }

        return false; // En caso de que la propiedad no exista, asumimos que la función está deshabilitada.
    }

    @Override
    public void onDisable() {
        // Desregistrar el evento del chat al desactivar el plugin
        HandlerList.unregisterAll(chatListener);
        super.onDisable();
        configManager.saveConfig();
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

        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Error loading format_config.json, the default configuration will be used.", e);
            defaultPrefix = "[Tect.host] ";
            defaultSuffix = "";
            customFormat = "<prefix> &r<player>&r<suffix> &e>> &7";
            groups = new HashMap<>();
        } catch (JsonSyntaxException e) {
            getLogger().log(Level.WARNING, "Error parsing format_config.json, the default configuration will be used.", e);
            defaultPrefix = "[Tect.host] ";
            defaultSuffix = "";
            customFormat = "<prefix> &r<player>&r<suffix> &e>> &7";
            groups = new HashMap<>();
        }
    }

    private void loadBannedWordsList() {
        bannedWords = new BannedWords(this);
    }
    
    private boolean isOptionEnabled(String optionName) {
        File configFile = new File(getDataFolder(), "disable.yml");
        if (configFile.exists()) {
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
                return config.getBoolean(optionName, true); // Mantenemos el valor predeterminado en true
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // En caso de error o si el archivo no existe, asumimos que la función está habilitada
        return false;  // Cambiado a false para reflejar que la opción está deshabilitada por defecto
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
            Player player = (Player) sender;
            format = PlaceholderAPI.setPlaceholders(player, format);
        } else {
            format = "%tchat_prefix%&f%tchat_nickname%%tchat_suffix%";
            Player player = (Player) sender;
            format = PlaceholderAPI.setPlaceholders(player, format);
        }

        // SetPlaceholders utilizando PlaceholderAPI
        if (sender instanceof Player) {
            Player player = (Player) sender;
            format = PlaceholderAPI.setPlaceholders(player, format);
        }

        return ChatColor.translateAlternateColorCodes('&', format) + message;
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "&cMessage not found: " + key);
    }
    
    private void loadMessages() {
        File configFile = new File(getDataFolder(), "messages.yml");

        if (!configFile.exists()) {
            saveResource("messages.yml", false);
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        
        messages.clear();
        
        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection != null) {
            for (String key : messagesSection.getKeys(false)) {
                messages.put(key, messagesSection.getString(key));
            }
        }
    }

    public Map<UUID, UUID> getLastConversations() {
        return lastConversations;
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
            
            JsonArray hoverArray = jsonObject.getAsJsonArray("hover");
            List<String> hoverLines = new ArrayList<>();
            if (hoverArray != null) {
                for (JsonElement element : hoverArray) {
                    hoverLines.add(ChatColor.translateAlternateColorCodes('&', element.getAsString()));
                }
            }

            groups.clear();
            JsonObject groupsObject = jsonObject.getAsJsonObject("groups");
            for (Entry<String, JsonElement> entry : groupsObject.entrySet()) {
                String groupName = entry.getKey();
                JsonObject groupObject = entry.getValue().getAsJsonObject();
                String prefix = ChatColor.translateAlternateColorCodes('&', groupObject.get("prefix").getAsString());
                String suffix = ChatColor.translateAlternateColorCodes('&', groupObject.get("suffix").getAsString());
                
                JsonArray groupHoverArray = groupObject.getAsJsonArray("hover");
                List<String> groupHoverLines = new ArrayList<>();
                if (groupHoverArray != null) {
                    for (JsonElement element : groupHoverArray) {
                        groupHoverLines.add(ChatColor.translateAlternateColorCodes('&', element.getAsString()));
                    }
                }
                
                groups.put(groupName, new ChatGroup(prefix, suffix));
            }

            unregisterChatListener();
            registerChatListener();
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Error loading format_config.json, the current configuration will be kept.", e);
        } catch (JsonSyntaxException e) {
            getLogger().log(Level.WARNING, "Error parsing format_config.json, the current configuration will be kept.", e);
        }
    }
    
    public UUID getLastMessageSender(UUID playerId) {
        return lastMessageSenders.get(playerId);
    }

    public void setLastMessageSender(UUID playerId, UUID lastMessageSender) {
        lastMessageSenders.put(playerId, lastMessageSender);
    }
    
    private void registerChatListener() {
        chatListener = new ChatListener(this);
        getServer().getPluginManager().registerEvents(chatListener, this);
    }

    private void unregisterChatListener() {
        HandlerList.unregisterAll(chatListener);
    }

    // Clase ChatGroup
    public class ChatGroup {
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
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            return false; // Si el archivo no existe, la función está deshabilitada por defecto.
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Obtener el valor de antiAdvertisingEnabled del archivo config.yml
        return config.getBoolean("AntiAdvertising.antiAdvertisingEnabled", true);
    }


    public boolean isIPv4Blocked() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            return false; // Si el archivo no existe, la función está deshabilitada por defecto.
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Obtener el valor de antiAdvertisingEnabled del archivo config.yml
        return config.getBoolean("AntiAdvertising.ipv4_blocked", true);
    }

    public boolean isDomainBlocked() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            return false; // Si el archivo no existe, la función está deshabilitada por defecto.
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Obtener el valor de antiAdvertisingEnabled del archivo config.yml
        return config.getBoolean("AntiAdvertising.domain_blocked", true);
    }

    public boolean isLinkBlocked() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            return false; // Si el archivo no existe, la función está deshabilitada por defecto.
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Obtener el valor de antiAdvertisingEnabled del archivo config.yml
        return config.getBoolean("AntiAdvertising.link_blocked", true);
    }

    public void handleBlockedIPv4(Player player) {
        String message = getMessagesYML("messages.antiAdvertisingIPv4Blocked");
        player.sendMessage(message);
    }

    public void handleBlockedDomain(Player player) {
        String message = getMessagesYML("messages.antiAdvertisingDomainBlocked");
        player.sendMessage(message);
    }

    public void handleBlockedLink(Player player) {
        String message = getMessagesYML("messages.antiAdvertisingLinkBlocked");
        player.sendMessage(message);
    }

    public String getMessagesYML(String key) {
        File configFile = new File(getDataFolder(), "messages.yml");
        if (!configFile.exists()) {
            getLogger().warning("messages.yml not found. Default message will be used.");
            return "Default message"; // Mensaje por defecto si el archivo no existe
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Obtener el mensaje del archivo messages.yml
        if (config.contains(key)) {
            String message = config.getString(key);
            return parseColors(message); // Traducir códigos de colores en el mensaje
        } else {
            getLogger().warning("Message key '" + key + "' not found in messages.yml. Default message will be used.");
            return ChatColor.RED + "Error in getMessagesYML2. Support: https://tect.host/"; // Mensaje por defecto si la clave no se encuentra
        }
    }
    
    public String getMessagesYML() {
        File configFile = new File(getDataFolder(), "messages.yml");
        if (!configFile.exists()) {
            getLogger().warning("messages.yml not found. Default message will be used.");
            return "Default message"; // Mensaje por defecto si el archivo no existe
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Obtener el mensaje del archivo messages.yml
        String defaultKey = "default"; // Puedes cambiar esto a la clave que desees usar como predeterminada

        if (config.contains(defaultKey)) {
            String message = config.getString(defaultKey);
            return parseColors(message); // Traducir códigos de colores en el mensaje
        } else {
            getLogger().warning("Default message key '" + defaultKey + "' not found in messages.yml. Default message will be used.");
            return ChatColor.RED + "Error in getMessagesYML2. Support: https://tect.host/"; // Mensaje por defecto si la clave predeterminada no se encuentra
        }
    }
    
    public List<String> getMessagesYMLList(String key) {
        File configFile = new File(getDataFolder(), "messages.yml");
        if (!configFile.exists()) {
            getLogger().warning("messages.yml not found. Default messages will be used.");
            return Collections.singletonList(ChatColor.RED + "Error in getMessagesYMLList. Support: https://tect.host/"); // Lista con un mensaje por defecto si el archivo no existe
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Obtener la lista de mensajes del archivo messages.yml
        if (config.contains(key)) {
            List<String> messages = config.getStringList(key);
            return messages.stream().map(this::parseColors).collect(Collectors.toList()); // Traducir códigos de colores en cada mensaje
        } else {
            getLogger().warning("Message key '" + key + "' not found in messages.yml. Default messages will be used.");
            return Collections.singletonList(ChatColor.RED + "Error in getMessagesYMLList2. Support: https://tect.host/"); // Lista con un mensaje por defecto si la clave no se encuentra
        }
    }

    private String parseColors(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public UUID getLastConversationalist(UUID playerUUID) {
        if (lastConversations.containsKey(playerUUID)) {
            return lastConversations.get(playerUUID);
        } else {
            return null; // Devuelve null si no hay una última conversación registrada
        }
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
    
    public ConfigManager getConfigManager() {
        return configManager;
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
    
    public void reloadAutoBroadcastConfig() {
        File file = new File(getDataFolder(), "autobroadcast.yml");
        if (file.exists()) {
            try {
                YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(file);

                int time = yamlConfig.getInt("time");
                boolean enabled = yamlConfig.getBoolean("enabled");

                Map<String, List<String>> broadcasts = new HashMap<>();
                ConfigurationSection broadcastsSection = yamlConfig.getConfigurationSection("broadcasts");
                for (String broadcastKey : broadcastsSection.getKeys(false)) {
                    List<String> broadcastMessages = broadcastsSection.getStringList(broadcastKey + ".messages");
                    broadcasts.put(broadcastKey, new ArrayList<>(broadcastMessages));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static TChat getInstance() {
        return instance;
    }
    
    public void reloadChatGamesConfig() {
        File file = new File(getDataFolder(), "chatgames.json");
        if (file.exists()) {
            try {
                FileReader reader = new FileReader(file);
                JSONParser jsonParser = new JSONParser();
                JSONArray jsonArray = (JSONArray) jsonParser.parse(reader);

                for (Object obj : jsonArray) {
                    JSONObject gameObject = (JSONObject) obj;

                    boolean enabled = (Boolean) gameObject.get("enabled");
                    String message = (String) gameObject.get("message");
                    long time = (Long) gameObject.get("time");
                    String keyword = (String) gameObject.get("keyword");

                    JSONArray rewardsArray = (JSONArray) gameObject.get("rewards");
                    List<String> rewards = new ArrayList<>();
                    for (Object reward : rewardsArray) {
                        rewards.add((String) reward);
                    }
                }

                reader.close();
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void reloadChatBotConfig() {
        File file = new File(getDataFolder(), "chatbot.yml");
        if (file.exists()) {
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);

                Map<String, String> responses = new HashMap<>();
                for (String key : config.getKeys(false)) {
                    String keyword = key;
                    String response = config.getString(key);
                    responses.put(keyword, response);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void reloadBannedCommandsConfig() {
        File file = new File(getDataFolder(), "banned_commands.yml");
        if (file.exists()) {
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);

                List<String> bannedCommands = config.getStringList("bannedCommands");
                blockedMessage = config.getString("blockedMessage");

                boolean titleEnabled = config.getBoolean("titleEnabled");
                String title = config.getString("title");
                String subtitle = config.getString("subtitle");
                boolean soundEnabled = config.getBoolean("soundEnabled");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public void reloadWorldsConfig() {
        File file = new File(getDataFolder(), "worlds.yml");
        if (file.exists()) {
            try (FileInputStream input = new FileInputStream(file)) {
                Yaml yaml = new Yaml();
                Iterable<Object> iterable = yaml.loadAll(input);

                for (Object obj : iterable) {
                    if (obj instanceof Map) {
                        Map<String, Object> worldConfig = (Map<String, Object>) obj;

                        for (Map.Entry<String, Object> entry : worldConfig.entrySet()) {
                            String worldName = entry.getKey();
							Map<String, Object> worldData = (Map<String, Object>) entry.getValue();

                            boolean chatEnabled = worldData.get("chatEnabled") != null && (boolean) worldData.get("chatEnabled");
                            boolean perWorldChat = worldData.get("perWorldChat") != null && (boolean) worldData.get("perWorldChat");
                            boolean radiusChatEnabled = worldData.get("radiusChatEnabled") != null && (boolean) worldData.get("radiusChatEnabled");
                            long radiusChat = worldData.get("radiusChat") != null ? (int) worldData.get("radiusChat") : 0;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public WorldsManager getWorldsManager() {
        return worldsManager;
    }

    public BannedCommands getBannedCommands() {
        return bannedCommands;
    }
}