package io.github.chanemilia.LGPlugin.Listeners;

import io.github.chanemilia.LGPlugin.LGPlugin;
import io.github.chanemilia.LGPlugin.Utils.ItemMatcher;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.List;
import java.util.Map;

public class CraftListener implements Listener {

    private final LGPlugin plugin;

    public CraftListener(LGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null) return;

        ItemStack result = event.getInventory().getResult();

        if (result == null) {
            result = event.getRecipe().getResult();
        }

        if (isRestricted(result)) {
            event.getInventory().setResult(null);
        }
    }

    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        if (isRestricted(event.getResult())) {
            event.setResult(null);
        }
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (isRestricted(event.getResult())) {
            event.setResult(null);
        }
    }

    @EventHandler
    public void onSmelt(FurnaceSmeltEvent event) {
        if (isRestricted(event.getResult())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBrew(BrewEvent event) {
        if (!plugin.getConfig().getBoolean("restricted-crafting.enabled", false)) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            BrewerInventory inv = event.getContents();
            for (int i = 0; i < 3; i++) {
                ItemStack item = inv.getItem(i);
                if (item == null || item.getType() == Material.AIR) continue;

                if (isRestricted(item)) {
                    ItemStack mundane = new ItemStack(item.getType());
                    PotionMeta meta = (PotionMeta) mundane.getItemMeta();
                    if (meta != null) {
                        meta.setBasePotionType(PotionType.MUNDANE);
                        mundane.setItemMeta(meta);
                    }
                    inv.setItem(i, mundane);
                }
            }
        });
    }

    @EventHandler
    public void onStonecutter(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.STONECUTTER) return;
        if (event.getSlotType() != InventoryType.SlotType.RESULT) return;

        ItemStack current = event.getCurrentItem();
        if (isRestricted(current)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCrafterCraft(CrafterCraftEvent event) {
        if (isRestricted(event.getResult())) {
            event.setCancelled(true);
        }
    }

    private boolean isRestricted(ItemStack result) {
        if (!plugin.getConfig().getBoolean("restricted-crafting.enabled", false)) return false;
        if (result == null || result.getType() == Material.AIR) return false;

        List<Map<?, ?>> restrictions = plugin.getConfig().getMapList("restricted-crafting.items");

        for (Map<?, ?> rule : restrictions) {
            String configName = (String) rule.get("material");
            if (configName == null) continue;

            if (ItemMatcher.matchesMaterial(result.getType(), configName)) {
                if (rule.containsKey("nbt")) {
                    Map<?, ?> nbtRules = (Map<?, ?>) rule.get("nbt");
                    if (!ItemMatcher.checkNbt(result, nbtRules)) {
                        continue;
                    }
                }
                return true;
            }
        }
        return false;
    }
}