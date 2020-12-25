package lazini;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class Commands implements CommandExecutor, TabCompleter {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("endersurfer") || label.equalsIgnoreCase("es")) {
			EnderSurfer plugin = EnderSurfer.getInstance();
			int health = plugin.getConfig().getInt("half-hearts");
			String strHealth = Integer.toString(health);
			int velMult = plugin.getConfig().getInt("vel-mult");
			String strVelMult = Integer.toString(velMult);
			boolean dmgOnAir = plugin.getConfig().getBoolean("dmg-on-air");
			String strDmgOnAir = Boolean.toString(dmgOnAir);
			boolean showParticles = plugin.getConfig().getBoolean("show-particles");
			String strShowParticles = Boolean.toString(showParticles);
			if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
				sender.sendMessage(ChatColor.RED + "/setHearts" + ChatColor.GREEN
						+ ": Set how much damage will be caused to the shooter " + ChatColor.ITALIC + "(count in half hearts)");
				sender.sendMessage(ChatColor.RED + "/setDamageOnAir" + ChatColor.GREEN
						+ ": Define if the damage will be dealt while the shooter is in the air or when they land");
				sender.sendMessage(ChatColor.RED + "/setVelocityMultiplier" + ChatColor.GREEN
						+ ": Set the velocity multiplier. In simple words, the bigger, the faster" + ChatColor.ITALIC
						+ "(setting it to 8 is fine, avoid bigger numbers)");
			} else if (args.length == 1) {
				if (args[0].equalsIgnoreCase("sethearts")) {
					plugin.reloadConfig();
					sender.sendMessage(ChatColor.GOLD + strHealth + ChatColor.GREEN
							+ " half hearts will be lost when someone hits the ground!");
					return true;
				}
				if (args[0].equalsIgnoreCase("setVelocityMultiplier")) {
					plugin.reloadConfig();
					sender.sendMessage(ChatColor.GREEN + "The current velocity multiplier is " + ChatColor.GOLD + strVelMult);
					return true;
				}
				if (args[0].equalsIgnoreCase("setDamageOnAir")) {
					plugin.reloadConfig();
					sender.sendMessage(ChatColor.GREEN + "Currently damaging the player while in the air is set to "
							+ ChatColor.GOLD + strDmgOnAir);
					return true;
				}
				if (args[0].equalsIgnoreCase("showParticles")) {
					plugin.reloadConfig();
					sender.sendMessage(ChatColor.GREEN + "Currently damaging the player while in the air is set to "
							+ ChatColor.GOLD + strShowParticles);
					return true;
				}
			} else if (args.length == 2) {
				if (args[0].equalsIgnoreCase("sethearts")) {
					plugin.reloadConfig();
					health = Integer.parseInt(args[1]);
					plugin.getConfig().set("half-hearts", Integer.valueOf(health));
					strHealth = Integer.toString(health);
					plugin.saveConfig();
					sender.sendMessage(ChatColor.GREEN + "Configuration file updated!" + ChatColor.GOLD + " (half-hearts: "
							+ strHealth + ")");
					return true;
				}
				if (args[0].equalsIgnoreCase("setVelocityMultiplier")) {
					plugin.reloadConfig();
					velMult = Integer.parseInt(args[1]);
					plugin.getConfig().set("vel-mult", Integer.valueOf(velMult));
					strVelMult = Integer.toString(velMult);
					plugin.saveConfig();
					sender.sendMessage(
							ChatColor.GREEN + "Configuration file updated!" + ChatColor.GOLD + " (vel-mult: " + strVelMult + ")");
					return true;
				}
				if (args[0].equalsIgnoreCase("setDamageOnAir")) {
					plugin.reloadConfig();
					dmgOnAir = Boolean.parseBoolean(args[1]);
					plugin.getConfig().set("dmg-on-air", Boolean.valueOf(dmgOnAir));
					strDmgOnAir = Boolean.toString(dmgOnAir);
					plugin.saveConfig();
					sender.sendMessage(ChatColor.GREEN + "Configuration file updated!" + ChatColor.GOLD + " (dmg-on-air: "
							+ strDmgOnAir + ")");
					return true;
				}
				if (args[0].equalsIgnoreCase("showParticles")) {
					plugin.reloadConfig();
					showParticles = Boolean.parseBoolean(args[1]);
					plugin.getConfig().set("show-particles", Boolean.valueOf(showParticles));
					strShowParticles = Boolean.toString(showParticles);
					plugin.saveConfig();
					sender.sendMessage(ChatColor.GREEN + "Configuration file updated!" + ChatColor.GOLD + " (show-particles: "
							+ strShowParticles + ")");
					return true;
				}
			} else if (args.length > 2) {
				sender.sendMessage(ChatColor.RED + "Too many arguments, don't you think?");
				return false;
			}
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("endersurfer") || label.equalsIgnoreCase("es")) {
			ArrayList<String> arguments = new ArrayList<>();
			String[] completions = { "setHearts", "setVelocityMultiplier", "setDamageOnAir", "showParticles", "help" };
			String[] completions2 = { "true", "false" };
			if (args.length == 1) {
				if (args[0].equals("")) {
					byte b;
					int i;
					String[] arrayOfString;
					for (i = (arrayOfString = completions).length, b = 0; b < i;) {
						String p = arrayOfString[b];
						arguments.add(p.toString());
						b++;
					}
				} else {
					byte b;
					int i;
					String[] arrayOfString;
					for (i = (arrayOfString = completions).length, b = 0; b < i;) {
						String p = arrayOfString[b];
						if (p.toLowerCase().startsWith(args[0].toLowerCase()))
							arguments.add(p.toString());
						b++;
					}
				}
				Collections.sort(arguments);
				return arguments;
			}
			if (args.length == 2)
				if (args[0].equalsIgnoreCase("setDamageOnAir") || args[0].equalsIgnoreCase("showParticles")) {
					if (!args[1].equals("")) {
						byte b;
						int i;
						String[] arrayOfString;
						for (i = (arrayOfString = completions2).length, b = 0; b < i;) {
							String p = arrayOfString[b];
							if (p.toLowerCase().startsWith(args[0].toLowerCase()))
								arguments.add(p.toString());
							b++;
						}
					} else {
						byte b;
						int i;
						String[] arrayOfString;
						for (i = (arrayOfString = completions).length, b = 0; b < i;) {
							String p = arrayOfString[b];
							arguments.add(p.toString());
							b++;
						}
					}
					Collections.sort(arguments);
					return arguments;
				}
			arguments.clear();
		}
		return null;
	}
}
