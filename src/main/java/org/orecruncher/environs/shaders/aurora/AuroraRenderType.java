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

package org.orecruncher.environs.shaders.aurora;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import org.orecruncher.environs.Environs;

@OnlyIn(Dist.CLIENT)
public class AuroraRenderType extends RenderType {
    public AuroraRenderType(String nameIn, VertexFormat formatIn, VertexFormat.Mode drawModeIn, int bufferSizeIn, boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
        super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
    }

    private static final TransparencyStateShard AURORA_TRANSPARENCY = new TransparencyStateShard(
            "aurora_transparency",
            () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            },
            RenderSystem::disableBlend);

    private static final OutputStateShard TARGET = WEATHER_TARGET;

    public static ResourceLocation TEXTURE = new ResourceLocation(Environs.MOD_ID,"textures/misc/aurora_band.png");

    public static final RenderType QUAD = create(
            "aurora_render_type",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            64, false, false,
            RenderType.CompositeState.builder()
                    .setTextureState(new TextureStateShard(TEXTURE, false, false))
                    .setTransparencyState(AURORA_TRANSPARENCY)
                    .setOutputState(TARGET)
//                    .setFogState(FOG)
//                    .setShadeModelState(RenderStateShard.SMOOTH_SHADE)
//                    .setAlphaState(DEFAULT_ALPHA)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .createCompositeState(false));
}
