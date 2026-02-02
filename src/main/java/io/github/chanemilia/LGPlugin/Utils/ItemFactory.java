package io.github.chanemilia.LGPlugin.Utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.regex.Matcher;

public class ItemFactory {

    public static ItemStack createItem(String materialName, int count, Map<?, ?> nbt) {
        Material mat = ItemMatcher.resolveMaterial(materialName);
        if (mat == null) return new ItemStack(Material.AIR);

        ItemStack item = new ItemStack(mat, count);
        if (nbt != null && !nbt.isEmpty()) {
            applyNbt(item, nbt);
        }
        return item;
    }

    @SuppressWarnings("deprecation")
    public static void applyNbt(ItemStack item, Map<?, ?> nbt) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        for (Map.Entry<?, ?> entry : nbt.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();

            if (key.equalsIgnoreCase("enchantments")) {
                if (value instanceof Map) {
                    applyEnchantsMap(meta, (Map<?, ?>) value);
                } else {
                    applyEnchantsString(meta, value.toString());
                }
                continue;
            }

            if (key.equalsIgnoreCase("name") || key.equalsIgnoreCase("custom_name")) {
                meta.setDisplayName(value.toString());
                continue;
            }

            if (key.equalsIgnoreCase("custom_model_data") || key.equalsIgnoreCase("custommodeldata")) {
                if (value instanceof Number) {
                    meta.setCustomModelData(((Number) value).intValue());
                }
            }
        }
        item.setItemMeta(meta);
    }

    private static void applyEnchantsMap(ItemMeta meta, Map<?, ?> enchants) {
        for (Map.Entry<?, ?> entry : enchants.entrySet()) {
            String key = entry.getKey().toString();
            Object val = entry.getValue();
            int level = (val instanceof Number) ? ((Number) val).intValue() : 1;

            Enchantment ench = ItemMatcher.resolveEnchantment(key);
            if (ench != null) {
                meta.addEnchant(ench, level, true);
            }
        }
    }

    private static void applyEnchantsString(ItemMeta meta, String enchantString) {
        String[] parts = enchantString.split(",");
        for (String part : parts) {
            Matcher matcher = ItemMatcher.ENCHANT_PATTERN.matcher(part.trim());
            if (matcher.find()) {
                String keyName = matcher.group(1);
                int level = Integer.parseInt(matcher.group(2));

                Enchantment ench = ItemMatcher.resolveEnchantment(keyName);
                if (ench != null) {
                    meta.addEnchant(ench, level, true);
                }
            }
        }
    }

    public static int parseSlot(String slot) {
        if (slot == null) return -1;
        slot = slot.toLowerCase();

        switch (slot) {
            case "armor.feet", "feet", "boots" -> { return 36; }
            case "armor.legs", "legs", "leggings" -> { return 37; }
            case "armor.chest", "chest", "chestplate" -> { return 38; }
            case "armor.head", "head", "helmet" -> { return 39; }
            case "weapon.offhand", "offhand" -> { return 40; }
        }

        try {
            if (slot.startsWith("hotbar.")) return Integer.parseInt(slot.substring(7));
            if (slot.startsWith("inventory.")) return Integer.parseInt(slot.substring(10)) + 9;
            return Integer.parseInt(slot);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }
}