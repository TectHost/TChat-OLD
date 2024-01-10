package minealex.tchat.commands;

import java.io.FileReader;
import java.math.BigDecimal;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import minealex.tchat.TChat;

public class CalculateCommand implements CommandExecutor {
	private TChat plugin;
	
	public CalculateCommand(TChat plugin) {
        this.plugin = plugin;
    }

	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
	      if (args.length != 3) {
	    	 sender.sendMessage(getMessages("calculateThirdArgument"));
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
	               sender.sendMessage(getMessages("calculateSecondArgument"));
	               return false;
	            }

	            results = a.divide(b);
	         }

	         sender.sendMessage(getMessages("calculate").replace("%arg1%", arg11).replace("%arg2%", arg22).replace("%arg3%", arg33));
	         sender.sendMessage(getMessages("calculateResult").replace("%results%", results.toString()));
	         return true;
	      }
	   }
	
	private String getMessages(String formatKey) {
        try {
            String filePath = plugin.getDataFolder().getPath() + "/format_config.json";
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(filePath));
            JSONObject jsonObject = (JSONObject) obj;

            JSONObject messages = (JSONObject) jsonObject.get("messages");

            if (messages.containsKey(formatKey)) {
                return ChatColor.translateAlternateColorCodes('&', (String) messages.get(formatKey));
            } else {
                // If the formatKey is not found, return a default message or handle it as needed
                return ChatColor.RED + "Message not found for key: " + formatKey;
            }
        } catch (Exception e) {
            return ChatColor.RED + "Error loading message";
        }
    }
}
