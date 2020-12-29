package com.github.magiccheese1.damageindicator;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

import com.github.magiccheese1.damageindicator.Utils.EntityHider;
import com.github.magiccheese1.damageindicator.Utils.Utility;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class DamageIndicatorListener implements Listener {
  private JavaPlugin plugin;
  private EntityHider entityHider;
  List<ArmorStand> toBeRemovedArmorstands;

  DamageIndicatorListener(JavaPlugin plugin, EntityHider entityHider, List<ArmorStand> toBeRemovedArmorstands) {
    this.plugin = plugin;
    this.entityHider = entityHider;
    this.toBeRemovedArmorstands = toBeRemovedArmorstands;
  }

  @EventHandler
  public void entityDamageByEntity(EntityDamageByEntityEvent event) {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      final Player damager;
      final DecimalFormat damageFormat;
      final double multiplier;
      if (event.isCancelled())
        return;
      // Don't show indicator if the damagee is an armor stand
      if (event.getEntity().getType() == EntityType.ARMOR_STAND)
        return;

      if (event.getDamager().getType() == EntityType.PLAYER) {
        multiplier = -0.5;
        damager = (Player) event.getDamager();
        damageFormat = Utility.getDamageFormat(plugin, Utility.isCritical(damager));
      } else if (event.getDamager() instanceof Projectile) {
        multiplier = -0.7;
        if (!(((Projectile) event.getDamager()).getShooter() instanceof Player))
          return;
        damager = (Player) ((Projectile) event.getDamager()).getShooter();
        damageFormat = Utility.getDamageFormat(plugin,
            event.getDamager() instanceof Arrow ? ((Arrow) event.getDamager()).isCritical() : false);

      } else
        return;
      Random random = new Random();
      Location _spawnLocation;
      // Tries random positions until it finds one that is not inside a block
      _spawnLocation = event.getEntity().getLocation()
          .add(random.nextDouble() * (1.0 + 1.0) - 1.0, 1, random.nextDouble() * (1.0 + 1.0) - 1.0)
          .add(damager.getLocation().getDirection()
              .multiply(event.getEntity().getLocation().distance(damager.getLocation())).multiply(multiplier));

      final Location spawnLocation = _spawnLocation;
      Bukkit.getScheduler().runTask(plugin, () -> {
        // Spawn an invisible armor stand
        final ArmorStand armorStand = (ArmorStand) spawnLocation.getWorld().spawn(spawnLocation, ArmorStand.class,
            new InvisibleArmorStand(plugin, damager, entityHider, plugin.getConfig().getBoolean("ShowToDamagerOnly")));

        // Set visible name
        armorStand.setCustomName(String.valueOf(damageFormat.format(event.getFinalDamage())));
        armorStand.setCustomNameVisible(true);

        // Destroy the armor stand after 3 sec
        toBeRemovedArmorstands.add(armorStand);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
          armorStand.remove();
          toBeRemovedArmorstands.remove(armorStand);
        }, 30);
      });
    });
  }
}