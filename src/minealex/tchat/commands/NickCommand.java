package minealex.tchat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class NickCommand implements CommandExecutor {
    private final Plugin plugin;
    private File messagesFile;
    private FileConfiguration messagesConfig;

    public NickCommand(Plugin plugin) {
        this.plugin = plugin;
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMessages("messages.onlyPlayer"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("tchat.nickname")) {
            player.sendMessage(getMessages("messages.noPermission"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(getMessages("messages.nickCommandUsage"));
            return true;
        }

        // Obtener el nombre del argumento
        String nuevoNick = args[0];

        // Reemplazar plugin.getConfig() con el nuevo archivo saves.yml
        File savesFile = new File(plugin.getDataFolder(), "saves.yml");
        FileConfiguration savesConfig = YamlConfiguration.loadConfiguration(savesFile);

        // Actualizar el nick en el saves.yml
        savesConfig.set("players." + player.getUniqueId() + ".nick", nuevoNick);

        // Guardar la configuración en saves.yml
        try {
            savesConfig.save(savesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Aplicar el nuevo nick al jugador
        player.setDisplayName(nuevoNick);
        player.setPlayerListName(nuevoNick);

        player.sendMessage(getMessages("messages.nick").replace("%nick%", nuevoNick));

        return true;
    }

    private String getMessages(String formatKey) {
        if (messagesConfig.contains(formatKey)) {
            return ChatColor.translateAlternateColorCodes('&', messagesConfig.getString(formatKey));
        } else {
            return "Invalid05";
        }
    }
}
