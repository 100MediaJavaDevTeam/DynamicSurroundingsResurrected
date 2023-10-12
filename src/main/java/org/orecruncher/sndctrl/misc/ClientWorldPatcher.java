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

package org.orecruncher.sndctrl.misc;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.config.Config;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = SoundControl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientWorldPatcher {
    private ClientWorldPatcher() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onWorldLoad(@Nonnull final LevelEvent.Load event) {
        if (Config.CLIENT.effects.fixupRandoms.get()) {
            final LevelAccessor world = event.getLevel();
            if (world.isClientSide() && world instanceof Level) {
                final Level w = (Level) world;
                w.random = new XoroshiroRandomSource(0);
            }
        }
    }
}
