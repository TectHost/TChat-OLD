package minealex.tchat.commands;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import minealex.tchat.TChat;

public class MentionCommand implements CommandExecutor {

    private final TChat plugin;

    public MentionCommand(TChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
        	sender.sendMessage(plugin.getMessagesYML("onlyPlayer"));
            return true;
        }

        if (args.length != 1) {
        	sender.sendMessage(plugin.getMessagesYML("incorrectUsageMention"));
            return true;
        }

        Player targetPlayer = plugin.getServer().getPlayer(args[0]);

        if (targetPlayer == null || !targetPlayer.isOnline()) {
        	sender.sendMessage(plugin.getMessagesYML("noPlayerOnline"));
            return true;
        }

        // Cargar la configuración desde el archivo config.yml
        String soundPath = plugin.getConfig().getString("mention_sound");

        // Validar que el sonido especificado sea válido (puedes agregar más validaciones si es necesario)
        Sound mentionSound;
        try {
            mentionSound = Sound.valueOf(soundPath);
        } catch (IllegalArgumentException e) {
        	sender.sendMessage(plugin.getMessagesYML("soundNotFound"));
            return true;
        }

        // Ejemplo: reproducir el sonido
        targetPlayer.playSound(targetPlayer.getLocation(), mentionSound, 1f, 1f);

        String mentionSenderMessage = plugin.getMessagesYML("mention-sender-message");
        String mentionTargetMessage = plugin.getMessagesYML("mention-target-message");
        
        sender.sendMessage(String.format(mentionSenderMessage, targetPlayer.getName()));
        targetPlayer.sendMessage(String.format(mentionTargetMessage, sender.getName()));

        return true;
    }
}
