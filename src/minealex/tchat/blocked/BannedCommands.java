package minealex.tchat.blocked;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import minealex.tchat.TChat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class BannedCommands implements CommandExecutor {
    private Set<String> bannedCommands;
    private FileConfiguration config;
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
        this.config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "banned_commands.yml"));
        loadBannedCommands();
        this.blockedMessage = loadBlockedMessage();
        loadTitleOptions();
    }

    private void loadBannedCommands() {
        this.bannedCommands.addAll(config.getStringList("bannedCommands"));
    }

    public String loadBlockedMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("blockedMessage", "&cNo permissions."));
    }

    public boolean isCommandBanned(String command) {
        return bannedCommands.contains(command.toLowerCase());
    }

    private void loadTitleOptions() {
        this.titleEnabled = config.getBoolean("titleEnabled", true);
        this.title = ChatColor.translateAlternateColorCodes('&', config.getString("title", "&cBanned Command"));
        this.subtitle = ChatColor.translateAlternateColorCodes('&', config.getString("subtitle", "&7You are not allowed to use this command."));
        this.soundEnabled = config.getBoolean("soundEnabled", true);
        this.sound = config.getString("sound", "entity.ender_dragon.growl");
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public String getSound() {
        return sound;
    }

    private String getMessages(String formatKey) {
        try {
            String filePath = plugin.getDataFolder().getPath() + "/format_config.yml";
            FileConfiguration messagesConfig = YamlConfiguration.loadConfiguration(new File(filePath));

            if (messagesConfig.contains("messages." + formatKey)) {
                return ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("messages." + formatKey));
            } else {
                // If the formatKey is not found, return a default message or handle it as needed
                return ChatColor.RED + "Message not found for key: " + formatKey;
            }
        } catch (Exception e) {
            // Handle the exception appropriately (log it, return a default value, etc.)
            plugin.getLogger().log(Level.WARNING, "Error loading message from format_config.yml", e);
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
    
    public void executeCommandsOnBlock(CommandSender sender, String blockedCommand) {
        List<String> commandsToRun = config.getStringList("commandsToRun");

        for (String command : commandsToRun) {
            // Reemplazar %executor% con el nombre del jugador que ejecutó el comando bloqueado
            command = command.replace("%executor%", sender.getName());

            // Reemplazar %blocked_command% con el comando bloqueado
            command = command.replace("%blocked_command%", blockedCommand);

            // Ejecutar el comando
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    public boolean canBypassCommandBlocker(Player player) {
        return player.hasPermission("tchat.bypass.commandblocker");
    }
}
