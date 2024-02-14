package minealex.tchat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import minealex.tchat.TChat;

import java.io.File;
import java.io.IOException;

public class NickCommand implements CommandExecutor {
    private final TChat plugin;

    public NickCommand(TChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessagesYML("messages.onlyPlayer"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("tchat.nickname")) {
            player.sendMessage(plugin.getMessagesYML("messages.noPermission"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(plugin.getMessagesYML("messages.nickCommandUsage"));
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

        player.sendMessage(plugin.getMessagesYML("messages.nick").replace("%nick%", nuevoNick));

        return true;
    }
}
