package lazini;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class EnderSurfer2 extends JavaPlugin implements Listener {
	// XXX Elytra fall damage is dealt correctly now
	// XXX Fixed enderpearls not getting consumed
	// XXX Fixed cool down issue
	// XXX Added sounds
	// XXX Velocity multiplier defaults to 4
	// Variables
	private static EnderSurfer instance;
	int health;
	int velMult;
	boolean dmgOnAir;
	boolean showParticles;
	private Random random;
	// Sets go here.
	private HashSet<UUID> list;
	private HashSet<UUID> threwE;
	private HashSet<UUID> particles;
	private List<String> disabledWorlds;
//	private HashMap<String, Long> cooldown;
//	private ItemStack enderpearl;

	@Override
	public void onEnable() {
		super.onEnable();
		getServer().getPluginManager().registerEvents(this, this);
		getConfig().options().copyDefaults(true);
		saveConfig();
//		instance = this;
		Commands cmds = new Commands(); // Memory saver
		getCommand("endersurfer").setExecutor(cmds);
		getCommand("es").setExecutor(cmds);
		health = getConfig().getInt("half-hearts");
		velMult = getConfig().getInt("vel-mult");
		dmgOnAir = getConfig().getBoolean("dmg-on-air");
		showParticles = getConfig().getBoolean("show-particles");
		random = new Random();
		list = new HashSet<>();
		threwE = new HashSet<>();
		particles = new HashSet<>();
		disabledWorlds = this.getConfig().getStringList("disabled-for-worlds");
//		cooldown = new HashMap<>(100);
//		enderpearl = new ItemStack(Material.ENDER_PEARL);
	}

	@Override
	public void onDisable() {
		super.onDisable();
		list.clear();
		threwE.clear();
		particles.clear();
		disabledWorlds.clear();
//		cooldown.clear();
	}

	@EventHandler
	public void onThrowEnderPearl(ProjectileLaunchEvent event) {
		if (!(event.getEntity().getShooter() instanceof Player) || !(event.getEntity() instanceof EnderPearl)
				|| !((Player) event.getEntity().getShooter()).hasPermission("endersurfer.utilize")) {
			return;
		}
		Player shooter = (Player) event.getEntity().getShooter();

		UUID shooterUUID;

//		String name = shooter.getName();
		/*long time = System.currentTimeMillis();
		if (!cooldown.containsKey(name)) {
			cooldown.put(name, time);
		} else {
			if (time < cooldown.get(name) + 1000) {
				event.setCancelled(true);
				return;
			}
			cooldown.put(name, time);
		}
		*/
		if (shooter.isSneaking() || ((shooter.isGliding() && !shooter.hasPermission("endersurfer.elytra"))))
			return;

		for (String str : disabledWorlds) {
			if (shooter.getWorld().getName().equalsIgnoreCase(str)) {
				// shooter.sendMessage("Disalbed for world: " + str);
				return;
			}
		}

		if (shooter.isOnGround()) {
			Location endermiteLoc = event.getEntity().getLocation();
			if (random.nextInt(100) < 5) {
				event.getEntity().getWorld().spawnEntity(endermiteLoc, EntityType.ENDERMITE);
			}
		}

//		this.reloadConfig();
//		health = getConfig().getInt("half-hearts");
//		velMult = getConfig().getInt("vel-mult");
//		dmgOnAir = getConfig().getBoolean("dmg-on-air");
//		showParticles = getConfig().getBoolean("show-particles");
		Vector velocity = event.getEntity().getLocation().getDirection();

		if (shooter.isOnGround() && (shooter.getLocation().getPitch() < 15)) {
			Location loc = shooter.getLocation();
			loc.setY(loc.getY() + .5);
			shooter.teleport(loc);
		}

		velocity = velocity.setY(-velocity.getY() * velMult);
		velocity = velocity.setX(-velocity.getX() * velMult);
		velocity = velocity.setZ(velocity.getZ() * velMult);
//		velocity = velocity.multiply(velMult);
		shooter.setVelocity(velocity);
		shooterUUID = shooter.getUniqueId();
		shooter.playSound(shooter.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 2, .7f);
		Location v = new Location(shooter.getWorld(), shooter.getLocation().getX(), -2000f, shooter.getLocation().getZ());
//		event.getEntity().setVelocity(new Vector(0, 0, 0));
//		event.getEntity().setGravity(false);
		event.getEntity().teleport(v);

//		event.getEntity().setVelocity(new Vector(0, 0, 0));
//		Bukkit.getServer().getPlayer(shooterUUID).getNearbyEntities(1, 1, 1).remove(event.getEntity());
//		shooter.getItemInHand().setAmount(shooter.getItemInHand().getAmount() - 1); // XXX DOESNT WORK IF ENDER PEARL IS ONLY 1
//		shooter.getInventory().setItem(shooter.getInventory().getHeldItemSlot(), );
//		shooter.getInventory().remove(is);
		/* ItemStack stack = shooter.getInventory().getItem(shooter.getInventory().getHeldItemSlot());
		
		 if (shooter.getInventory().getItemInOffHand() instanceof EnderPearl) {
			stack = shooter.getInventory().getItemInOffHand();
			if (stack.getAmount() == 1)
				shooter.getInventory().clear(-106);
			else
				stack.setAmount(stack.getAmount() - 1);
		} else {
			if (stack.getAmount() == 1)
				shooter.getInventory().clear(shooter.getInventory().getHeldItemSlot());
			else
				stack.setAmount(stack.getAmount() - 1);
		}
		*/
		if (dmgOnAir) {
			if (!shooter.hasPermission("endersurfer.defydamageonair"))
				handlePlayerDamage(shooter, health);
			if (!list.contains(shooterUUID))
				list.add(shooterUUID);
		} else {
			if (!threwE.contains(shooterUUID))
				threwE.add(shooterUUID);
		}

		if (!particles.contains(shooterUUID) && (showParticles && shooter.hasPermission("endersurfer.showparticles"))) {
			// shooter.sendMessage(ChatColor.BLUE + "Added to the particles list.");
			particles.add(shooterUUID);
			// shooter.sendMessage("Should now be added to the 'particles' list via throw enderpearl");
		}

	}

	@EventHandler
	public void onGetDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)
				//	|| event.getEntity() != shooter
				|| !((Player) event.getEntity()).hasPermission("endersurfer.utilize"))
			return;
		Player shooter = (Player) event.getEntity();
		UUID shooterUUID = shooter.getUniqueId();

