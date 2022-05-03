/*
 *  Dynamic Surroundings
 *  Copyright (C) 2020  OreCruncher
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.environs.effects;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.environs.effects.emitters.Jet;
import org.orecruncher.environs.effects.emitters.SteamJet;
import org.orecruncher.lib.WorldUtils;

import javax.annotation.Nonnull;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class SteamJetEffect extends JetEffect {

    public SteamJetEffect(final int chance) {
        super(chance);
    }

    public static boolean isValidSpawnBlock(@Nonnull final BlockGetter provider,
                                            @Nonnull final BlockPos pos, @Nonnull final BlockState source) {
        if (!WorldUtils.isAirBlock(provider, pos.above()))
            return false;
        if (provider.getBlockState(pos) != source)
            return false;
        return countCubeBlocks(provider, pos, HOTBLOCK_PREDICATE, true) > 0;
    }

    @Override
    @Nonnull
    public BlockEffectType getEffectType() {
        return BlockEffectType.STEAM;
    }

    @Override
    public boolean canTrigger(@Nonnull final BlockGetter provider, @Nonnull final BlockState state,
                              @Nonnull final BlockPos pos, @Nonnull final Random random) {
        return isValidSpawnBlock(provider, pos, state) && super.canTrigger(provider, state, pos, random);
    }

    @Override
    public void doEffect(@Nonnull final BlockGetter provider, @Nonnull final BlockState state,
                         @Nonnull final BlockPos pos, @Nonnull final Random random) {
        final int strength = countCubeBlocks(provider, pos, HOTBLOCK_PREDICATE, false);
        if (strength > 0) {
            final FluidState fluidState = state.getFluidState();
            final float spawnHeight;
            if (fluidState.isEmpty()) {
                spawnHeight = pos.getY() + 0.9F;
            } else {
                spawnHeight = pos.getY() + fluidState.getOwnHeight() + 0.1F;
            }
            final Jet effect = new SteamJet(strength, provider, pos.getX() + 0.5D, spawnHeight, pos.getZ() + 0.5D);
            addEffect(effect);
        }
    }
}
