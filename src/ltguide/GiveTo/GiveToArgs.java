package ltguide.GiveTo;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveToArgs {
	public CommandSender From;
	public CommandSender To;
	public String Item;
	public Integer Count;
	public String State;
	
	public GiveToArgs(GiveTo plugin, CommandSender From, String Command, String[] args) {
		this.From = From;
		Command = Command.equals("giveme") ? "self" : "others";
		
		if (!plugin.hasPermission(From, "giveto." + Command)) State = "permission";
		else if (args.length == 0) State = "noargs";
		else if (args[0].equalsIgnoreCase("reload")) {
			if (plugin.hasPermission(From, "giveto.reload")) State = "reload";
			else State = "permission";
		}
		else {
			Integer lastArg = args.length - 1;
			Integer firstArg = 1;
			
			if (Command == "self") {
				To = From;
				firstArg--;
			}
			else if (args[0].equalsIgnoreCase("me")) To = From;
			else {
				List<Player> matches = plugin.getServer().matchPlayer(args[0]);
				
				if (matches.size() == 0) State = "notarget";
				else if (matches.size() > 1) State = "badtarget";
				else To = matches.get(0);
				
				if (State != null) return;
			}
			
			if (!(To instanceof Player)) State = "console";
			else if (firstArg > lastArg) State = "noargs";
			else {
				if (firstArg < lastArg && args[lastArg].matches("[1-9](?:[0-9]+)?")) {
					Count = Integer.parseInt(args[lastArg]);
					lastArg--;
				}
				else Count = 0;
				
				Item = args[firstArg];
				for (Integer i = firstArg + 1; i <= lastArg; i++)
					Item += " " + args[i];
				Item = Item.toLowerCase();
			}
		}
	}
}
