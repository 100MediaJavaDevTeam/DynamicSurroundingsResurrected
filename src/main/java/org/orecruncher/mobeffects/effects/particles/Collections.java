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

package org.orecruncher.mobeffects.effects.particles;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.particles.CollectionManager;
import org.orecruncher.lib.particles.IParticleCollection;
import org.orecruncher.lib.particles.IParticleMote;
import org.orecruncher.lib.particles.DSParticleRenderType;
import org.orecruncher.mobeffects.MobEffects;
import org.orecruncher.mobeffects.footsteps.FootprintStyle;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public final class Collections {

    private static final DSParticleRenderType FOOTPRINT_RENDER =
            new DSParticleRenderType(new ResourceLocation(MobEffects.MOD_ID, "textures/particles/footprint.png")) {
                @Override
                public void begin(@Nonnull final BufferBuilder buffer, @Nonnull final TextureManager textureManager) {
                    super.begin(buffer, textureManager);
                    RenderSystem.depthMask(false);
                    RenderSystem.enableBlend();
                    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                }
            };

    private final static IParticleCollection thePrints = CollectionManager.create("Footprints", FOOTPRINT_RENDER);

    private Collections() {

    }

    public static void addFootprint(@Nonnull final FootprintStyle style, @Nonnull final Level world,
                                    final Vec3 loc, final float rot, final float scale, final boolean isRight) {
        if (thePrints.canFit()) {
            final IParticleMote mote = new FootprintMote(style, world, loc.x, loc.y, loc.z, rot, scale, isRight);
            thePrints.add(mote);
        }
    }
}
