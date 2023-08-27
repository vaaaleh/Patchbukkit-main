package pw.patchbukkitmain;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class KnockbackPatch extends JavaPlugin {
    private static KnockbackPatch instance;
    private String craftBukkitVersion;
    private double horMultiplier = 1.0;
    private double verMultiplier = 1.0;

    public void onEnable() {
        instance = this;
        this.getConfig().options().copyDefaults(true);
        this.getConfig().addDefault("knockback-multiplier.horizontal", 1.0);
        this.getConfig().addDefault("knockback-multiplier.vertical", 1.0);
        this.saveConfig();
        this.craftBukkitVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        this.horMultiplier = this.getConfig().getDouble("knockback-multiplier.horizontal");
        this.verMultiplier = this.getConfig().getDouble("knockback-multiplier.vertical");
        Bukkit.getPluginManager().registerEvents(new DamageListener(), this);
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("knockbackpatch.setknockback")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <horizontal multiplier> <vertical multiplier>.");
            return true;
        }
        double horMultiplier = NumberUtils.toDouble(args[0], -1.0);
        double verMultiplier = NumberUtils.toDouble(args[1], -1.0);
        if (horMultiplier < 0.0 || verMultiplier < 0.0) {
            sender.sendMessage(ChatColor.RED + "Invalid horizontal/vertical multiplier!");
            return true;
        }
        this.horMultiplier = horMultiplier;
        this.verMultiplier = verMultiplier;
        this.getConfig().set("knockback-multiplier.horizontal", horMultiplier);
        this.getConfig().set("knockback-multiplier.vertical", verMultiplier);
        this.saveConfig();
        sender.sendMessage(ChatColor.GREEN + "Successfully updated the knockback multipliers!");
        return true;
    }

    public static KnockbackPatch getInstance() {
        return instance;
    }

    public String getCraftBukkitVersion() {
        return this.craftBukkitVersion;
    }

    public double getHorMultiplier() {
        return this.horMultiplier;
    }

    public void setHorMultiplier(double horMultiplier) {
        this.horMultiplier = horMultiplier;
    }

    public double getVerMultiplier() {
        return this.verMultiplier;
    }

    public void setVerMultiplier(double verMultiplier) {
        this.verMultiplier = verMultiplier;
    }
}
