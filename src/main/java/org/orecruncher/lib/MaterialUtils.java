/*
 * Dynamic Surroundings
 * Copyright (C) 2020  OreCruncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.lib;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.world.level.material.Material;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@SuppressWarnings("unused")
public final class MaterialUtils {

    private static final Reference2ObjectOpenHashMap<Material, String> materialMap = new Reference2ObjectOpenHashMap<>();
    private static final Map<String, Material> materialMapInv = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    static {
        materialMap.defaultReturnValue("CUSTOM");

        materialMap.put(Material.AIR, "AIR");
        materialMap.put(Material.STRUCTURAL_AIR, "STRUCTURE_VOID");
        materialMap.put(Material.PORTAL, "PORTAL");
        materialMap.put(Material.CLOTH_DECORATION, "CARPET");
        materialMap.put(Material.PLANT, "PLANTS");
        materialMap.put(Material.WATER_PLANT, "OCEAN_PLANT");
        materialMap.put(Material.REPLACEABLE_PLANT, "TALL_PLANTS");
        materialMap.put(Material.REPLACEABLE_FIREPROOF_PLANT, "NETHER_PLANTS");
        materialMap.put(Material.REPLACEABLE_WATER_PLANT, "SEA_GRASS");
        materialMap.put(Material.WATER, "WATER");
        materialMap.put(Material.BUBBLE_COLUMN, "BUBBLE_COLUMN");
        materialMap.put(Material.LAVA, "LAVA");
        materialMap.put(Material.TOP_SNOW, "SNOW");
        materialMap.put(Material.FIRE, "FIRE");
        materialMap.put(Material.DECORATION, "MISCELLANEOUS");
        materialMap.put(Material.WEB, "WEB");
        materialMap.put(Material.BUILDABLE_GLASS, "REDSTONE_LIGHT");
        materialMap.put(Material.CLAY, "CLAY");
        materialMap.put(Material.DIRT, "EARTH");
        materialMap.put(Material.GRASS, "ORGANIC");
        materialMap.put(Material.ICE_SOLID, "PACKED_ICE");
        materialMap.put(Material.SAND, "SAND");
        materialMap.put(Material.SPONGE, "SPONGE");
        materialMap.put(Material.SHULKER_SHELL, "SHULKER");
        materialMap.put(Material.WOOD, "WOOD");
        materialMap.put(Material.NETHER_WOOD, "NETHER_WOOD");
        materialMap.put(Material.BAMBOO_SAPLING, "BAMBOO_SAPLING");
        materialMap.put(Material.BAMBOO, "BAMBOO");
        materialMap.put(Material.WOOL, "WOOL");
        materialMap.put(Material.EXPLOSIVE, "TNT");
        materialMap.put(Material.LEAVES, "LEAVES");
        materialMap.put(Material.GLASS, "GLASS");
        materialMap.put(Material.ICE, "ICE");
        materialMap.put(Material.CACTUS, "CACTUS");
        materialMap.put(Material.STONE, "ROCK");
        materialMap.put(Material.METAL, "IRON");
        materialMap.put(Material.SNOW, "SNOW_BLOCK");
        materialMap.put(Material.HEAVY_METAL, "ANVIL");
        materialMap.put(Material.BARRIER, "BARRIER");
        materialMap.put(Material.PISTON, "PISTON");
//        materialMap.put(Material.CORAL, "CORAL");
        materialMap.put(Material.VEGETABLE, "GOURD");
        materialMap.put(Material.EGG, "DRAGON_EGG");
        materialMap.put(Material.CAKE, "CAKE");

        // Create the inverse map
        for (final Map.Entry<Material, String> kvp : materialMap.entrySet()) {
            materialMapInv.put(kvp.getValue(), kvp.getKey());
        }
    }

    private MaterialUtils() {

    }

    @Nonnull
    public static Set<Material> getMaterials() {
        return materialMap.keySet();
    }

    @Nullable
    public static Material getMaterial(@Nonnull final String name) {
        return materialMapInv.get(name);
    }

    @Nullable
    public static String getMaterialName(@Nonnull final Material mat) {
        return materialMap.get(mat);
    }
}
