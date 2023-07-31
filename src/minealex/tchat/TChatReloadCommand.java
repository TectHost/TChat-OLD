package minealex.tchat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TChatReloadCommand implements CommandExecutor {
    private final TChat plugin;

    public TChatReloadCommand(TChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("chat") && args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!(sender instanceof Player) || sender.hasPermission("tchat.reload")) {
                plugin.reloadFormatConfig();
                String reloadSuccessMessage = plugin.getMessage("reloadSuccess");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', reloadSuccessMessage));
            } else {
                String noPermissionMessage = plugin.getMessage("noPermission");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermissionMessage));
            }
            return true;
        }
        return false;
    }
}
