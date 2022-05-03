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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.environs.shaders.ShaderPrograms;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.lib.shaders.ShaderCallContext;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/*
 * Renders a shader generated aurora along a curved path.  Makes it ribbon like.
 */
@OnlyIn(Dist.CLIENT)
public class AuroraShaderBand extends AuroraBase {

	private static final float V1 = 0;
	private static final float V2 = 0.5F;
	
	protected ShaderPrograms program;
	protected Consumer<ShaderCallContext> callback;

	protected final float auroraWidth;
	protected final float panelTexWidth;
	
	public AuroraShaderBand(final long seed) {
		super(seed);

		this.program = ShaderPrograms.AURORA;

		this.callback = shaderCallContext -> {
			shaderCallContext.set("time", AuroraUtils.getTimeSeconds() * 0.75F);
			shaderCallContext.set("resolution", AuroraShaderBand.this.getAuroraWidth(), AuroraShaderBand.this.getAuroraHeight());
			shaderCallContext.set("topColor", AuroraShaderBand.this.getFadeColor());
			shaderCallContext.set("middleColor", AuroraShaderBand.this.getMiddleColor());
			shaderCallContext.set("bottomColor", AuroraShaderBand.this.getBaseColor());
			shaderCallContext.set("alpha", AuroraShaderBand.this.getAlpha());
		};

		this.auroraWidth = this.band.getPanelCount() * this.band.getNodeWidth();
		this.panelTexWidth = this.band.getNodeWidth() / this.auroraWidth;
	}

	@Override
	public void update() {
		super.update();
		this.band.update();
	}

	@Override
	protected float getAlpha() {
		return MathStuff.clamp((this.band.getAlphaLimit() / 255F) * this.tracker.ageRatio() * 2.0F, 0F, 1F);
	}

	protected float getAuroraWidth() {
		return this.auroraWidth;
	}

	protected float getAuroraHeight() {
		return AuroraBand.AURORA_AMPLITUDE;
	}

	protected void generateBand(@Nonnull final VertexConsumer builder, @Nonnull final Matrix4f matrix) {

		for (int i = 0; ; i++) {
			final Vector3f[] quad = this.band.getPanelQuad(i);
			if (quad == null)
				break;

			final float u1 = i * this.panelTexWidth;
			final float u2 = u1 + this.panelTexWidth;

			builder.vertex(matrix, quad[0].x(), quad[0].y(), quad[0].z()).uv(u1, V1).endVertex();
			builder.vertex(matrix, quad[1].x(), quad[1].y(), quad[1].z()).uv(u2, V1).endVertex();
			builder.vertex(matrix, quad[2].x(), quad[2].y(), quad[2].z()).uv(u2, V2).endVertex();
			builder.vertex(matrix, quad[3].x(), quad[3].y(), quad[3].z()).uv(u1, V2).endVertex();
		}

	}

	@Override
	public void render(@Nonnull final PoseStack matrixStack, final float partialTick) {

		if (this.program == null)
			return;

		this.band.translate(partialTick);

		final double tranY = getTranslationY(partialTick);
		final double tranX = getTranslationX(partialTick);
		final double tranZ = getTranslationZ(partialTick);

		final Vec3 view = GameUtils.getMC().gameRenderer.getMainCamera().getPosition();
		matrixStack.pushPose();
		matrixStack.translate(-view.x(), -view.y(), -view.z());

		final RenderType type = AuroraRenderType.QUAD;
		final MultiBufferSource.BufferSource buffer = GameUtils.getMC().renderBuffers().bufferSource();

		ShaderPrograms.MANAGER.useShader(this.program, this.callback);

		try {

			for (int b = 0; b < this.bandCount; b++) {
				final VertexConsumer builder = buffer.getBuffer(type);
				matrixStack.pushPose();
				matrixStack.translate(tranX, tranY, tranZ + this.offset * b);
				generateBand(builder, matrixStack.last().pose());
				matrixStack.popPose();
				RenderSystem.disableDepthTest();
				buffer.endBatch(type);
			}

		} catch (final Exception ex) {
			ex.printStackTrace();
			this.program = null;
		}

		ShaderPrograms.MANAGER.releaseShader();

		matrixStack.popPose();
	}

	@Override
	public String toString() {
		return "<SHADER> " + super.toString();
	}
}
