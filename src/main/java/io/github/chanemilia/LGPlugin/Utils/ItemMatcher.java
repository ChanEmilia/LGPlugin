package io.github.chanemilia.LGPlugin.Utils;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemMatcher {

    public static final Pattern ENCHANT_PATTERN = Pattern.compile("[\"']?([a-z0-9_:]+)[\"']?:(\\d+)");

    @SuppressWarnings("deprecation")
    public static boolean checkNbt(ItemStack item, Map<?, ?> nbt) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();

        for (Map.Entry<?, ?> entry : nbt.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();

            if (key.equalsIgnoreCase("enchantments")) {
                if (value instanceof Map) {
                    if (!checkEnchantsMap(meta, (Map<?, ?>) value)) return false;
                } else {
                    if (!checkEnchantsString(meta, value.toString())) return false;
                }
                continue;
            }

            if (key.equalsIgnoreCase("name") || key.equalsIgnoreCase("custom_name")) {
                if (!meta.hasDisplayName()) return false;
                if (!meta.getDisplayName().equals(value.toString())) return false;
                continue;
            }

            if (key.equalsIgnoreCase("custom_model_data") || key.equalsIgnoreCase("custommodeldata")) {
                if (!meta.hasCustomModelData()) return false;
                int required = (value instanceof Number) ? ((Number) value).intValue() : 0;
                if (meta.getCustomModelData() != required) return false;
                continue;
            }

            String fullComponentString = meta.getAsString();
            String searchKey = key.toLowerCase();
            if (!searchKey.contains(":")) searchKey = "minecraft:" + searchKey;

            String expectedValue = value.toString();
            String exactSnippet = searchKey + "=" + expectedValue;
            String quotedSnippet = searchKey + "=\"" + expectedValue + "\"";
            String singleQuotedSnippet = searchKey + "='" + expectedValue + "'";

            if (!fullComponentString.contains(exactSnippet) &&
                    !fullComponentString.contains(quotedSnippet) &&
                    !fullComponentString.contains(singleQuotedSnippet)) {

                if (!fullComponentString.contains(expectedValue)) return false;
            }
        }
        return true;
    }

    private static boolean checkEnchantsMap(ItemMeta meta, Map<?, ?> enchants) {
        for (Map.Entry<?, ?> entry : enchants.entrySet()) {
            String key = entry.getKey().toString();
            Object val = entry.getValue();
            int level = (val instanceof Number) ? ((Number) val).intValue() : 1;

            Enchantment ench = resolveEnchantment(key);
            if (ench == null) return false;
            if (!meta.hasEnchant(ench)) return false;
            if (meta.getEnchantLevel(ench) < level) return false;
        }
        return true;
    }

    private static boolean checkEnchantsString(ItemMeta meta, String enchantString) {
        String[] parts = enchantString.split(",");
        for (String part : parts) {
            Matcher matcher = ENCHANT_PATTERN.matcher(part.trim());
            if (matcher.find()) {
                String keyName = matcher.group(1);
                int level = Integer.parseInt(matcher.group(2));

                Enchantment ench = resolveEnchantment(keyName);
                if (ench == null) return false;
                if (!meta.hasEnchant(ench)) return false;
                if (meta.getEnchantLevel(ench) < level) return false;
            }
        }
        return true;
    }

    // Public Resolution Helpers
    public static Material resolveMaterial(String name) {
        if (name == null) return null;
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException ignored) {}

        Material match = Material.matchMaterial(name);
        if (match != null) return match;

        if (name.contains(":")) {
            return Material.matchMaterial(name.split(":")[1]);
        } else {
            return Material.matchMaterial("minecraft:" + name);
        }
    }

    @SuppressWarnings("deprecation")
    public static Enchantment resolveEnchantment(String key) {
        NamespacedKey nsKey = NamespacedKey.fromString(key.toLowerCase());
        if (nsKey == null && !key.contains(":")) {
            nsKey = NamespacedKey.minecraft(key.toLowerCase());
        }

        if (nsKey != null) {
            Enchantment ench = Registry.ENCHANTMENT.get(nsKey);
            if (ench != null) return ench;
        }

        try {
            return Enchantment.getByName(key.toUpperCase());
        } catch (Exception ignored) {}

        return null;
    }
}