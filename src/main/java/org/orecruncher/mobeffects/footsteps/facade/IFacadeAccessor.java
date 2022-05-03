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

package org.orecruncher.mobeffects.footsteps.facade;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
interface IFacadeAccessor {

	/*
	 * Name of the facade accessor for logging and identification purposes.
	 */
	@Nonnull
	String getName();

	/*
	 * Determines if the block can be handled by the accessor
	 */
	boolean instanceOf(@Nonnull final Block block);

	/*
	 * Indicates if the accessor is valid. It could be invalid if the associated mod
	 * is not installed.
	 */
	boolean isValid();

	/*
	 * Requests the underlying IBlockState for the block. The underlying IBlockState
	 * is what should be used when generating sound effects.
	 */
	@Nullable
	BlockState getBlockState(@Nonnull final LivingEntity entity, @Nonnull final BlockState state,
							 @Nonnull final BlockGetter world, @Nonnull final Vec3 pos, @Nullable final Direction side);
}