//		health = getConfig().getInt("half-hearts");
		if (event.getCause() == DamageCause.FALL) {
			if (list.contains(shooterUUID)) {
				list.remove(shooterUUID);
				event.setCancelled(true);
			}

			if (threwE.contains(shooterUUID)) {
				if (!shooter.hasPermission("endersurfer.defyfalldamage"))
					handlePlayerDamage(shooter, health);
				threwE.remove(shooterUUID);
				event.setCancelled(true);
			}
			if (particles.contains(shooterUUID)) {
				// shooter.sendMessage(ChatColor.BLUE + "Removed from the particles list.");
				particles.remove(shooterUUID);
				// shooter.sendMessage("Should now be removed from the 'particles' list via get Damage");
				if (shooter.isOnGround()) {
					Location endermiteLoc = event.getEntity().getLocation();
					if (random.nextInt(100) < 5) {
						event.getEntity().getWorld().spawnEntity(endermiteLoc, EntityType.ENDERMITE);
					}
				}
			}
		}
	}

	/** Handles particles and elytra gliding */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerFlyWithEnderPearl(PlayerMoveEvent event) {
		if (!(event.getPlayer() instanceof Player) || !(event.getPlayer()).hasPermission("endersurfer.utilize"))
			return;
		Player shooter = event.getPlayer();
		UUID shooterUUID = shooter.getUniqueId();
//		reloadConfig();
//		showParticles = getConfig().getBoolean("show-particles");

		if (shooter.isGliding()) {
			list.remove(shooterUUID);
			threwE.remove(shooterUUID);
		}

		if (particles.contains(shooterUUID)) {
			if (shooter.isOnGround()) {
				Vector tempVel = shooter.getVelocity().normalize();
				if (tempVel.getX() < 0.1 && tempVel.getX() > -0.1) {
					if (tempVel.getZ() < 0.1 && tempVel.getZ() > -0.1) {
						// if (tempVel.getY())
						if (shooter.getLocation().getPitch() > -10 && shooter.getLocation().getPitch() <= 90) {
							/*if (threwE.contains(shooterUUID))
								threwE.remove(shooterUUID);
							else if (list.contains(shooterUUID))
								list.remove(shooterUUID);
							*/
							particles.remove(shooterUUID);
							// shooter.sendMessage("Move method deleted you");
							return;
						}
					}
				}
			}
			if (shooter.hasPermission("endersurfer.showparticles") && showParticles) {
				// shooter.sendMessage(ChatColor.BLUE + "Showing particles for the player.");
				showEnderParticles(shooter);
			}
		}
	}

	public static EnderSurfer getInstance() {
		return instance;
	}

	private void handlePlayerDamage(Player shooter, double health) {
		if (health == 0)
			return;
		shooter.damage(health);
	}

	private void showEnderParticles(Player shooter) {
		Location loc = shooter.getLocation();
		loc.setY(loc.getY() + 1);
//		ParticleEffect.PORTAL.display(.5f, .5f, .5f, .5f, 20, loc, 40);
		shooter.spawnParticle(Particle.PORTAL, loc, 70);
	}
}