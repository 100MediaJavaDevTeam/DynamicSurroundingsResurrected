/*
 *  Dynamic Surroundings: Environs
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

import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.math.MathStuff;

@OnlyIn(Dist.CLIENT)
public class AuroraBand {

	protected static final float ANGLE1 = MathStuff.PI_F / 16.0F;
	protected static final float ANGLE2 = MathStuff.toRadians(90.0F / 7.0F);
	protected static final float AURORA_SPEED = 0.75F;
	public static final float AURORA_AMPLITUDE = 18.0F;

	protected final Random random;

	protected Panel[] nodes;
	protected float cycle = 0.0F;
	protected int alphaLimit = 128;
	protected int length;
	protected float nodeLength;
	protected float nodeWidth;

	public AuroraBand(final Random random,
					  final AuroraFactory.AuroraGeometry geo, final boolean noTaper,
					  final boolean fixedHeight) {
		this.random = random;
		preset(geo);
		generateBands(noTaper, fixedHeight);
		translate(0);
	}

	public AuroraBand(final Random random, final AuroraFactory.AuroraGeometry geo) {
		this(random, geo, false, false);
	}

	protected AuroraBand(final Panel[] nodes, final AuroraBand band) {
		this.random = band.random;
		this.nodes = nodes;
		this.cycle = band.cycle;
		this.length = band.length;
		this.nodeLength = band.nodeLength;
		this.nodeWidth = band.nodeWidth;
		this.alphaLimit = band.alphaLimit;
		translate(0);
	}

	public int getAlphaLimit() {
		return this.alphaLimit;
	}

	@Nonnull
	public Panel[] getNodeList() {
		return this.nodes;
	}

	public float getNodeWidth() {
		return this.nodeWidth;
	}

	public float getCycle() {
		return this.cycle;
	}
	
	public void update() {
		if ((this.cycle += AURORA_SPEED) >= 360.0F)
			this.cycle -= 360.0F;
	}

	public AuroraBand copy(final int offset) {
		final Panel[] newNodes = new Panel[this.nodes.length];
		for (int i = 0; i < this.nodes.length; i++)
			newNodes[i] = new Panel(this.nodes[i], offset);
		return new AuroraBand(newNodes, this);
	}

	/*
	 * Calculates the next "frame" of the aurora if it is being animated.
	 */
	public void translate(final float partialTick) {
		final float c = this.cycle + AURORA_SPEED * partialTick;
		for (int i = 0; i < this.nodes.length; i++) {
			// Travelling sine wave: https://en.wikipedia.org/wiki/Wavelength
			// final float f = MathStuff.cos(MathStuff.toRadians(AURORA_WAVELENGTH * i +
			// c));
			final float f = MathStuff.cos(MathStuff.toRadians((i << 3) + c));
			final Panel node = this.nodes[i];
			node.dZ = f * AURORA_AMPLITUDE;
			node.dY = f * 3.0F;

			final float mZ = node.getModdedZ();
			node.tetZ = mZ + node.sinDeg90;
			node.tetZ2 = mZ + node.sinDeg270;
		}
	}

	protected void preset(final AuroraFactory.AuroraGeometry geo) {
		this.length = geo.length;
		this.nodeLength = geo.nodeLength;
		this.nodeWidth = geo.nodeWidth;
		this.alphaLimit = geo.alphaLimit;
	}

	protected void generateBands(final boolean noTaper, final boolean fixedHeight) {
		this.nodes = populate(noTaper, fixedHeight);
		final float factor = MathStuff.PI_F / (this.length / 4);
		final int lowerBound = this.length / 8 + 1;
		final int upperBound = this.length * 7 / 8 - 1;

		int count = 0;
		for (int i = 0; i < this.length; i++) {
			// Scale the widths at the head and tail of the
			// aurora band. This makes them taper.
			float width;
			if (noTaper) {
				width = this.nodeWidth;
			} else if (i < lowerBound) {
				width = MathStuff.sin(factor * count++) * this.nodeWidth;
			} else if (i > upperBound) {
				width = MathStuff.sin(factor * count--) * this.nodeWidth;
			} else {
				width = this.nodeWidth;
			}

			this.nodes[i].setWidth(width);
		}
	}

	@Nonnull
	protected Panel[] populate(final boolean noTaper, final boolean fixedHeight) {
		final Panel[] nodeList = new Panel[this.length];
		final int bound = this.length / 2 - 1;

		float angleTotal = 0.0F;
		for (int i = this.length / 8 / 2 - 1; i >= 0; i--) {
			float angle = (this.random.nextFloat() - 0.5F) * 8.0F;
			angleTotal += angle;
			if (MathStuff.abs(angleTotal) > 180.0F) {
				angle = -angle;
				angleTotal += angle;
			}

			for (int k = 7; k >= 0; k--) {
				final int idx = i * 8 + k;
				if (idx == bound) {
					final float amplitude = fixedHeight ? AURORA_AMPLITUDE : (7.0F + this.random.nextFloat());
					nodeList[idx] = new Panel(0.0F, amplitude, 0.0F, angle);
				} else {
					float y;
					if (fixedHeight)
						y = AURORA_AMPLITUDE;
					else if (i == 0)
						y = MathStuff.sin(ANGLE1 * k) * 7.0F + this.random.nextFloat() / 2.0F;
					else
						y = 10.0F + this.random.nextFloat() * 5.0F;

					final Panel node = nodeList[idx + 1];
					final float subAngle = node.angle + angle;
					final float subAngleRads = MathStuff.toRadians(subAngle);
					final float z = node.posZ - (MathStuff.sin(subAngleRads) * this.nodeLength);
					final float x = node.posX - (MathStuff.cos(subAngleRads) * this.nodeLength);

					nodeList[idx] = new Panel(x, y, z, subAngle);
				}
			}
		}

		angleTotal = 0.0F;
		for (int j = this.length / 8 / 2; j < this.length / 8; j++) {
			float angle = (this.random.nextFloat() - 0.5F) * 8.0F;
			angleTotal += angle;
			if (MathStuff.abs(angleTotal) > 180.0F) {
				angle = -angle;
				angleTotal += angle;
			}
			for (int h = 0; h < 8; h++) {
				float y;
				if (fixedHeight) {
					y = AURORA_AMPLITUDE;
				} else if (j == this.length / 8 - 1)
					y = MathStuff.cos(ANGLE2 * h) * 7.0F + this.random.nextFloat() / 2.0F;
				else
					y = 10.0F + this.random.nextFloat() * 5.0F;

				final Panel node = nodeList[j * 8 + h - 1];
				final float subAngle = node.angle + angle;
				final float subAngleRads = MathStuff.toRadians(subAngle);
				final float z = node.posZ + (MathStuff.sin(subAngleRads) * this.nodeLength);
				final float x = node.posX + (MathStuff.cos(subAngleRads) * this.nodeLength);

				nodeList[j * 8 + h] = new Panel(x, y, z, subAngle);
			}
		}

		return nodeList;
	}

}
