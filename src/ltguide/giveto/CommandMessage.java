package ltguide.giveto;

import java.util.IllegalFormatException;

import org.bukkit.ChatColor;

enum CommandMessage {
	ARGLENGTH(ChatColor.DARK_PURPLE + "Syntax: " + ChatColor.WHITE + "%s"),
	PERMISSION(ChatColor.RED + "You do not have permission."),
	NOTARGET(ChatColor.RED + "Unable to find target user."),
	TOOMANYTARGET(ChatColor.DARK_PURPLE + "Matching users: " + ChatColor.WHITE + "%s"),
	CONSOLE("This functionality does not work from the console."),
	NOMATCHES(ChatColor.RED + "No matching items."),
	TOOMANYMATCHES(ChatColor.DARK_PURPLE + "Matching results: " + ChatColor.WHITE + "%s"),
	BADSEARCH(ChatColor.RED + "Search term invalid."),
	DELAY(ChatColor.RED + "You must wait %s before giving %s."),
	NOACCOUNT(ChatColor.RED + "You do not have an account."),
	NOMONEY(ChatColor.RED + "This will cost %s. You are %s short."),
	RELOADABLE(ChatColor.DARK_PURPLE + "Syntax: " + ChatColor.WHITE + "/%s reload"),
	RELOADDONE(ChatColor.GREEN + "Reloaded items (%s)."),
	INVENTORYFULL(ChatColor.RED + "Your inventory is full."),
	GIVEFROM(ChatColor.GREEN + "%s is placing '%s' in your inventory."),
	GIVETO(ChatColor.GREEN + "Placing '%s' in %s inventory. %s"),
	SUBTRACTMONEY(" (-%s; balance is now %s)");
	
	private String message;
	
	CommandMessage(String message) {
		setMessage(message);
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	@Override
	public String toString() {
		return message;
	}
	
	public String toString(Object... args) {
		try {
			return String.format(message, args);
		}
		catch (IllegalFormatException e) {
			return ChatColor.RED + "Error in " + this.name() + " translation! (" + e.getMessage() + ")";
		}
	}
}
