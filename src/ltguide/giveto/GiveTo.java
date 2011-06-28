package ltguide.giveto;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Method.MethodAccount;
import com.nijikokun.register.payment.Methods;

public class GiveTo extends JavaPlugin {
	private final Logger log = Logger.getLogger("Minecraft");
	private Configuration config;
	private HashMap<String, Integer> delayPlayers = new HashMap<String, Integer>();
	
	public PermissionHandler Permissions;
	public boolean checkPermissions;
	public Method Method;
	public boolean checkMethod;
	
	public void onDisable() {
		delayPlayers.clear();
	}
	
	public void onEnable() {
		getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_DISABLE, new GiveToServerListener(this), Priority.Monitor, this);
		sendLog("v" + getDescription().getVersion() + " enabled (loaded items: " + reload() + ")");
	}
	
	private void sendMsg(CommandSender target, String msg) {
		sendMsg(target, msg, false);
	}
	
	private void sendMsg(CommandSender target, String msg, Boolean log) {
		if (target instanceof Player) {
			((Player) target).sendMessage(msg);
			if (log) sendLog("->" + ((Player) target).getName() + " " + msg);
		}
		else sendLog("->*CONSOLE " + msg);
	}
	
	private void sendLog(String msg) {
		log.info("[" + getDescription().getName() + "] " + ChatColor.stripColor(msg));
	}
	
	private boolean writeResource(String resource, File outFile) {
		InputStream inStream = getClass().getResourceAsStream(resource);
		if (inStream == null) sendLog("unable to find " + resource + " in .jar");
		else {
			try {
				outFile.getParentFile().mkdirs();
				OutputStream outStream = new FileOutputStream(outFile);
				byte[] buf = new byte[1024];
				int len;
				
				while ((len = inStream.read(buf)) > -1)
					if (len > 0) outStream.write(buf, 0, len);
				
				inStream.close();
				outStream.flush();
				outStream.close();
				
				return true;
			}
			catch (FileNotFoundException e) {
				sendLog("error opening file:");
			}
			catch (IOException e) {
				sendLog("io error writing file");
			}
		}
		return false;
	}
	
	private int reload() {
		Permissions = null;
		checkPermissions = true;
		Method = null;
		checkMethod = false;
		
		if (config == null) config = getConfiguration();
		else config.load();
		
		boolean saveConfig = false;
		for (CommandMessage message : CommandMessage.values()) {
			String key = "messages." + message.name().toLowerCase();
			String value = config.getString(key);
			if (value == null) {
				config.setProperty(key, message.toString());
				saveConfig = true;
			}
			else message.setMessage(value);
		}
		if (saveConfig && !config.save()) sendLog("error saving config file");
		
		Map<String, ConfigurationNode> nodes = config.getNodes("items");
		if (nodes == null || nodes.size() == 0) {
			sendLog("unable to find any items in config.yml; copying default from .jar");
			
			if (writeResource("/resources/config.yml", new File(getDataFolder(), "config.yml"))) {
				config.load();
				nodes = config.getNodes("items");
			}
		}
		
		Pattern pattern = Pattern.compile("\\d+(?::\\d+)?");
		for (Map.Entry<String, ConfigurationNode> entry : nodes.entrySet()) {
			ConfigurationNode node = entry.getValue();
			Boolean discard = false;
			List<String> ids = getStringAsList(node, "id");
			
			if (ids.size() == 0) discard = true;
			else for (String id : ids)
				if (!pattern.matcher(id).matches()) discard = true;
			
			if (node.getDouble("cost", -1) > 0) checkMethod = true;
			
			if (discard) {
				sendLog("error in item " + entry.getKey() + "; skipping");
				config.removeProperty("items." + entry.getKey());
			}
		}
		
		if (config.getDouble("cost", -1) > 0) checkMethod = true;
		
		if (checkMethod) {
			try {
				Class.forName("com.nijikokun.register.payment.Method");
			}
			catch (ClassNotFoundException e) {
				checkMethod = false;
				sendLog("cost associated with items; copying Registry library from .jar");
				if (writeResource("/resources/Register.jar", new File("lib", "Register.jar"))) sendLog("must RELOAD server to load Registry library (items will be FREE)");
			}
		}
		
		return config.getKeys("items").size();
	}
	
	public Boolean hasPermission(CommandSender sender, String node) {
		if (!(sender instanceof Player)) return true;
		
		Player player = (Player) sender;
		if (Permissions == null && checkPermissions) {
			checkPermissions = false;
			Plugin plugin = getServer().getPluginManager().getPlugin("Permissions");
			if (plugin != null && plugin.isEnabled()) Permissions = ((Permissions) plugin).getHandler();
			
			if (Permissions == null) sendLog("no compatible permissions plugin found, so defaulting to OPs only");
		}
		
		if (Permissions != null) return Permissions.has(player, node);
		return player.isOp();
	}
	
	public boolean hasMethod() {
		if (Method == null && checkMethod) {
			checkMethod = false;
			Methods Methods = new Methods();
			Plugin[] plugins = getServer().getPluginManager().getPlugins();
			for (Plugin plugin : plugins)
				if (plugin.isEnabled() && Methods.setMethod(plugin)) {
					Method = Methods.getMethod();
					break;
				}
			
			if (Method == null) sendLog("cost associated with items; no compatible ecomony plugin found, so items are FREE");
		}
		
		return Method != null;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		try {
			if (!hasPermission(sender, "giveto." + (command.getName().equalsIgnoreCase("giveme") ? "self" : "others"))) throw new CommandException(CommandMessage.PERMISSION);
			if (args.length == 0) {
				if (hasPermission(sender, "giveto.reload")) sendMsg(sender, CommandMessage.RELOADABLE.toString(label));
				throw new CommandException(CommandMessage.ARGLENGTH, command.getUsage().replace("<command>", label));
			}
			
			if (args[0].equalsIgnoreCase("reload")) {
				if (!hasPermission(sender, "giveto.reload")) throw new CommandException(CommandMessage.PERMISSION);
				sendMsg(sender, CommandMessage.RELOADDONE.toString(reload()), true);
				return true;
			}
			
			int lastArg = args.length - 1;
			int firstArg = 1;
			int count = 0;
			CommandSender to = sender;
			
			if (command.getName().equalsIgnoreCase("giveme")) firstArg--;
			else if (!args[0].equalsIgnoreCase("me")) {
				List<Player> matches = getServer().matchPlayer(args[0]);
				
				if (matches.size() == 0) throw new CommandException(CommandMessage.NOTARGET);
				if (matches.size() > 1) throw new CommandException(CommandMessage.TOOMANYTARGET, joinAsString(matches));
				to = matches.get(0);
			}
			
			if (!(to instanceof Player)) throw new CommandException(CommandMessage.CONSOLE);
			if (firstArg > lastArg) throw new CommandException(CommandMessage.ARGLENGTH, command.getUsage().replace("<command>", label));
			
			if (firstArg < lastArg && args[lastArg].matches("[1-9](?:[0-9]+)?")) {
				count = Integer.parseInt(args[lastArg]);
				lastArg--;
			}
			
			Item item = findItem(joinAsString(args, " ", firstArg, lastArg + 1).trim().toLowerCase());
			processItem(sender, count, item);
			giveItem(sender, (Player) to, item);
		}
		catch (CommandException e) {
			sendMsg(sender, e.getMessage());
		}
		
		return true;
	}
	
	private Item findItem(String query) throws CommandException {
		short durability = -1;
		String text = query;
		Matcher m = Pattern.compile("(.+):(\\d+)").matcher(query);
		if (m.matches()) {
			text = m.group(1);
			durability = Short.parseShort(m.group(2));
		}
		else query += ":-1";
		
		Pattern pattern;
		try {
			pattern = Pattern.compile(".*" + text.replaceAll("\\*", ".*").replaceAll(" ", ".* .*") + ".*", Pattern.CASE_INSENSITIVE);
		}
		catch (PatternSyntaxException e) {
			throw new CommandException(CommandMessage.BADSEARCH);
		}
		
		boolean isId = text.matches("\\d+");
		Set<Item> matches = new HashSet<Item>();
		Map<String, ConfigurationNode> nodes = config.getNodes("items");
		for (Map.Entry<String, ConfigurationNode> entry : nodes.entrySet()) {
			ConfigurationNode node = entry.getValue();
			Item item = new Item(entry.getKey(), durability, getStringAsList(node, "id"));
			
			if (isId) {
				if (item.ids.size() != 1) continue;
				String id = item.ids.get(0);
				
				if (id.equals(query)) return item;
				if (id.equals(text)) matches.add(item);
			}
			else {
				List<String> names = getStringAsList(node, "alias");
				names.add(item.name);
				
				for (String name : names) {
					if (text.equalsIgnoreCase(name)) return checkBetterMatch(item);
					if (pattern.matcher(name).matches()) matches.add(item);
				}
			}
		}
		
		if (matches.size() == 0) throw new CommandException(CommandMessage.NOMATCHES);
		if (matches.size() > 1) throw new CommandException(CommandMessage.TOOMANYMATCHES, Item.join(matches));
		
		Item item = matches.iterator().next();
		return isId ? item : checkBetterMatch(item);
	}
	
	private Item checkBetterMatch(Item item) {
		if (item.durability > -1 && item.ids.size() == 1) {
			try {
				Matcher m = Pattern.compile("(\\d+)(?::\\d+)?").matcher(item.ids.get(0));
				m.matches();
				return findItem(m.group(1) + ":" + item.durability);
			}
			catch (CommandException e) {}
		}
		return item;
	}
	
	private void processItem(CommandSender from, int count, Item item) throws CommandException {
		ConfigurationNode node = config.getNode("items." + item.name);
		if (count == 0) count = node.getInt("def", config.getInt("count.def", 64));
		count = Math.min(count, node.getInt("max", config.getInt("count.max", 512)));
		if (count < 1) count = 1;
		item.count = count;
		
		if (!(from instanceof Player)) return;
		
		String permission = node.getString("permission");
		if (permission != null && !hasPermission(from, "giveto.item." + permission)) throw new CommandException(CommandMessage.PERMISSION);
		
		String fromName = ((Player) from).getName();
		double cost = 0;
		MethodAccount account = null;
		
		if (!hasPermission(from, "giveto.exempt.cost")) {
			cost = node.getDouble("cost", -1);
			if (cost < 0) cost = config.getDouble("cost", 0);
			cost *= item.count;
			
			if (cost > 0 && hasMethod()) {
				if (!Method.hasAccount(fromName)) throw new CommandException(CommandMessage.NOACCOUNT);
				account = Method.getAccount(fromName);
				if (!account.hasEnough(cost)) throw new CommandException(CommandMessage.NOMONEY, Method.format(cost), Method.format(cost - account.balance()));
				item.costMsg = CommandMessage.SUBTRACTMONEY.toString(Method.format(cost), Method.format(account.balance()));
			}
			else cost = 0;
		}
		
		if (!hasPermission(from, "giveto.exempt.delay")) {
			String type = "";
			int delay = node.getInt("delay", -1);
			if (delay > -1) type = item.name;
			else delay = config.getInt("delay", 0);
			
			if (delay > 0) {
				String key = fromName + type;
				int time = (int) (System.currentTimeMillis() / 1000);
				if (delayPlayers.containsKey(key) && delayPlayers.get(key) > time) throw new CommandException(CommandMessage.DELAY, secondsToTime(delayPlayers.get(key) - time), type.equals("") ? "another item" : item.name + " again");
				delayPlayers.put(key, time + delay);
			}
		}
		
		if (cost > 0) account.subtract(cost);
	}
	
	private String secondsToTime(int seconds) {
		char[] symbol = { 'd', 'h', 'm', 's' };
		int[] increment = { 86400, 3600, 60, 1 };
		StringBuilder sb = new StringBuilder();
		int times;
		
		for (int i = 0; i < 4; i++) {
			if ((times = seconds / increment[i]) == 0 && sb.length() == 0) continue;
			sb.append(times);
			sb.append(symbol[i]);
			seconds %= increment[i];
		}
		
		return sb.toString();
	}
	
	private void giveItem(CommandSender from, Player to, Item item) throws CommandException {
		if (to != from && from instanceof Player) {
			sendMsg(to, CommandMessage.GIVEFROM.toString(((Player) from).getName(), item.name), true);
			sendMsg(from, CommandMessage.GIVETO.toString(item.name, to.getName() + "'s", item.costMsg), true);
		}
		else sendMsg(to, CommandMessage.GIVETO.toString(item.name, "your", item.costMsg), true);
		
		Boolean fullInventory = false;
		PlayerInventory inventory = to.getInventory();
		Pattern pattern = Pattern.compile(":");
		
		if (item.durability < 0) item.durability = 0;
		
		for (String id : item.ids) {
			String[] parts = pattern.split(id);
			fullInventory = !inventory.addItem(new ItemStack(Integer.parseInt(parts[0]), item.count, parts.length == 2 ? Short.parseShort(parts[1]) : item.durability)).isEmpty();
		}
		
		if (fullInventory) sendMsg(to, CommandMessage.INVENTORYFULL.toString());
	}
	
	private String joinAsString(List<Player> players) {
		List<String> strings = new ArrayList<String>();
		for (Player player : players)
			strings.add(player.getName());
		
		return joinAsString(strings.toArray(), ", ", 0, strings.size());
	}
	
	private String joinAsString(Object[] objects, String separator, int first, int last) {
		StringBuilder sb = new StringBuilder(objects[first].toString());
		for (int i = first + 1; i < last; i++)
			sb.append(separator + objects[i].toString());
		
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	private List<String> getStringAsList(ConfigurationNode node, String key) {
		Object o = node.getProperty(key);
		if (o instanceof List) return (List<String>) o;
		
		List<String> ids = new ArrayList<String>();
		if (o != null) ids.add(o.toString());
		return ids;
	}
}
