package com.github.magiccheese1.damageindicator.Utils;

import java.text.DecimalFormat;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

public class Utility {

  /**
   * Determine if the direct hit was a critical hit
   * 
   * @param damager - The damaging player
   */
  public static boolean isCritical(Player damager) {
    return damager.getAttackCooldown() == 1F && damager.getFallDistance() > 0.0F
        && !damager.getLocation().getBlock().isLiquid() && !damager.getActivePotionEffects().stream()
            .filter(o -> o.getType().equals(PotionEffectType.BLINDNESS)).findFirst().isPresent()
        && damager.getVehicle() == null && !damager.isSprinting();
  }

  public static DecimalFormat getDamageFormat(Plugin plugin, boolean critical) {
    return new DecimalFormat(
        ChatColor.translateAlternateColorCodes('&', critical ? plugin.getConfig().getString("CriticalIndicatorFormat")
            : plugin.getConfig().getString("IndicatorFormat")));
  }
}
