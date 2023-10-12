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

package org.orecruncher.dsurround.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.dsurround.DynamicSurroundings;
import org.orecruncher.dsurround.config.Config;
import org.orecruncher.dsurround.huds.lightlevel.LightLevelHUD;
import org.orecruncher.lib.GameUtils;

@Mod.EventBusSubscriber(modid = DynamicSurroundings.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Keys {

    private static KeyMapping lightLevelHUD;
    private static KeyMapping chunkBorders;

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        if ((Config.CLIENT.logging.flagMask.get() & Config.Flags.ALLOW_CHUNK_BORDER_HUD) != 0) {
            lightLevelHUD = new KeyMapping(
                    "dsurround.text.lightlevel.toggle",
                    InputConstants.UNKNOWN.getValue(),
                    "dsurround.text.controls.group");
            event.register(lightLevelHUD);
        }

        if ((Config.CLIENT.logging.flagMask.get() & Config.Flags.ALLOW_LIGHTLEVEL_HUD) != 0) {
            chunkBorders = new KeyMapping(
                    "dsurround.text.chunkborders.toggle",
                    InputConstants.UNKNOWN.getValue(),
                    "dsurround.text.controls.group");
            event.register(chunkBorders);
        }
    }

    @SubscribeEvent
    public static void keyPressed(InputEvent.Key event) {
        if (GameUtils.getMC().screen == null && GameUtils.getPlayer() != null) {
            if (lightLevelHUD != null && lightLevelHUD.consumeClick()) {
                LightLevelHUD.toggleDisplay();
            }

            if (chunkBorders != null && chunkBorders.consumeClick()) {
                GameUtils.getMC().debugRenderer.switchRenderChunkborder();
            }
        }
    }
}