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
package org.orecruncher.sndctrl.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.sndctrl.SoundControl;

@Mod.EventBusSubscriber(modid = SoundControl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeysModBus {

    public static KeyMapping quickVolumeGui;
    public static KeyMapping soundConfigGui;

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        quickVolumeGui = new KeyMapping(
                "sndctrl.text.quickvolumemenu.open",
                InputConstants.UNKNOWN.getValue(),
                "dsurround.text.controls.group");
        quickVolumeGui.setKeyModifierAndCode(KeyModifier.CONTROL, InputConstants.getKey("key.keyboard.v"));

        soundConfigGui = new KeyMapping(
                "sndctrl.text.soundconfig.open",
                InputConstants.UNKNOWN.getValue(),
                "dsurround.text.controls.group");
        soundConfigGui.setKeyModifierAndCode(KeyModifier.CONTROL, InputConstants.getKey("key.keyboard.i"));

        event.register(quickVolumeGui);
        event.register(soundConfigGui);
    }

}