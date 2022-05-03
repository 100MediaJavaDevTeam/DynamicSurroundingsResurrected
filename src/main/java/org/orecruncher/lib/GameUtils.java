/*
 * Dynamic Surroundings: Sound Control
 * Copyright (C) 2019  OreCruncher
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

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class GameUtils {
    private GameUtils() {

    }

    // Client methods
    @OnlyIn(Dist.CLIENT)
    @Nullable
    public static Player getPlayer() {
        return getMC().player;
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    public static ClientLevel getWorld() {
        return getMC().level;
    }

    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public static Minecraft getMC() {
        return Minecraft.getInstance();
    }

    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public static Options getGameSettings() {
        return getMC().options;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean displayDebug() {
        return getGameSettings().renderDebug;
    }

    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public static SoundManager getSoundHander() {
        return getMC().getSoundManager();
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isInGame() {
        return getWorld() != null && getPlayer() != null;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isThirdPersonView() {
        return getGameSettings().getCameraType() != CameraType.FIRST_PERSON;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isFirstPersonView() {
        return getGameSettings().getCameraType() == CameraType.FIRST_PERSON;
    }
}
