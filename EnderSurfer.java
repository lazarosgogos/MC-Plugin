package lazini;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class EnderSurfer extends JavaPlugin implements Listener {
	private static EnderSurfer instance;

	int health;
	int velMult;
	boolean dmgOnAir;
	boolean showParticles;

	private Random random;

	private HashSet<UUID> list;
	private HashSet<UUID> threwE;
	private HashSet<UUID> particles;
	private List<String> disabledWorlds;
	private HashMap<UUID, Collection<AttributeModifier>> modifiers;

	@Override
	public void onEnable() {
		super.onEnable();
		getServer().getPluginManager().registerEvents(this, this);
		getConfig().options().copyDefaults(true);
		saveConfig();
		instance = this;
		Commands cmds = new Commands();
		getCommand("endersurfer").setExecutor(cmds);
		getCommand("es").setExecutor(cmds);
		this.health = getConfig().getInt("half-hearts");
		this.velMult = getConfig().getInt("vel-mult");
		this.dmgOnAir = getConfig().getBoolean("dmg-on-air");
		this.showParticles = getConfig().getBoolean("show-particles");
		this.random = new Random();
		this.list = new HashSet<>();
		this.threwE = new HashSet<>();
		this.particles = new HashSet<>();
		this.modifiers = new HashMap<>();
		this.disabledWorlds = getConfig().getStringList("disabled-for-worlds");
	}

	@Override
	public void onDisable() {
		super.onDisable();
		this.list.clear();
		this.threwE.clear();
		this.particles.clear();
		this.disabledWorlds.clear();
		this.modifiers.clear();
	}

	@EventHandler
	public void onThrowEnderPearl(ProjectileLaunchEvent event) {
		if (!(event.getEntity().getShooter() instanceof Player) || !(event.getEntity() instanceof org.bukkit.entity.EnderPearl)
				|| !((Player) event.getEntity().getShooter()).hasPermission("endersurfer.utilize"))
			return;
		Player shooter = (Player) event.getEntity().getShooter();
		if (shooter.isSneaking() || (shooter.isGliding() && !shooter.hasPermission("endersurfer.elytra")))
			return;
		for (String str : this.disabledWorlds) {
			if (shooter.getWorld().getName().equalsIgnoreCase(str))
				return;
		}
		if (shooter.isOnGround()) {
			Location endermiteLoc = event.getEntity().getLocation();
			if (this.random.nextInt(100) < 5) {
				Endermite endermite = (Endermite) event.getEntity().getWorld().spawnEntity(endermiteLoc, EntityType.ENDERMITE);
				endermite.setPlayerSpawned(true);
			}
		}
		Vector velocity = event.getEntity().getLocation().getDirection();
		saveKnockbackResistanceToList(shooter);
		velocity = velocity.setY(-velocity.getY() * this.velMult);
		velocity = velocity.setX(-velocity.getX() * this.velMult);
		velocity = velocity.setZ(velocity.getZ() * this.velMult);
		shooter.setVelocity(velocity);
		UUID shooterUUID = shooter.getUniqueId();
		shooter.playSound(shooter.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 0.7F);
		Location v = new Location(shooter.getWorld(), shooter.getLocation().getX(), -2000.0D, shooter.getLocation().getZ());
		event.getEntity().teleport(v);
		if (this.dmgOnAir) {
			if (!shooter.hasPermission("endersurfer.defydamageonair"))
				handlePlayerDamage(shooter, this.health);
			if (!this.list.contains(shooterUUID))
				this.list.add(shooterUUID);
		} else if (!this.threwE.contains(shooterUUID)) {
			this.threwE.add(shooterUUID);
		}
		if (!this.particles.contains(shooterUUID) && this.showParticles && shooter.hasPermission("endersurfer.showparticles"))
			this.particles.add(shooterUUID);
	}

	@EventHandler
	public void onGetDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player)
			if (((Player) event.getEntity()).hasPermission("endersurfer.utilize")) {
				Player shooter = (Player) event.getEntity();
				UUID shooterUUID = shooter.getUniqueId();
				if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
					boolean flag = false;
					if (this.list.contains(shooterUUID)) {
						this.list.remove(shooterUUID);
						flag = true;
						event.setCancelled(true);
					}
					if (this.threwE.contains(shooterUUID) && !shooter.hasPermission("endersurfer.defyfalldamage")) {
						handlePlayerDamage(shooter, this.health);
						event.setCancelled(true);
						this.threwE.remove(shooterUUID);
						flag = true;
					}
					if (shooter.isOnGround() && flag) {
						Location endermiteLoc = event.getEntity().getLocation();
						if (this.random.nextInt(100) < 5) {
							Endermite e = (Endermite) event.getEntity().getWorld().spawnEntity(endermiteLoc,
									EntityType.ENDERMITE);
							e.setPlayerSpawned(true);
						}
					}
				}
				return;
			}
	}

	@EventHandler
	public void onPlayerFlyWithEnderPearl(PlayerMoveEvent event) {
		if (!(event.getPlayer() instanceof Player) || !event.getPlayer().hasPermission("endersurfer.utilize"))
			return;
		Player shooter = event.getPlayer();
		UUID shooterUUID = shooter.getUniqueId();
		if (shooter.isGliding()) {
			this.list.remove(shooterUUID);
			this.threwE.remove(shooterUUID);
		}
		if (shooter.isOnGround()) {
			giveBackKnockbackResistanceAttrubute(shooter);
			if (this.particles.contains(shooterUUID)) {
				Vector tempVel = shooter.getVelocity().normalize();
				if (tempVel.getX() < 0.1D && tempVel.getX() > -0.1D && tempVel.getZ() < 0.1D && tempVel.getZ() > -0.1D
						&& shooter.getLocation().getPitch() > -10.0F && shooter.getLocation().getPitch() <= 90.0F) {
					this.particles.remove(shooterUUID);
					return;
				}
			}
		} else if (shooter.hasPermission("endersurfer.showparticles") && this.showParticles
				&& this.particles.contains(shooterUUID)) {
			showEnderParticles(shooter);
		}
	}

	public static EnderSurfer getInstance() {
		return instance;
	}

	private void handlePlayerDamage(Player shooter, double health) {
		if (health == 0.0D)
			return;
		double temp = shooter.getHealth() - health;
		if (temp < 0.0D)
			temp = 0.0D;
		if (this.dmgOnAir) {
			shooter.setHealth(temp);
		} else {
			shooter.damage(health);
		}
		shooter.damage(0.0D);
		shooter.setLastDamageCause(new EntityDamageEvent(shooter, EntityDamageEvent.DamageCause.FALL, health));
	}

	private void saveKnockbackResistanceToList(Player shooter) {
		UUID shooterUUID = shooter.getUniqueId();
		Collection<AttributeModifier> collection = shooter.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).getModifiers();
		this.modifiers.put(shooterUUID, collection);
		for (AttributeModifier modifier : collection)
			shooter.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).removeModifier(modifier);
	}

	private void giveBackKnockbackResistanceAttrubute(Player shooter) {
		UUID shooterUUID = shooter.getUniqueId();
		if (this.modifiers.containsKey(shooterUUID)) {
			Collection<AttributeModifier> collection = this.modifiers.get(shooterUUID);
			for (AttributeModifier modifier : collection)
				if (!shooter.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).getModifiers().contains(modifier))
					shooter.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).addModifier(modifier);
			this.modifiers.remove(shooterUUID);
		}
	}

	private void showEnderParticles(Player shooter) {
		Location loc = shooter.getLocation();
		loc.setY(loc.getY() + 1.0D);
		shooter.spawnParticle(Particle.PORTAL, loc, 70);
	}
}
