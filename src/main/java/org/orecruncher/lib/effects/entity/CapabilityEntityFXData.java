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

package org.orecruncher.lib.effects.entity;

import dev._100media.capabilitysyncer.core.CapabilityAttacher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;
import org.orecruncher.sndctrl.SoundControl;

@OnlyIn(Dist.CLIENT)
public class CapabilityEntityFXData extends CapabilityAttacher {
	private static final Class<IEntityFX> CAPABILITY_CLASS = IEntityFX.class;
	public static final Capability<IEntityFX> FX_INFO_CAPABILITY = getCapability(new CapabilityToken<>() {});
	public static final ResourceLocation CAPABILITY_ID = new ResourceLocation(SoundControl.MOD_ID, "entityfx");

	public static void register() {
		CapabilityAttacher.registerCapability(CAPABILITY_CLASS);
		CapabilityAttacher.registerEntityAttacher(LivingEntity.class, CapabilityEntityFXData::attach, CapabilityEntityFXData::getFxInfo);
	}

	@Nullable
	public static IEntityFX getFxInfoUnwrap(Entity entity) {
		return getFxInfo(entity).orElse(null);
	}

	public static LazyOptional<IEntityFX> getFxInfo(Entity entity) {
		return entity.getCapability(FX_INFO_CAPABILITY);
	}

	@SuppressWarnings("ConstantConditions")
	private static void attach(AttachCapabilitiesEvent<Entity> event, LivingEntity livingEntity) {
		// Check for null - some mods do not honor the contract
		if (livingEntity.level != null && livingEntity.level.isClientSide) {
			genericAttachCapability(event, new EntityFXData(livingEntity), FX_INFO_CAPABILITY, CAPABILITY_ID, false);
		}
	}
}