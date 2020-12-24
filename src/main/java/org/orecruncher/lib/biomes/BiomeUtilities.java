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

package org.orecruncher.lib.biomes;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.registries.ForgeRegistries;
import org.orecruncher.environs.Environs;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.Localization;
import org.orecruncher.lib.gui.Color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class BiomeUtilities {
    private BiomeUtilities() {

    }

    private static final Color NO_COLOR = new Color(1F, 1F, 1F);

    @Nonnull
    public static String getBiomeName(@Nonnull final Biome biome) {
        ResourceLocation loc = biome.getRegistryName();
        if (loc == null) {
            final Biome forgeBiome = getBiome(biome);
            if (forgeBiome != null)
                loc = forgeBiome.getRegistryName();
        }
        if (loc == null)
            return "UNKNOWN";
        final String fmt = String.format("biome.%s.%s", loc.getNamespace(), loc.getPath());
        return Localization.load(fmt);
    }

    // ===================================
    //
    // Miscellaneous Support Functions
    //
    // ===================================
    @Nonnull
    public static Collection<BiomeDictionary.Type> getBiomeTypes() {
        return BiomeDictionary.Type.getAll();
    }

    @Nonnull
    public static Color getColorForLiquid(@Nonnull final IBlockReader world, @Nonnull final BlockPos pos) {
        final FluidState fluidState = world.getFluidState(pos);

        if (fluidState.isEmpty())
            return NO_COLOR;

        final Fluid fluid = fluidState.getFluid();
        return new Color(fluid.getAttributes().getColor());
    }

    @Nonnull
    public static Set<BiomeDictionary.Type> getBiomeTypes(@Nonnull final Biome biome) {
        try {
            ResourceLocation loc = biome.getRegistryName();
            if (loc == null) {
                final Biome forgeBiome = getBiome(biome);
                if (forgeBiome != null)
                    loc = forgeBiome.getRegistryName();
            }
            if (loc != null) {
                RegistryKey<Biome> key = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, loc);
                return BiomeDictionary.getTypes(key);
            }
        } catch (@Nonnull final Throwable t) {
            final String name = biome.toString(); //biome.getDisplayName().getFormattedText();
            Environs.LOGGER.warn("Unable to get biome type data for biome '%s'", name);
        }
        return ImmutableSet.of();
    }

    public static Biome getBiome(@Nonnull BlockPos pos) {
        final ClientWorld world = GameUtils.getWorld();
        assert world != null;
        final ResourceLocation loc = world.func_241828_r().getRegistry(Registry.BIOME_KEY).getKey(world.getBiome(pos));
        return ForgeRegistries.BIOMES.getValue(loc);
    }

    @Nullable
    public static Biome getBiome(@Nonnull final Biome biome) {
        final ClientWorld world = GameUtils.getWorld();
        assert world != null;
        final ResourceLocation loc = world.func_241828_r().getRegistry(Registry.BIOME_KEY).getKey(biome);
        return ForgeRegistries.BIOMES.getValue(loc);
    }
}
