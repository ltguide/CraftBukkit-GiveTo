package ltguide.giveto;

import ltguide.nijikokun.register.payment.Methods;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServerListener;

class GiveToServerListener extends ServerListener {
	private GiveTo plugin;
	
	public GiveToServerListener(GiveTo plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onPluginDisable(PluginDisableEvent event) {
		if (Methods.hasMethod() && Methods.getMethod().getPlugin().equals(event.getPlugin())) {
			Methods.reset();
			plugin.checkMethod = true;
		}
		else if (plugin.Permissions != null && event.getPlugin().getDescription().getName().equalsIgnoreCase("Permissions")) {
			plugin.Permissions = null;
			plugin.checkPermissions = true;
		}
	}
	
}
