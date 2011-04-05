package ltguide.GiveTo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveToArgs {
	public CommandSender From;
	public CommandSender To;
	public String Item;
	public Integer Count;
	public String Command;
	public String State;
	
	public GiveToArgs(GiveTo parent, CommandSender From, String Command, String[] args) {
		this.From = From;
		this.Command = Command;
		
		if (!parent.hasPermission(From, "giveto." + Command)) State = "permission";
		else if (args.length == 0) State = "noargs";
		else if (args[0].equalsIgnoreCase("reload")) {
			if (parent.hasPermission(From, "giveto.reload")) State = "reload";
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
				for (Player player : parent.getServer().getOnlinePlayers()) {
					if (args[0].equalsIgnoreCase(player.getName())) To = player;
				}
				
				if (To == null) {
					Pattern pattern;
					Matcher m;
					try {
						pattern = Pattern.compile(".*" + args[0] + ".*", Pattern.CASE_INSENSITIVE);
						
						for (Player player : parent.getServer().getOnlinePlayers()) {
							m = pattern.matcher(player.getName());
							if (m.matches()) {
								if (To != null) {
									State = "badtarget";
									break;
								}
								To = player;
							}
						}
					}
					catch (PatternSyntaxException e) {}
				}
				
				if (To == null) State = "notarget";
			}
			
			if (State != null) return;
			
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
