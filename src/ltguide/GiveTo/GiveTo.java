package ltguide.GiveTo;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.*;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class GiveTo extends JavaPlugin {
	private final Logger log = Logger.getLogger("Minecraft");
	public Configuration config;
	private PermissionHandler Permissions;
	
	public void onDisable() {}
	
	public void onEnable() {
		sendMsg(null, false, "v" + getDescription().getVersion() + " enabled (loaded items: " + reload() + ")");
	}
	
	private void sendMsg(CommandSender sender, Boolean console, String msg) {
		if (sender instanceof Player) {
			((Player) sender).sendMessage(msg);
			if (console) log.info("[" + getDescription().getName() + "] ->" + ((Player) sender).getName() + " " + ChatColor.stripColor(msg));
		}
		else log.info("[" + getDescription().getName() + "] " + ChatColor.stripColor(msg));
	}
	
	private Integer reload() {
		if (config == null) config = getConfiguration();
		else config.load();
		
		Hashtable<String, Object> defaults = new Hashtable<String, Object>();
		defaults.put("command.self", "gme");
		defaults.put("command.others", "gto");
		defaults.put("count.max", 512);
		defaults.put("count.def", 64);
		
		Set<String> keys = defaults.keySet();
		for (String key : keys) {
			if (config.getProperty(key) == null) config.setProperty(key, defaults.get(key));
		}
		
		if (config.getProperty("items") == null) {
			Map<String, Object> item = new Hashtable<String, Object>();
			item.put("id", "1");
			item.put("alias", "rock");
			config.setProperty("items.stone", item);
			
			item = new Hashtable<String, Object>();
			List<String> list = new ArrayList<String>();
			list.add("276");
			list.add("277");
			list.add("278");
			list.add("279");
			list.add("259");
			list.add("288");
			item.put("id", list);
			item.put("permission", "beginnerset");
			item.put("max", 64);
			item.put("def", 1);
			config.setProperty("items.kit", item);
		}
		
		if (!config.save()) sendMsg(null, false, "error saving config file");
		
		Pattern pattern = Pattern.compile("[0-9]+(?::[0-9]+)?");
		Matcher m;
		for (String name : config.getKeys("items")) {
			Boolean discard = false;
			List<String> items = getItems(name);
			
			if (items.size() == 0) discard = true;
			else for (String item : items) {
				m = pattern.matcher(item);
				if (!m.matches()) discard = true;
			}
			
			if (discard) {
				sendMsg(null, false, "error in " + name + "; skipping");
				config.removeProperty("items." + name);
			}
		}
		
		return config.getKeys("items").size();
	}
	
	public Boolean hasPermission(CommandSender sender, String node) {
		if (!(sender instanceof Player)) return true;
		
		Player player = (Player) sender;
		if (Permissions != null) return Permissions.has(player, node);
		else {
			Plugin test = getServer().getPluginManager().getPlugin("Permissions");
			if (test != null) {
				Permissions = ((Permissions) test).getHandler();
				return Permissions.has(player, node);
			}
		}
		return player.isOp();
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (command.getName().equalsIgnoreCase(config.getString("command.self"))) return processArgs(new GiveToArgs(this, sender, "self", args));
		if (command.getName().equalsIgnoreCase(config.getString("command.others"))) return processArgs(new GiveToArgs(this, sender, "others", args));
		return false;
	}
	
	private Boolean processArgs(GiveToArgs args) {
		if (args.State == "permission") sendMsg(args.From, false, ChatColor.RED + "You do not have permission.");
		else if (args.State == "noargs") {
			sendMsg(args.From, false, ChatColor.GRAY + "Usage: /" + config.getString("command." + args.Command) + (args.Command == "others" ? " <player|me>" : "") + " <itemid|itemname> [count]");
			if (hasPermission(args.From, "giveto.reload")) sendMsg(args.From, false, ChatColor.GRAY + "Usage: /" + config.getString("command." + args.Command) + " reload");
		}
		else if (args.State == "reload") sendMsg(args.From, true, ChatColor.GREEN + "Reloaded items (" + reload() + ").");
		else if (args.State == "nouser") sendMsg(args.From, false, ChatColor.RED + "Unable to find target user.");
		else if (args.State == "console") sendMsg(args.From, false, ChatColor.RED + "This functionality does not work from the console.");
		else {
			for (String name : config.getKeys("items")) {
				if (name.equalsIgnoreCase(args.Item) || config.getString("items." + name + ".alias", "").equalsIgnoreCase(args.Item) || config.getProperty("items." + name + ".id").equals(args.Item)) {
					giveItem(args, name);
					return true;
				}
			}
			
			Pattern pattern;
			try {
				pattern = Pattern.compile(".*" + args.Item.replaceAll("[* ]", ".*") + ".*", Pattern.CASE_INSENSITIVE);
			}
			catch (PatternSyntaxException e) {
				sendMsg(args.From, true, ChatColor.RED + "Search term invalid (" + args.Item + ").");
				return true;
			}
			
			String matches = "";
			String alias;
			Matcher m;
			for (String name : config.getKeys("items")) {
				if (matches.length() > 400) break;
				
				m = pattern.matcher(name);
				if (m.matches()) matches += (matches != "" ? ", " : "") + name;
				else if ((alias = config.getString("items." + name + ".alias", "")) != "") {
					m = pattern.matcher(alias);
					if (m.matches()) matches += (matches != "" ? ", " : "") + name;
				}
			}
			
			if (matches == "") sendMsg(args.From, false, ChatColor.RED + "No matching items.");
			else if (matches.contains(",")) sendMsg(args.From, false, ChatColor.DARK_PURPLE + "Matching results: " + matches);
			else giveItem(args, matches);
		}
		
		return true;
	}
	
	@SuppressWarnings("unchecked") private List<String> getItems(String inName) {
		Object id = config.getProperty("items." + inName + ".id");
		if (id instanceof List) return (List<String>) id;
		
		List<String> items = new ArrayList<String>();
		if (id != null) items.add((String) id);
		return items;
	}
	
	private void giveItem(GiveToArgs args, String inName) {
		String permission = config.getString("items." + inName + ".permission");
		if (permission != null && !hasPermission(args.From, "giveto.item." + permission)) {
			sendMsg(args.From, false, ChatColor.RED + "You do not have permission.");
			return;
		}
		
		if (args.Count == 0) args.Count = config.getInt("items." + inName + ".def", config.getInt("count.def", 64));
		args.Count = Math.min(args.Count, config.getInt("items." + inName + ".max", config.getInt("count.max", 512)));
		if (args.Count == 0) args.Count++; // if default/max are ever 0
		
		if (args.To != args.From && args.From instanceof Player) {
			sendMsg(args.To, true, ChatColor.GREEN + ((Player) args.From).getDisplayName() + " is placing '" + inName + "' in your inventory.");
			sendMsg(args.From, true, ChatColor.GREEN + "Placing '" + inName + "' in " + ((Player) args.To).getDisplayName() + "'s inventory.");
		}
		else sendMsg(args.To, true, ChatColor.GREEN + "Placing '" + inName + "' in your inventory.");
		
		List<String> items = getItems(inName);
		Pattern pattern = Pattern.compile(":");
		
		PlayerInventory inventory = ((Player) args.To).getInventory();
		ItemStack itemstack = new ItemStack(0);
		itemstack.setAmount(args.Count);
		for (String item : items) {
			String[] parts = pattern.split(item);
			if (parts.length == 2) itemstack.setDurability(Short.parseShort(parts[1]));
			itemstack.setTypeId(Integer.parseInt(parts[0]));
			inventory.addItem(itemstack);
		}
	}
}
