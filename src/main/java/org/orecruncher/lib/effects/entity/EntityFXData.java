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

import javax.annotation.Nullable;

import dev._100media.capabilitysyncer.core.EntityCapability;
import dev._100media.capabilitysyncer.network.EntityCapabilityStatusPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.simple.SimpleChannel;
import org.orecruncher.lib.effects.EntityEffectManager;


@OnlyIn(Dist.CLIENT)
public class EntityFXData extends EntityCapability implements IEntityFX {

	protected EntityEffectManager handler;

	public EntityFXData(LivingEntity entity) {
		super(entity);
	}

	@Override
	public void set(@Nullable final EntityEffectManager handler) {
		this.handler = handler;
	}

	@Override
	@Nullable
	public EntityEffectManager get() {
		return this.handler;
	}

	// Since this is a client-only cap, we don't need any of this stuff

	@Override
	public EntityCapabilityStatusPacket createUpdatePacket() {
		return null;
	}

	@Override
	public SimpleChannel getNetworkChannel() {
		return null;
	}

	@Override
	public CompoundTag serializeNBT(boolean savingToDisk) {
		return null;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt, boolean readingFromDisk) {}
}