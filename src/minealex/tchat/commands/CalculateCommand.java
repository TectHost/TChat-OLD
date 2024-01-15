package minealex.tchat.commands;

import java.io.File;
import java.math.BigDecimal;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import minealex.tchat.TChat;

public class CalculateCommand implements CommandExecutor {
	private File messagesFile;
    private FileConfiguration messagesConfig;
	
	public CalculateCommand(TChat plugin) {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
	      if (args.length != 3) {
	    	 sender.sendMessage(getMessages("messages.calculateThirdArgument"));
	         return false;
	      } else {
	         String arg11 = args[0];
	         String arg22 = args[1];
	         String arg33 = args[2];
	         BigDecimal a = new BigDecimal(arg11);
	         BigDecimal b = new BigDecimal(arg33);
	         new BigDecimal("" + a);
	         BigDecimal results;
	         if (arg22.equals("+")) {
	            results = a.add(b);
	         } else if (arg22.equals("*")) {
	            results = a.multiply(b);
	         } else if (arg22.equals("-")) {
	            results = a.subtract(b);
	         } else {
	            if (!arg22.equals("/")) {
	               sender.sendMessage(getMessages("messages.calculateSecondArgument"));
	               return false;
	            }

	            results = a.divide(b);
	         }

	         sender.sendMessage(getMessages("messages.calculate").replace("%arg1%", arg11).replace("%arg2%", arg22).replace("%arg3%", arg33));
	         sender.sendMessage(getMessages("messages.calculateResult").replace("%results%", results.toString()));
	         return true;
	      }
	   }
	
	private String getMessages(String formatKey) {
        if (messagesConfig.contains(formatKey)) {
            return ChatColor.translateAlternateColorCodes('&', messagesConfig.getString(formatKey));
        } else {
            return "Invalid05";
        }
    }
}
