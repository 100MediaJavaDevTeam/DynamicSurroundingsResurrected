/*
 *  Dynamic Surroundings: Mob Effects
 *  Copyright (C) 2019  OreCruncher
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

package org.orecruncher.mobeffects.effects.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.orecruncher.lib.GameUtils;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class BubbleBreathParticle  extends TextureSheetParticle {
    public BubbleBreathParticle(@Nonnull final LivingEntity entity, final boolean isDrowning) {
        super((ClientLevel) entity.getCommandSenderWorld(), 0, 0, 0);

        // Reuse the bubble sheet
        final SpriteSet spriteSet = GameUtils.getMC().particleEngine.spriteSets.get(ForgeRegistries.PARTICLE_TYPES.getKey(ParticleTypes.BUBBLE));
        this.pickSprite(spriteSet);

        final Vec3 origin = ParticleUtils.getBreathOrigin(entity);
        final Vec3 trajectory = ParticleUtils.getLookTrajectory(entity);
        final double factor = isDrowning ? 0.02D : 0.005D;

        this.setPos(origin.x, origin.y, origin.z);
        this.xo = origin.x;
        this.yo = origin.y;
        this.zo = origin.z;

        this.xd = trajectory.x * factor;
        this.yd = trajectory.y * 0.002D;
        this.zd = trajectory.z * factor;

        this.gravity = 0F;

        this.setAlpha(0.2F);
        this.setSize(0.02F, 0.02F);
        this.quadSize *= this.random.nextFloat() * 0.6F + 0.2F;
        this.quadSize *= entity.isBaby() ? 0.125F : 0.25F;
        this.lifetime = (int) (8.0D / (Math.random() * 0.8D + 0.2D));
    }

    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.lifetime-- <= 0) {
            this.remove();
        } else {
            this.yd += 0.002D;
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.8500000238418579D;
            this.yd *= 0.8500000238418579D;
            this.zd *= 0.8500000238418579D;
            if (!this.level.getFluidState(new BlockPos(this.x, this.y, this.z)).is(FluidTags.WATER)) {
                this.remove();
            }
        }
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
