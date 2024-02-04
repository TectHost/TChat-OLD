package minealex.tchat.commands;

import java.math.BigDecimal;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import minealex.tchat.TChat;

public class CalculateCommand implements CommandExecutor {
	
	private TChat plugin;

	public CalculateCommand(TChat plugin) {
        this.plugin = plugin;
    }

	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
	      if (args.length != 3) {
	    	 sender.sendMessage(plugin.getMessagesYML("messages.calculateThirdArgument"));
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
	               sender.sendMessage(plugin.getMessagesYML("messages.calculateSecondArgument"));
	               return false;
	            }

	            results = a.divide(b);
	         }

	         sender.sendMessage(plugin.getMessagesYML("messages.calculate").replace("%arg1%", arg11).replace("%arg2%", arg22).replace("%arg3%", arg33));
	         sender.sendMessage(plugin.getMessagesYML("messages.calculateResult").replace("%results%", results.toString()));
	         return true;
	      }
	   }
}
