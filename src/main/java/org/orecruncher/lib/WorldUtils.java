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

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.compat.ModEnvironment;
import org.orecruncher.lib.reflection.BooleanField;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public final class WorldUtils {

    private static final BooleanField<ClientLevel.ClientLevelData> flatWorld = new BooleanField<>(ClientLevel.ClientLevelData.class, false, "flatWorld", "isFlat");

    /**
     * Temperatures LESS than this value are considered cold temperatures.
     */
    public static final float COLD_THRESHOLD = 0.2F;

    /**
     * Temperatures LESS than this value are considered cold enough for snow.
     */
    public static final float SNOW_THRESHOLD = 0.15F;

    /**
     * SereneSeasons support to obtain the temperature at a specific block location.
     */
    private interface ITemperatureHandler {
        float getTemp(@Nonnull final Level world, @Nonnull final BlockPos pos);
    }

    /**
     * Weather support to obtain current rain/thunder strength client side
     */
    private interface IWeatherStrength {
        float getStrength(@Nonnull final Level world, final float partialTicks);
    }

    /**
     * Weather support to determine if an aspect of weather is occuring.
     */
    private interface IWeatherAspect {
        boolean isOccuring(@Nonnull final Level world);
    }

    private static final ITemperatureHandler TEMP;
    private static final IWeatherStrength RAIN_STRENGTH;
    private static final IWeatherStrength THUNDER_STRENGTH;
    private static final IWeatherAspect RAIN_OCCURING;
    private static final IWeatherAspect THUNDER_OCCURING;

    static {
        ITemperatureHandler TEMP1;
        if (ModEnvironment.SereneSeasons.isLoaded()) {
            try {
                final Class<?> clazz = Class.forName("sereneseasons.season.SeasonHooks");
                final Method method = clazz.getMethod("getBiomeTemperature", Level.class, Biome.class, BlockPos.class);
                TEMP1 = (world, pos) -> {
                    try {
                        return (float)method.invoke(null, world, world.getBiome(pos), pos);
                    } catch(@Nonnull final Throwable t) {
                        return world.getBiome(pos).getTemperature(pos);
                    }
                };
                Lib.LOGGER.info("Hooked SereneSeasons getBiomeTemperature()");
            } catch(@Nonnull final Throwable t) {
                Lib.LOGGER.warn("Unable to hook SereneSeasons getBiomeTemperature()!");
                TEMP1 = (world, pos) -> world.getBiome(pos).getTemperature(pos);
            }
        } else {
            TEMP1 = (world, pos) -> world.getBiome(pos).getTemperature(pos);
        }

        TEMP = TEMP1;

        // Place holder for future
        RAIN_STRENGTH = Level::getRainLevel;
        RAIN_OCCURING = Level::isRaining;
        THUNDER_STRENGTH = Level::getThunderLevel;
        THUNDER_OCCURING = Level::isThundering;
    }

    private WorldUtils() {

    }

    @Nonnull
    public static BlockPos getTopSolidOrLiquidBlock(@Nonnull final LevelReader world, @Nonnull final BlockPos pos) {
        return new BlockPos(pos.getX(), world.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()), pos.getZ());
    }

    /**
     * Gets the temperature value for the given BlockPos in the world.
     */
    public static float getTemperatureAt(@Nonnull final Level world, @Nonnull final BlockPos pos) {
        return TEMP.getTemp(world, pos);
    }

    /**
     * Determines if the temperature value is considered a cold temperature.
     */
    public static boolean isColdTemperature(final float temp) {
        return temp < COLD_THRESHOLD;
    }

    /**
     * Determines if the temperature value is considered cold enough for snow.
     */
    public static boolean isSnowTemperature(final float temp) {
        return temp < SNOW_THRESHOLD;
    }

    /**
     * Determines if the side of the block at the specified position is considered solid.
     */
    public static boolean isSolid(@Nonnull final BlockGetter world, @Nonnull final BlockPos pos, @Nonnull final Direction dir) {
        final BlockState state = world.getBlockState(pos);
        return Block.isFaceFull(state.getCollisionShape(world, pos, CollisionContext.empty()),dir);
    }

    /**
     * Determines if the top side of the block at the specified position is considered solid.
     */
    public static boolean isTopSolid(@Nonnull final BlockGetter world, @Nonnull final BlockPos pos) {
        return isSolid(world, pos, Direction.UP);
    }

    /**
     * Determines if the block at the specified location is solid.
     */
    public static boolean isBlockSolid(@Nonnull final BlockGetter world, @Nonnull final BlockPos pos) {
        final BlockState state = world.getBlockState(pos);
        return state.canOcclude();
    }

    /**
     * Determines if the block at the specified location is an air block.
     */
    public static boolean isAirBlock(@Nonnull final BlockGetter world, @Nonnull final BlockPos pos) {
        return isAirBlock(world.getBlockState(pos));
    }

    /**
     * Determines if the BlockState reference is an air block.
     */
    public static boolean isAirBlock(@Nonnull final BlockState state) {
        return state.getMaterial() == Material.AIR;
    }

    /**
     * Gets the precipitation currently falling at the specified location.  It takes into account temperature and the
     * like.
     */
    public static Biome.Precipitation getCurrentPrecipitationAt(@Nonnull final LevelReader world, @Nonnull final BlockPos pos) {
        if (!(world instanceof Level) || !isRaining((Level) world)) {
            // Not currently raining
            return Biome.Precipitation.NONE;
        }

        final Biome biome = world.getBiome(pos);

        // If the biome has no rain...
        if (biome.getPrecipitation() == Biome.Precipitation.NONE)
            return Biome.Precipitation.NONE;

        // Is there a block above that is blocking the rainfall?
        final BlockPos p = getPrecipitationHeight(world, pos);
        if (p.getY() > pos.getY()) {
            return Biome.Precipitation.NONE;
        }

        // Use the temperature of the biome to get whether it is raining or snowing
        final float temp = getTemperatureAt((Level) world, pos);
        return isSnowTemperature(temp) ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN;
    }

    public static float getRainStrength(@Nonnull final Level world, final float partialTicks) {
        return RAIN_STRENGTH.getStrength(world, partialTicks);
    }

    public static float getThunderStrength(@Nonnull final Level world, final float partialTicks) {
        return THUNDER_STRENGTH.getStrength(world, partialTicks);
    }

    public static boolean isRaining(@Nonnull final Level world) {
        return RAIN_OCCURING.isOccuring(world);
    }

    public static boolean isThundering(@Nonnull final Level world) {
        return THUNDER_OCCURING.isOccuring(world);
    }

    @Nonnull
    public static BlockPos getPrecipitationHeight(@Nonnull final LevelReader world, @Nonnull final BlockPos pos) {
        return world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos);
    }

    public static boolean hasVoidParticles(@Nonnull final Level world) {
        return world.dimensionType().hasSkyLight();
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isSuperFlat(@Nonnull final Level world) {
        final LevelData info = world.getLevelData();
        return info instanceof ClientLevel.ClientLevelData && flatWorld.get((ClientLevel.ClientLevelData) info);
    }

}
