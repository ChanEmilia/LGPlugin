package io.github.chanemilia.LGPlugin.Listeners;

import io.github.chanemilia.LGPlugin.LGPlugin;
import io.github.chanemilia.LGPlugin.Utils.ItemFactory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RespawnListener implements Listener {

    private final LGPlugin plugin;
    private final Map<UUID, Long> lastKitTime = new HashMap<>();

    public RespawnListener(LGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean("respawn-kit.first-login", false)) return;

        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            giveKit(player);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!plugin.getConfig().getBoolean("respawn-kit.death", false)) return;

        if (event.getRespawnReason() != PlayerRespawnEvent.RespawnReason.DEATH) {
            return;
        }

        giveKit(event.getPlayer());
    }

    private void giveKit(Player player) {
        long cooldownTicks = plugin.getConfig().getLong("respawn-kit.cooldown", 0);
        if (cooldownTicks > 0) {
            long last = lastKitTime.getOrDefault(player.getUniqueId(), 0L);
            long now = System.currentTimeMillis();
            if (now - last < cooldownSec * 1000L) {
                return;
            }
            lastKitTime.put(player.getUniqueId(), now);
        }

        List<Map<?, ?>> items = plugin.getConfig().getMapList("respawn-kit.items");
        for (Map<?, ?> itemConfig : items) {
            String slotStr = (String) itemConfig.get("slot");
            String materialName = (String) itemConfig.get("item");
            Object countObj = itemConfig.get("count");
            int count = (countObj instanceof Number) ? ((Number) countObj).intValue() : 1;
            Map<?, ?> nbt = (Map<?, ?>) itemConfig.get("nbt");

            ItemStack item = ItemFactory.createItem(materialName, count, nbt);
            int slot = ItemFactory.parseSlot(slotStr);

            if (slot >= 0) {
                ItemStack existing = player.getInventory().getItem(slot);
                if (existing == null || existing.getType() == Material.AIR) {
                    player.getInventory().setItem(slot, item);
                } else {
                    player.getInventory().addItem(item);
                }
            } else {
                player.getInventory().addItem(item);
            }
        }
    }
}