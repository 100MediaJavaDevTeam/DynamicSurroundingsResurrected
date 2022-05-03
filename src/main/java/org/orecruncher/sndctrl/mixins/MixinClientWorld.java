/*
 * Dynamic Surroundings
 * Copyright (C) 2020 OreCruncher
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

package org.orecruncher.sndctrl.mixins;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.orecruncher.lib.world.ClientBlockUpdateHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class MixinClientWorld {

    /**
     * Inserts a hook into client side BlockState processing so that block changes can be monitored and listeners
     * notified of such changes.
     *
     * @param pos      Position of the block change that has occurred
     * @param oldState The old blockState
     * @param newState The new blockState
     * @param flags    Update flags
     * @param ci       Ignored
     */
    @Inject(method = "sendBlockUpdated", at = @At("RETURN"))
    public void blockUpdateCallback(BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci) {
        if (oldState != newState)
            ClientBlockUpdateHandler.blockUpdateCallback((ClientLevel) ((Object) this), pos, newState);
    }
}
