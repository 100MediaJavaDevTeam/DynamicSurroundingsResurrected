/*
 * Dynamic Surroundings
 * Copyright (C) 2021  OreCruncher
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.sndctrl.api.sound.ISoundCategory;
import org.orecruncher.sndctrl.api.sound.ISoundInstance;
import org.orecruncher.sndctrl.audio.handlers.SoundVolumeEvaluator;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class DSSoundInstance extends AbstractSoundInstance implements ISoundInstance {

    private SoundState state;
    private final ISoundCategory category;

    private int playDelay;

    public DSSoundInstance(@Nonnull final SoundEvent event, @Nonnull final ISoundCategory cat) {
        this(event.getLocation(), cat);
    }

    public DSSoundInstance(@Nonnull final ResourceLocation soundResource, @Nonnull final ISoundCategory cat) {
        super(soundResource, cat.getRealCategory(), new XoroshiroRandomSource(0));

        this.state = SoundState.NONE;
        this.category = cat;
        this.volume = 1F;
        this.pitch = 1F;
        this.x = this.y = this.z = 0;
        this.looping = false;
        this.delay = 0;
        this.attenuation = DSSoundInstance.Attenuation.LINEAR;

        this.playDelay = 0;
        this.sound = SoundManager.EMPTY_SOUND;

        // Force creation of the sound instance now.  Need the info in the OGG definition for attenuation
        // distance prior to submission.
        this.resolve(GameUtils.getSoundHander());
    }

    @Override
    @Nonnull
    public ISoundCategory getSoundCategory() {
        return this.category;
    }

    @Override
    @Nonnull
    public SoundState getState() {
        return this.state;
    }

    @Override
    public void setState(@Nonnull final SoundState state) {
        this.state = state;
    }

    public void setPitch(final float p) {
        this.pitch = p;
    }

    public void setPosition(final float x, final float y, final float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setPosition(@Nonnull final Vec3i pos) {
        this.setPosition(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
    }

    public void setPosition(@Nonnull final Vec3 pos) {
        this.setPosition((float) pos.x, (float) pos.y, (float) pos.z);
    }

    public void setAttenuationType(@Nonnull final DSSoundInstance.Attenuation type) {
        this.attenuation = type;
    }

    public void setRepeat(final boolean flag) {
        this.looping = flag;
    }

    public void setRepeatDelay(final int delay) {
        this.delay = delay;
    }

    public void setVolume(final float v) {
        this.volume = v;
    }

    public void scaleVolume(final float v) {
        this.volume *= v;
    }

    public void setGlobal(final boolean flag) {
        this.relative = flag;
    }

    public boolean isDonePlaying() {
        return getState().isTerminal();
    }

    @Override
    public int getPlayDelay() {
        return this.playDelay;
    }

    public void setPlayDelay(final int delay) {
        this.playDelay = delay;
    }

    @Override
    @Nonnull
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .addValue(getLocation().toString())
                .addValue(getSoundCategory().toString())
                .addValue(getAttenuation().toString())
                .addValue(getState().toString())
                .add("v", getVolume())
                .add("ev", getEffectiveVolume(this))
                .add("p", getPitch())
                .add("x", getX())
                .add("y", getY())
                .add("z", getZ())
                .toString();
    }

    static float getEffectiveVolume(@Nonnull final SoundInstance sound) {
        try {
            return SoundVolumeEvaluator.getClampedVolume(sound);
        } catch(@Nonnull final Throwable ignored) {
        }
        return -1;
    }

}
