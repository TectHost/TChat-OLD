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
        	sender.sendMessage(plugin.getMessagesYML("messages.onlyPlayer"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("tchat.mention")) {
            player.sendMessage(plugin.getMessagesYML("messages.noPermission"));
            return true;
        }

        if (args.length != 1) {
        	sender.sendMessage(plugin.getMessagesYML("messages.incorrectUsageMention"));
            return true;
        }

        Player targetPlayer = plugin.getServer().getPlayer(args[0]);

        if (targetPlayer == null || !targetPlayer.isOnline()) {
        	sender.sendMessage(plugin.getMessagesYML("messages.noPlayerOnline"));
            return true;
        }

        // Cargar la configuración desde el archivo config.yml
        String soundPath = plugin.getConfig().getString("mention_sound");

        // Validar que el sonido especificado sea válido (puedes agregar más validaciones si es necesario)
        Sound mentionSound;
        try {
            mentionSound = Sound.valueOf(soundPath);
        } catch (IllegalArgumentException e) {
        	sender.sendMessage(plugin.getMessagesYML("messages.soundNotFound"));
            return true;
        }

        // Ejemplo: reproducir el sonido
        targetPlayer.playSound(targetPlayer.getLocation(), mentionSound, 1f, 1f);

        String mentionSenderMessage = plugin.getMessagesYML("messages.mention-sender-message");
        String mentionTargetMessage = plugin.getMessagesYML("messages.mention-target-message");
        
        sender.sendMessage(String.format(mentionSenderMessage, targetPlayer.getName()));
        targetPlayer.sendMessage(String.format(mentionTargetMessage, sender.getName()));

        return true;
    }
}
