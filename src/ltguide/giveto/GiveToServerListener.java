package ltguide.giveto;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServerListener;

class GiveToServerListener extends ServerListener {
	private GiveTo plugin;
	
	public GiveToServerListener(GiveTo plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onPluginDisable(PluginDisableEvent event) {
		if (plugin.Method != null && plugin.Method.getPlugin().equals(event.getPlugin())) {
			plugin.Method = null;
			plugin.checkMethod = true;
		}
		else if (plugin.Permissions != null && event.getPlugin().getDescription().getName().equalsIgnoreCase("Permissions")) {
			plugin.Permissions = null;
			plugin.checkPermissions = true;
		}
	}
	
}
