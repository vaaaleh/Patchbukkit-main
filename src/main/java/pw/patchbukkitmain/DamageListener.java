package pw.patchbukkitmain;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

public class DamageListener implements Listener {
    private final Field fieldPlayerConnection;
    private final Method sendPacket;
    private final Constructor<?> packetVelocity;

    public DamageListener() {
        try {
            String version = KnockbackPatch.getInstance().getCraftBukkitVersion();
            Class<?> entityPlayerClass = Class.forName("net.minecraft.server." + version + ".EntityPlayer");
            Class<?> packetVelocityClass = Class.forName("net.minecraft.server." + version + ".PacketPlayOutEntityVelocity");
            Class<?> playerConnectionClass = Class.forName("net.minecraft.server." + version + ".PlayerConnection");
            fieldPlayerConnection = entityPlayerClass.getField("playerConnection");
            sendPacket = playerConnectionClass.getMethod("sendPacket", packetVelocityClass.getSuperclass());
            packetVelocity = packetVelocityClass.getConstructor(Integer.TYPE, Double.TYPE, Double.TYPE, Double.TYPE);
        } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException("Failed to initialize DamageListener", e);
        }
    }

    @EventHandler
    public void onPlayerVelocity(PlayerVelocityEvent event) {
        Player player = event.getPlayer();
        EntityDamageEvent lastDamage = player.getLastDamageCause();
        if (lastDamage == null || !(lastDamage instanceof EntityDamageByEntityEvent)) {
            return;
        }
        if (((EntityDamageByEntityEvent) lastDamage).getDamager() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }
        Player damaged = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();
        if (damaged.getNoDamageTicks() > damaged.getMaximumNoDamageTicks() / 2.0) {
            return;
        }
        double horMultiplier = KnockbackPatch.getInstance().getHorMultiplier();
        double verMultiplier = KnockbackPatch.getInstance().getVerMultiplier();
        double sprintMultiplier = damager.isSprinting() ? 0.8 : 0.5;
        double kbMultiplier = damager.getItemInHand() == null ? 0.0 : damager.getItemInHand().getEnchantmentLevel(Enchantment.KNOCKBACK) * 0.2;
        double airMultiplier = damaged.isOnGround() ? 1.0 : 0.5;
        Vector knockback = damaged.getLocation().toVector().subtract(damager.getLocation().toVector()).normalize();
        knockback.setX((knockback.getX() * sprintMultiplier + kbMultiplier) * horMultiplier);
        knockback.setY(0.35 * airMultiplier * verMultiplier);
        knockback.setZ((knockback.getZ() * sprintMultiplier + kbMultiplier) * horMultiplier);
        try {
            Object entityPlayer = damaged.getClass().getMethod("getHandle").invoke(damaged);
            Object playerConnection = fieldPlayerConnection.get(entityPlayer);
            Object packet = packetVelocity.newInstance(damaged.getEntityId(), knockback.getX(), knockback.getY(), knockback.getZ());
            sendPacket.invoke(playerConnection, packet);
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException
                 | SecurityException | InvocationTargetException e) {
            throw new RuntimeException("Failed to apply knockback", e);
        }
    }
}
