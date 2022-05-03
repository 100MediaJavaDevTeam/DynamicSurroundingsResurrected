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

package org.orecruncher.lib.math;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Block ray trace and context rolled into one!  With some optimizations specific to blocks.  These routines are
 * based on what the Minecraft raytrace algorithms do.  Pretty standard voxel based ray trace.
 */
public class BlockRayTrace {

    private static final double NUDGE = -1.0E-7D;

    final BlockGetter world;
    final ClipContext.Block blockMode;
    final ClipContext.Fluid fluidMode;
    final CollisionContext selectionCtx;

    // Can be changed dynamically to avoid recreating contexts
    Vec3 start;
    Vec3 end;

    public BlockRayTrace(@Nonnull final BlockGetter world, @Nonnull final ClipContext.Block bm, @Nonnull final ClipContext.Fluid fm) {
        this(world, Vec3.ZERO, Vec3.ZERO, bm, fm);
    }

    public BlockRayTrace(@Nonnull final BlockGetter world, @Nonnull final Vec3 start, @Nonnull final Vec3 end, @Nonnull final ClipContext.Block bm, @Nonnull final ClipContext.Fluid fm) {
        this.world = world;
        this.start = start;
        this.end = end;
        this.blockMode = bm;
        this.fluidMode = fm;
        this.selectionCtx = CollisionContext.empty();
    }

    @Nonnull
    public BlockHitResult trace() {
        return traceLoop();
    }

    @Nonnull
    public BlockHitResult trace(@Nonnull final Vec3 start, @Nonnull final Vec3 end) {
        this.start = start;
        this.end = end;
        return traceLoop();
    }

    @Nonnull
    private BlockHitResult traceLoop() {
        if (this.start.equals(this.end)) {
            return miss();
        } else {

            final double lerpX = Mth.lerp(NUDGE, this.start.x, this.end.x);
            final double lerpY = Mth.lerp(NUDGE, this.start.y, this.end.y);
            final double lerpZ = Mth.lerp(NUDGE, this.start.z, this.end.z);

            int posX = Mth.floor(lerpX);
            int posY = Mth.floor(lerpY);
            int posZ = Mth.floor(lerpZ);

            // Do a quick check on the first block.  If there is a hit return
            // that result.  Else, traverse the line segment between start and end
            // points until a hit.
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(posX, posY, posZ);
            BlockHitResult traceResult = hitCheck(mutablePos);
            if (traceResult == null) {
                // No hit.  Do the calcs to traverse the line
                final double xLerp = Mth.lerp(NUDGE, this.end.x, this.start.x);
                final double yLerp = Mth.lerp(NUDGE, this.end.y, this.start.y);
                final double zLerp = Mth.lerp(NUDGE, this.end.z, this.start.z);
                final double lenX = xLerp - lerpX;
                final double lenY = yLerp - lerpY;
                final double lenZ = zLerp - lerpZ;
                final int dirX = Mth.sign(lenX);
                final int dirY = Mth.sign(lenY);
                final int dirZ = Mth.sign(lenZ);
                final double deltaX = dirX == 0 ? Double.MAX_VALUE : (dirX / lenX);
                final double deltaY = dirY == 0 ? Double.MAX_VALUE : (dirY / lenY);
                final double deltaZ = dirZ == 0 ? Double.MAX_VALUE : (dirZ / lenZ);

                double X = deltaX * (dirX > 0 ? 1.0D - Mth.frac(lerpX) : Mth.frac(lerpX));
                double Y = deltaY * (dirY > 0 ? 1.0D - Mth.frac(lerpY) : Mth.frac(lerpY));
                double Z = deltaZ * (dirZ > 0 ? 1.0D - Mth.frac(lerpZ) : Mth.frac(lerpZ));

                // Main processing loop that traverses the line segment between start and end point.  This process
                // will continue until there is a miss or a block is hit.
                do {
                    // Reached the end of the line?
                    if (X > 1.0D && Y > 1.0D && Z > 1.0D) {
                        return miss();
                    }

                    // Delta the axis that needs to be advanced.
                    if (X < Y) {
                        if (X < Z) {
                            posX += dirX;
                            X += deltaX;
                        } else {
                            posZ += dirZ;
                            Z += deltaZ;
                        }
                    } else if (Y < Z) {
                        posY += dirY;
                        Y += deltaY;
                    } else {
                        posZ += dirZ;
                        Z += deltaZ;
                    }

                    // Check for a hit.  If null is returned loop back around.
                    traceResult = hitCheck(mutablePos.set(posX, posY, posZ));
                } while (traceResult == null);

            }
            // Return whatever result we have
            return traceResult;
        }
    }

    @Nonnull
    private BlockHitResult miss() {
        final Vec3 directionVec = this.start.subtract(this.end);
        return BlockHitResult.miss(this.end, Direction.getNearest(directionVec.x, directionVec.y, directionVec.z), new BlockPos(this.end));
    }

    // Fast path an empty air block as much as possible.  For tracing this would be the most common block
    // encountered.  As an FYI the logic needs to consider both the solid and fluid aspects of a block since
    // Minecraft now has this notion of water logged.
    @Nullable
    private BlockHitResult hitCheck(@Nonnull final BlockPos pos) {
        // Handle the block
        BlockHitResult traceResult = null;
        final BlockState state = this.world.getBlockState(pos);
//        if (!state.isAir(this.world, pos)) {
        if (!state.isAir()) {
            final VoxelShape voxelShape = this.blockMode.get(state, this.world, pos, this.selectionCtx);
            if (!voxelShape.isEmpty())
                traceResult = this.world.clipWithInteractionOverride(this.start, this.end, pos, voxelShape, state);
        }

        // Handle it's fluid state
        BlockHitResult fluidTraceResult = null;
        final FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty() && this.fluidMode.canPick(fluidState)) {
            final VoxelShape voxelFluidShape = state.getShape(this.world, pos);
            if (!voxelFluidShape.isEmpty())
                fluidTraceResult = voxelFluidShape.clip(this.start, this.end, pos);
        }

        // No results for either
        if (traceResult == fluidTraceResult)
            return null;
        // No fluid result
        if (fluidTraceResult == null)
            return traceResult;
        // No block result
        if (traceResult == null)
            return fluidTraceResult;

        // Get the closest.  It is possible to encounter the water before the solid, like a fence post that is
        // water logged.
        final double blockDistance = this.start.distanceToSqr(traceResult.getLocation());
        final double fluidDistance = this.start.distanceToSqr(fluidTraceResult.getLocation());
        return blockDistance <= fluidDistance ? traceResult : fluidTraceResult;
    }
}
