package ltguide.giveto;

import java.util.IllegalFormatException;

import org.bukkit.ChatColor;

enum CommandMessage {
	ARGLENGTH("&5Syntax: &f%s"),
	PERMISSION("&cYou do not have permission."),
	NOTARGET("&cUnable to find target user."),
	TOOMANYTARGET("&5Matching users: &f%s"),
	CONSOLE("This functionality does not work from the console."),
	NOMATCHES("&cNo matching items."),
	TOOMANYMATCHES("&5Matching results: &f%s"),
	BADSEARCH("&cSearch term invalid."),
	DELAY("&cYou must wait %s before giving %s."),
	NOACCOUNT("&cYou do not have an account."),
	NOMONEY("&cThis will cost %s. You are %s short."),
	RELOADABLE("&5Syntax: &f/%s reload"),
	RELOADDONE("&aReloaded items (%s)."),
	INVENTORYFULL("&cYour inventory is full."),
	GIVEFROM("&a%s is placing '%s' in your inventory."),
	GIVETO("&aPlacing '%s' in %s inventory. %s"),
	SUBTRACTMONEY(" (-%s; balance is now %s)");
	
	private String message;
	
	CommandMessage(String message) {
		setMessage(message);
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
	
	@Override
	public String toString() {
		return message.replaceAll("(?i)&([0-F])", "\u00A7$1");
	}
	
	public String toString(Object... args) {
		try {
			return String.format(this.toString(), args);
		}
		catch (IllegalFormatException e) {
			return ChatColor.RED + "Error in " + this.name() + " translation! (" + e.getMessage() + ")";
		}
	}
}
