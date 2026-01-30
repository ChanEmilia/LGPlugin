package io.github.chanemilia.LGPlugin.Listeners;

import io.github.chanemilia.LGPlugin.LGPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class EffectListener implements Listener {

    private final LGPlugin plugin;

    public EffectListener(LattePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        PotionEffectType type = event.getModifiedType();

        List<String> disabledPotions = plugin.getConfig().getStringList("disabled-effects.effects");

        if (disabledPotions.contains(type.getName())) {
            event.setCancelled(true);
        }
    }
}