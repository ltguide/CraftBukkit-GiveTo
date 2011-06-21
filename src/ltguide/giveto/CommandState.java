package ltguide.giveto;

import org.bukkit.ChatColor;

enum CommandState {
	ARGLENGTH(""),
	PERMISSION(ChatColor.RED + "You do not have permission."),
	NOTARGET(ChatColor.RED + "Unable to find target user."),
	TOOMANYTARGET(ChatColor.DARK_PURPLE + "Matching users: " + ChatColor.WHITE + "%s"),
	CONSOLE("This functionality does not work from the console."),
	NOMATCHES(ChatColor.RED + "No matching items."),
	TOOMANYMATCHES(ChatColor.DARK_PURPLE + "Matching results: " + ChatColor.WHITE + "%s"),
	BADSEARCH(ChatColor.RED + "Search term invalid."),
	DELAY(ChatColor.RED + "You must wait %s before giving %s."),
	NOACCOUNT(ChatColor.RED + "You do not have an account."),
	NOMONEY(ChatColor.RED + "This will cost %s. You are %s short.");
	
	final String Message;
	
	CommandState(String message) {
		Message = message;
	}
	
	String format(Object... args) {
		return String.format(Message, args);
	}
}
