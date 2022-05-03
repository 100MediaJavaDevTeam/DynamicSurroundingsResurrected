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

package org.orecruncher.sndctrl.audio.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.WorldUtils;
import org.orecruncher.sndctrl.library.AudioEffectLibrary;

@OnlyIn(Dist.CLIENT)
public final class WorldContext {

    /**
     * Quick Minecraft reference
     */
    public final Minecraft mc;
    /**
     * Reference to the client side PlayerEntity
     */
    public final Player player;
    /**
     * Reference to the player's world
     */
    public final LevelReader world;
    /**
     * Position of the player.
     */
    public final Vec3 playerPosition;
    /**
     * Position of the player's eyes.
     */
    public final Vec3 playerEyePosition;
    /**
     * Block position of the player.
     */
    public final BlockPos playerPos;
    /**
     * Block position of the player's eyes.
     */
    public final BlockPos playerEyePos;
    /**
     * Flag indicating if it is precipitating
     */
    public final boolean isPrecipitating;
    /**
     * Current strength of precipitation.
     */
    public final float precipitationStrength;
    /**
     * Coefficient used for dampening sound.  Usually caused by the player's head being in lava or water.
     */
    public final float auralDampening;

    public WorldContext() {
        if (GameUtils.isInGame()) {
            final Level w = GameUtils.getWorld();
            this.world = w;
            assert world != null;

            this.player = GameUtils.getPlayer();
            assert this.player != null;

            this.isPrecipitating = w.isRaining();
            this.playerPosition = this.player.position();
            this.playerEyePosition = this.player.getEyePosition(1F);
            this.playerPos = new BlockPos(this.playerPosition);
            this.playerEyePos = new BlockPos(this.playerEyePosition);

            final Fluid fs = this.player.level.getFluidState(this.playerEyePos).getType();
            final ResourceLocation name = fs.getRegistryName();
            if (name != null)
                this.auralDampening = AudioEffectLibrary.getFluidCoeffcient(name);
            else
                this.auralDampening = 0;

            // Get our current rain strength.
            this.precipitationStrength = WorldUtils.getRainStrength(w, 1F);
            this.mc = Minecraft.getInstance();
        } else {
            this.mc = null;
            this.player = null;
            this.world = null;
            this.isPrecipitating = false;
            this.playerPosition = Vec3.ZERO;
            this.playerEyePosition = Vec3.ZERO;
            this.playerPos = BlockPos.ZERO;
            this.playerEyePos = BlockPos.ZERO;
            this.auralDampening = 0;
            this.precipitationStrength = 0F;
        }
    }

    public boolean isNotValid() {
        return this.mc == null;
    }

}
