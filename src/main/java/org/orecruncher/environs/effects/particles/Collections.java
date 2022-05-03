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

package org.orecruncher.environs.effects.particles;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.environs.Environs;
import org.orecruncher.environs.config.Config;
import org.orecruncher.lib.particles.CollectionManager;
import org.orecruncher.lib.particles.DSParticleRenderType;
import org.orecruncher.lib.particles.IParticleCollection;
import org.orecruncher.lib.particles.IParticleMote;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = Environs.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class Collections {

    private static final DSParticleRenderType RIPPLE_RENDER =
            new DSParticleRenderType(new ResourceLocation(Environs.MOD_ID,"textures/particles/ripple.png")) {

                @Override
                protected ResourceLocation getTexture() {
                    return Config.CLIENT.effects.waterRippleStyle.get().getTexture();
                }

                @Override
                public void begin(@Nonnull BufferBuilder buffer, @Nonnull TextureManager textureManager) {
                    super.begin(buffer, textureManager);
                    RenderSystem.depthMask(true);
                    RenderSystem.enableBlend();
                    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//                    RenderSystem.alphaFunc(516, 0.003921569F);
                }
            };

    private static final DSParticleRenderType SPRAY_RENDER = new DSParticleRenderType(new ResourceLocation(Environs.MOD_ID,"textures/particles/rainsplash.png"));
    private static final ParticleRenderType FIREFLY_RENDER = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;

    private final static IParticleCollection theRipples = CollectionManager.create("Rain Ripples", RIPPLE_RENDER);
    private final static IParticleCollection theSprays = CollectionManager.create("Water Spray", SPRAY_RENDER);
    private final static IParticleCollection theFireFlies = CollectionManager.create("Fireflies", FIREFLY_RENDER);

    private Collections() {

    }

    public static void addWaterRipple(@Nonnull final BlockGetter world, final double x, final double y,
                                      final double z) {
        if (theRipples.canFit()) {
            final IParticleMote mote = new MoteWaterRipple(world, x, y, z);
            theRipples.add(mote);
        }
    }

    public static boolean addWaterSpray(@Nonnull final BlockGetter world, final double x, final double y,
                                              final double z, final double dX, final double dY, final double dZ) {
        if (theSprays.canFit()) {
            final IParticleMote mote = new MoteWaterSpray(world, x, y, z, dX, dY, dZ);
            theSprays.add(mote);
            return true;
        }
        return false;
    }

    public static boolean canFitWaterSpray() {
        return theSprays.canFit();
    }

    public static void addRainSplash(@Nonnull final BlockGetter world, final double x, final double y,
                                              final double z) {
        if (theSprays.canFit()) {
            final IParticleMote mote = new MoteRainSplash(world, x, y, z);
            theSprays.add(mote);
        }
    }

    public static void addFireFly(@Nonnull final BlockGetter world, final double x, final double y, final double z) {
        if (theFireFlies.canFit()) {
            final IParticleMote mote = new MoteFireFly(world, x, y, z);
            theFireFlies.add(mote);
        }
    }

}
