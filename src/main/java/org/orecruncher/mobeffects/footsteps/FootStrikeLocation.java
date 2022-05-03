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

package org.orecruncher.mobeffects.footsteps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.mobeffects.footsteps.facade.FacadeHelper;
import org.orecruncher.mobeffects.library.FootstepLibrary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public final class FootStrikeLocation {

	private final LivingEntity entity;
	private final Vec3 strike;
	private final BlockPos stepPos;

	public FootStrikeLocation(@Nonnull final LivingEntity entity, final double x, final double y, final double z) {
		this(entity, new Vec3(x, y, z));
	}

	public FootStrikeLocation(@Nonnull final LivingEntity entity, @Nonnull final Vec3 loc) {
		this.entity = entity;
		this.strike = loc;
		this.stepPos = new BlockPos(loc);
	}

	protected FootStrikeLocation(@Nonnull final LivingEntity entity, @Nonnull final Vec3 loc,
			@Nonnull final BlockPos pos) {
		this.entity = entity;
		this.strike = loc;
		this.stepPos = pos;
	}

	public FootStrikeLocation rebase(@Nonnull final BlockPos pos) {
		if (!this.stepPos.equals(pos)) {
			return new FootStrikeLocation(this.entity, this.strike, pos);
		}
		return this;
	}

	@Nonnull
	public LivingEntity getEntity() {
		return this.entity;
	}

	@Nonnull
	public BlockPos getStepPos() {
		return this.stepPos;
	}

	@Nonnull
	public Vec3 getStrikePosition() {
		return this.strike;
	}

	public Vec3 north() {
		return offset(Direction.NORTH, 1);
	}

	public Vec3 south() {
		return offset(Direction.SOUTH, 1);
	}

	public Vec3 east() {
		return offset(Direction.EAST, 1);
	}

	public Vec3 west() {
		return offset(Direction.WEST, 1);
	}

	public Vec3 up() {
		return offset(Direction.UP, 1);
	}

	public Vec3 down() {
		return offset(Direction.DOWN, 1);
	}

	/**
	 * Offsets this strike position n blocks in the given direction
	 */
	@Nonnull
	public Vec3 offset(@Nonnull final Direction facing, final float n) {
		return n == 0 ? this.strike
				: new Vec3(this.strike.x + facing.getStepX() * n, this.strike.y + facing.getStepY() * n,
						this.strike.z + facing.getStepZ() * n);
	}

	/**
	 * Determines the actual footprint location based on the BlockPos provided. The
	 * print is to ride on top of the bounding box. If the block does not have a
	 * print a null is returned.
	 *
	 * @return Vector containing footprint coordinates or null if no footprint is to
	 *         be generated
	 */
	@Nullable
	protected Vec3 footprintPosition() {
		final Level world = this.entity.getCommandSenderWorld();

		// The foot strike is just inside the block that was stepped on.
		BlockState state = world.getBlockState(this.stepPos);

		// If its an air block just return
		if (state.getMaterial() == Material.AIR)
			return null;

		// Check the block above - it could be a snow layer or carpet
		BlockPos feetPos = this.stepPos.above();
		BlockState upState = world.getBlockState(feetPos);
		if (upState.getMaterial() != Material.AIR) {
			state = upState;
		} else {
			feetPos = this.stepPos;
		}

		// if we get here the feetpos location is what has the block the player is stepping on, and state
		// should be the blockState.
		if (!hasFootstepImprint(state, feetPos))
			return null;

		final VoxelShape shape = state.getCollisionShape(world, feetPos);
		final double boundingY = state.getShape(world, feetPos).max(Direction.Axis.Y);
		final double collisionY = shape.isEmpty() ? 0 : shape.max(Direction.Axis.Y);
		final double maxYblock = feetPos.getY() + Math.max(boundingY, collisionY);
		// Should we get the max of strike.Y and maxYblock?
		return new Vec3(this.strike.x(), maxYblock, this.strike.z());
	}

	protected boolean hasFootstepImprint(@Nonnull final BlockState state, @Nonnull final BlockPos pos) {
		final BlockState footstepState = FacadeHelper.resolveState(this.entity, state, this.entity.getCommandSenderWorld(), Vec3.atCenterOf(pos), Direction.UP);
		return FootstepLibrary.hasFootprint(footstepState);
	}
}
