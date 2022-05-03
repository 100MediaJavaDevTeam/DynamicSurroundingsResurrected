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

package org.orecruncher.lib.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class DSParticleRenderType implements ParticleRenderType {

    private final ResourceLocation texture;

    public DSParticleRenderType(@Nonnull final ResourceLocation texture) {
        this.texture = texture;
    }

    @Nonnull
    protected VertexFormat getVertexFormat() {
        return DefaultVertexFormat.PARTICLE;
    }

    @Override
    public void begin(@Nonnull final BufferBuilder buffer, @Nonnull final TextureManager textureManager) {
        RenderSystem.setShaderTexture(0, getTexture());
        buffer.begin(VertexFormat.Mode.QUADS, getVertexFormat());
    }

    protected ResourceLocation getTexture() {
        return this.texture;
    }

    @Override
    public void end(@Nonnull final Tesselator tessellator) {
        tessellator.end();
    }

    @Override
    public String toString() {
        return this.texture.toString();
    }
}
