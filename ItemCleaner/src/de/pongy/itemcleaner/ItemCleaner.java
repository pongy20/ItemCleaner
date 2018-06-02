package de.pongy.itemcleaner;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class ItemCleaner extends JavaPlugin implements CommandExecutor {

	boolean messagePlayers;
	String prefix = "";
	String msg = "";
	ArrayList<World> worlds = new ArrayList<>();
	int interval = 0;
	
	@Override
	public void onEnable() {
		registerConfig();
		startCountdown();
		
		this.getCommand("itemcleaner").setExecutor(this);
	}
	@SuppressWarnings("unchecked")
	private void registerConfig() {
		saveConfig();
		saveDefaultConfig();
		
		getConfig().options().copyDefaults(true);
		getConfig().addDefault("interval", 60);
		getConfig().addDefault("prefix", "&cIC &8:");
		getConfig().addDefault("message", "&0 items on ground have been removed!");
		getConfig().addDefault("messagePlayers", true);
		ArrayList<String> SavedWorlds = new ArrayList<>();
		SavedWorlds.add(Bukkit.createWorld(new WorldCreator("world")).getName());
		getConfig().addDefault("worlds", SavedWorlds);
		
		
		messagePlayers = getConfig().getBoolean("messagePlayers");
		prefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("prefix"));
		msg= ChatColor.translateAlternateColorCodes('&', getConfig().getString("message"));
		ArrayList<String> WorldArrayString = (ArrayList<String>) getConfig().get("worlds");
		for (String s : WorldArrayString) {
			worlds.add(Bukkit.createWorld(new WorldCreator(s)));
		}
		interval = getConfig().getInt("interval");
		saveConfig();
	}
	@SuppressWarnings("deprecation")
	private void startCountdown() {
		if (worlds.size() != 0) {
			Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
				
				@Override
				public void run() {
					for (World w : worlds) {
						clearWorld(w);
					}
				}
			}, 0, 20*interval);
		} else {
			System.out.println("Countdown can't start becuase there are no worlds found in config!");
		}
	}
	private void clearWorld(World w) {
		int amount = 0; 
		for (Entity e : w.getEntities()) {
			if (e instanceof Item) {
				e.remove();
				amount += 1;
			}
			if (e instanceof Player) {
				if (messagePlayers) {
					e.sendMessage(prefix + msg);
				}
			}
		}
		System.out.println(amount + " items have been cleared in world: " + w.getName());
	}
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if (args.length == 0) {
			if (sender instanceof Player) {
				Player p = (Player) sender;
				if (p.hasPermission("itemcleaner.remove")) {
					clearWorld(p.getWorld());
					p.sendMessage(ChatColor.BLUE + "Items have been removed in World " + p.getWorld());
				}
			}
		} else if (args.length == 1) {
			if (sender.hasPermission("itemcleaner.remove"))
			if (args[0].equalsIgnoreCase("all")) {
				for (World w : worlds) {
					clearWorld(w);
				}
				sender.sendMessage(ChatColor.RED + "All worlds have been removed!");
			}
		} else {
			sender.sendMessage("Syntax error!");
		}
		
		return true;
	}
}