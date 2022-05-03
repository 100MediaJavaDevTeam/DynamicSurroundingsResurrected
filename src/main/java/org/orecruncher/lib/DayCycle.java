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

import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.sndctrl.SoundControl;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public enum DayCycle {

    NO_SKY(false, "NoSky"),
    SUNRISE(false, "Sunrise"),
    SUNSET(true, "Sunset"),
    DAYTIME(false, "Daytime"),
    NIGHTTIME(true, "Nighttime");

    private static final float DAYTIME_THRESHOLD = 0.8F;
    private static final float SUNRISE_THRESHOLD = DAYTIME_THRESHOLD - 0.04F;
    private static final float NIGHTTIME_THRESHOLD = 0.26F;
    private static final float SUNSET_THRESHOLD = NIGHTTIME_THRESHOLD - 0.04F;

    private final boolean auroraVisible;
    private final String localizeString;

    DayCycle(final boolean auroraVisible, @Nonnull final String localName) {
        this.auroraVisible = auroraVisible;
        this.localizeString = SoundControl.MOD_ID + ".format." + localName;
    }

    public static boolean isDaytime(@Nonnull final LevelAccessor world) {
        return getCycle(world) == DayCycle.DAYTIME;
    }

    public static boolean isNighttime(@Nonnull final LevelAccessor world) {
        return getCycle(world) == DayCycle.NIGHTTIME;
    }

    public static boolean isSunrise(@Nonnull final LevelAccessor world) {
        return getCycle(world) == DayCycle.SUNRISE;
    }

    public static boolean isSunset(@Nonnull final LevelAccessor world) {
        return getCycle(world) == DayCycle.SUNSET;
    }

    public static DayCycle getCycle(@Nonnull final LevelAccessor world) {
        if (world.dimensionType().hasCeiling() || !world.dimensionType().hasSkyLight())
            return DayCycle.NO_SKY;

        final float angle = world.getTimeOfDay(0);

        if (angle > DAYTIME_THRESHOLD)
            return DayCycle.DAYTIME;
        if (angle > SUNRISE_THRESHOLD)
            return DayCycle.SUNRISE;
        if (angle > NIGHTTIME_THRESHOLD)
            return DayCycle.NIGHTTIME;
        if (angle > SUNSET_THRESHOLD)
            return DayCycle.SUNSET;
        return DayCycle.DAYTIME;
    }

    public static float getMoonPhaseFactor(@Nonnull final LevelAccessor world) {
        return world.getMoonBrightness();
    }

    public static boolean isAuroraVisible(@Nonnull final LevelAccessor world) {
        return getCycle(world).isAuroraVisible();
    }

    public static boolean isAuroraInvisible(@Nonnull final LevelAccessor world) {
        return !getCycle(world).isAuroraVisible();
    }

    public boolean isAuroraVisible() {
        return this.auroraVisible;
    }

    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public String getFormattedName() {
        return Localization.load(this.localizeString);
    }

}
