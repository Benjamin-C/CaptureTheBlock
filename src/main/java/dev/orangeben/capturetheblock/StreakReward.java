package dev.orangeben.capturetheblock;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class StreakReward {

    /** The name of the reward */
    private String name;
    /** The minimum streak length to get this reward */
    private int length = -1;
    /** The item to give if required */
    private Material item = null;
    /** The number of items to give */
    private int count = -1;
    /** The effect to give if required */
    private PotionEffectType effect = null;
    /** The strength of the effect */
    private int strength = -1;
    /** The duration of the effect */
    private int duration = -1;
    /** The message to send */
    private String message = null;

    public StreakReward(String name, ConfigurationSection cs) {
        this.name = name;
        this.length = cs.getInt(Keys.CONFIG_REWARD_LENGTH);
        try {
            if(cs.contains(Keys.CONFIG_REWARD_ITEM)) {
                item = Material.valueOf(cs.getString(Keys.CONFIG_REWARD_ITEM).toUpperCase());
                count = 1;
                if(cs.contains(Keys.CONFIG_REWARD_COUNT)) {
                    count = cs.getInt(Keys.CONFIG_REWARD_COUNT);
                }
            }
            if(cs.contains(Keys.CONFIG_REWARD_MESSAGE)) {
                message = cs.getString(Keys.CONFIG_REWARD_MESSAGE);
            }
            if(cs.contains(Keys.CONFIG_REWARD_EFFECT)) {
                effect = Registry.EFFECT.get(NamespacedKey.minecraft(cs.getString(Keys.CONFIG_REWARD_EFFECT)));
                strength = 1;
                duration = 30;
                if(cs.contains(Keys.CONFIG_REWARD_STRENGTH)) {
                    strength = cs.getInt(Keys.CONFIG_REWARD_STRENGTH);
                }
                if(cs.contains(Keys.CONFIG_REWARD_DURATION)) {
                    duration = cs.getInt(Keys.CONFIG_REWARD_DURATION);
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Could not load reward " + name + ", " + e.getMessage());
        }
    }

    public int getStreakLength() {
        return length;
    }

    public String getName() {
        return name;
    }

    public void giveTo(Player p) {
        if(item != null) {
            ItemStack is = new ItemStack(item, count);
            p.getInventory().addItem(is);
        }
        if(effect != null) {
            p.addPotionEffect(effect.createEffect(duration*20, count));
        }
        if(message != null) {
            p.sendMessage(message);
        }
    }

    @Override
    public String toString() {
        String str = "StreakReward[name:" + name + ",length:" + length;
        if(item != null) {
            str += ",item:" + count + "x" + item;
        }
        if(effect != null) {
            str += ",effect:" + strength + "x" + effect + "x" + duration + "s";
        }
        if(message != null) {
            str += ",msg:\"" + message + "\"";
        }
        str += "]";
        return str;
    }

}
