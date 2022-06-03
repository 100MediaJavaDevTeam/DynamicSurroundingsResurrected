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
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
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
            final Biome forgeBiome = getClientBiome(biome);
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
    public static Color getColorForLiquid(@Nonnull final BlockGetter world, @Nonnull final BlockPos pos) {
        final FluidState fluidState = world.getFluidState(pos);

        if (fluidState.isEmpty())
            return NO_COLOR;

        // If the fluid is water, need to check the biome for coloration
        final Fluid fluid = fluidState.getType();
        if (fluid.is(FluidTags.WATER)) {
            final Biome biome = BiomeUtilities.getClientBiome(pos);
            if (biome != null)
                return new Color(biome.getWaterColor());
        }
        return new Color(fluid.getAttributes().getColor());
    }

    @Nonnull
    public static Set<BiomeDictionary.Type> getBiomeTypes(@Nonnull final Biome biome) {
        try {
            ResourceLocation loc = biome.getRegistryName();
            if (loc == null) {
                final Biome forgeBiome = getClientBiome(biome);
                if (forgeBiome != null)
                    loc = forgeBiome.getRegistryName();
            }
            if (loc != null) {
                ResourceKey<Biome> key = ResourceKey.create(Registry.BIOME_REGISTRY, loc);
                return BiomeDictionary.getTypes(key);
            }
        } catch (@Nonnull final Throwable t) {
            final String name = biome.toString(); //biome.getDisplayName().getFormattedText();
            Environs.LOGGER.warn("Unable to get biome type data for biome '%s'", name);
        }
        return ImmutableSet.of();
    }

    @Nullable
    public static Biome getClientBiome(@Nonnull final BlockPos pos) {
        final ClientLevel world = GameUtils.getWorld();
        if (world == null)
            return RegistryAccess.BUILTIN.get().registryOrThrow(Registry.BIOME_REGISTRY).get(Biomes.THE_VOID);
        final Biome biome = world.getBiome(pos).value();
        return getClientBiome(biome);
    }

    @Nullable
    public static Biome getClientBiome(@Nonnull final Biome biome) {
        final ClientLevel world = GameUtils.getWorld();
        if (world == null)
            return RegistryAccess.BUILTIN.get().registryOrThrow(Registry.BIOME_REGISTRY).get(Biomes.THE_VOID);
        ResourceLocation loc = world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome);
        if (loc == null)
            return RegistryAccess.BUILTIN.get().registryOrThrow(Registry.BIOME_REGISTRY).get(Biomes.THE_VOID);
        final Biome result = ForgeRegistries.BIOMES.getValue(loc);
        if (result == null)
            return RegistryAccess.BUILTIN.get().registryOrThrow(Registry.BIOME_REGISTRY).get(Biomes.THE_VOID);
        return result;
    }
}
