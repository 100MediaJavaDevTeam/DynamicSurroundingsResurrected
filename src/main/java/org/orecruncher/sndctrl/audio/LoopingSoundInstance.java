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

package org.orecruncher.sndctrl.audio;

import com.google.common.base.MoreObjects;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.sndctrl.api.sound.ISoundCategory;
import org.orecruncher.sndctrl.api.sound.ISoundInstance;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class LoopingSoundInstance extends WrappedSoundInstance {

    private final Vec3 position;

    public LoopingSoundInstance(@Nonnull final ISoundInstance sound, @Nonnull final ISoundCategory category) {
        super(sound, category);
        this.position = null;
    }

    public LoopingSoundInstance(@Nonnull final ISoundInstance sound) {
        super(sound);
        this.position = null;
    }

    public LoopingSoundInstance(@Nonnull final ISoundInstance sound, @Nonnull final Vec3 position) {
        super(sound);
        this.position = position;
    }

    @Override
    public boolean isLooping() {
        return true;
    }

    @Override
    public int getDelay() {
        return 0;
    }

    @Override
    public int getPlayDelay() {
        return this.sound.getPlayDelay();
    }

    @Override
    public void setPlayDelay(final int delay) {
        this.sound.setPlayDelay(delay);
    }

    @Override
    public double getX() {
        return this.position != null ? (float) this.position.x : super.getX();
    }

    @Override
    public double getY() {
        return this.position != null ? (float) this.position.y : super.getY();
    }

    @Override
    public double getZ() {
        return this.position != null ? (float) this.position.z : super.getZ();
    }

    @Override
    public Attenuation getAttenuation() {
        return Attenuation.LINEAR;
    }

    @Override
    @Nonnull
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .addValue(getLocation().toString())
                .addValue(getSoundCategory().toString())
                .addValue(getState().toString())
                .add("v", getVolume())
                .add("ev", DSSoundInstance.getEffectiveVolume(this))
                .add("p", getPitch())
                .add("x", getX())
                .add("y", getY())
                .add("z", getZ())
                .toString();
    }

}
