package minealex.tchat.commands;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class MentionCommand implements CommandExecutor {

    private final Plugin plugin;

    public MentionCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando solo puede ser ejecutado por un jugador.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("Uso correcto: /mention <jugador>");
            return true;
        }

        Player targetPlayer = plugin.getServer().getPlayer(args[0]);

        if (targetPlayer == null || !targetPlayer.isOnline()) {
            sender.sendMessage("El jugador especificado no está en línea.");
            return true;
        }

        // Cargar la configuración desde el archivo config.yml
        String soundPath = plugin.getConfig().getString("mention_sound");

        // Validar que el sonido especificado sea válido (puedes agregar más validaciones si es necesario)
        Sound mentionSound;
        try {
            mentionSound = Sound.valueOf(soundPath);
        } catch (IllegalArgumentException e) {
            sender.sendMessage("El sonido especificado en la configuración no es válido.");
            return true;
        }

        // Ejemplo: reproducir el sonido
        targetPlayer.playSound(targetPlayer.getLocation(), mentionSound, 1f, 1f);

        sender.sendMessage("Has mencionado a " + targetPlayer.getName());
        targetPlayer.sendMessage("¡Has sido mencionado por " + sender.getName());

        return true;
    }
}
