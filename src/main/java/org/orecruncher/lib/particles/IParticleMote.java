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

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public interface IParticleMote {

    /**
     * Indicates if a mote is active or is considered dead.
     *
     * @return true if the mote is active; false otherwise
     */
    boolean isAlive();

    /**
     * Kills a mote. It will stop displaying and be released from collections.
     */
    void kill();

    /**
     * Causes the mote to update advancing it's state one tick.
     *
     * @return true if the particle has been ticked; false if it should die
     */
    boolean tick();

    /**
     * Causes the mote to render itself.
     */
    void renderParticle(@Nonnull VertexConsumer buffer, @Nonnull Camera renderInfo, float partialTicks);

    /**
     * obtains the current position of the mote
     * @return Position of the mote
     */
    @Nonnull
    Vec3 getPosition();

}