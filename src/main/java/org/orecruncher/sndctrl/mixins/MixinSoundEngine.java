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

import com.google.common.collect.Multimap;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import net.minecraft.client.Options;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.api.sound.Category;
import org.orecruncher.sndctrl.api.sound.ISoundInstance;
import org.orecruncher.sndctrl.audio.AudioEngine;
import org.orecruncher.sndctrl.audio.SoundUtils;
import org.orecruncher.sndctrl.audio.handlers.SoundFXProcessor;
import org.orecruncher.sndctrl.audio.handlers.SoundVolumeEvaluator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Mixin(SoundEngine.class)
public class MixinSoundEngine {

    @Final
    @Shadow
    private Library library;

    @Final
    @Shadow
    public Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;

    @Final
    @Shadow
    private Options options;

    @Final
    @Shadow
    private Map<SoundInstance, Integer> soundDeleteTime;

    @Final
    @Shadow
    private Multimap<SoundSource, SoundInstance> instanceBySource;

    @Final
    @Shadow
    private List<TickableSoundInstance> tickingSounds;

    /**
     * Calculates the volume of sound based on the myriad of factors in the game as
     * well as configuration.
     *
     * @param sound The sound instance that is being evaluated
     * @param ci    The callback context used to return the result of the calculation
     */
    @Inject(method = "calculateVolume", at = @At("HEAD"), cancellable = true)
    private void getClampedVolume(SoundInstance sound, CallbackInfoReturnable<Float> ci) {
        try {
            ci.setReturnValue(SoundVolumeEvaluator.getClampedVolume(sound));
        } catch (final Throwable ignored) {
        }
    }

    /**
     * Callback hook to initialize sndcntrl when the SoundEngine initializes.
     *
     * @param ci ignored
     */
    @Inject(method = "loadLibrary", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/audio/Library;init(Ljava/lang/String;)V", shift = At.Shift.AFTER))
    public void initialize(CallbackInfo ci) {
        SoundUtils.initialize(this.library);
    }

    /**
     * Callback hook to deinitialize sndctrl when the SoundEngine is unloaded.
     *
     * @param ci ignored
     */
    @Inject(method = "destroy", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/audio/Library;cleanup()V", shift = At.Shift.BEFORE))
    public void deinitialize(CallbackInfo ci) {
        SoundUtils.deinitialize(this.library);
    }

    /**
     * Callback will trigger creation of sound context information for the sound play once it has been queued to the
     * sound engine.  It will also perform the first calculations of sound effects based on the player environment.
     *
     * @param p_sound            The sound that is being played
     * @param ci                 Ignored
     * @param soundeventaccessor Ignored
     * @param resourcelocation   Ignored
     * @param sound              Ignored
     * @param f                  Ignored
     * @param f1                 Ignored
     * @param soundcategory      Ignored
     * @param f2                 Ignored
     * @param f3                 Ignored
     * @param attenuationtype    Ignored
     * @param flag               Ignored
     * @param vector3d           Ignored
     * @param flag2              Ignored
     * @param flag3              Ignored
     * @param completablefuture  Ignored
     * @param entry              The ChannelManager entry that is being queued to the SoundEngine for off thread processing.
     */
    @Inject(method = "play", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;execute(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void onSoundPlay(SoundInstance p_sound, CallbackInfo ci, WeighedSoundEvents soundeventaccessor, ResourceLocation resourcelocation, Sound sound, float f, float f1, SoundSource soundcategory, float f2, float f3, SoundInstance.Attenuation attenuationtype, boolean flag, Vec3 vector3d, boolean flag2, boolean flag3, CompletableFuture completablefuture, ChannelAccess.ChannelHandle entry) {
        try {
            SoundFXProcessor.onSoundPlay(p_sound, entry);
            AudioEngine.onPlaySound(p_sound);
        } catch(@Nonnull final Throwable t) {
            SoundControl.LOGGER.error(t, "Error in onSoundPlay()!");
        }
    }

    /**
     * Need to tick and handle sounds that are tagged with CONFIG category even if the game is paused.
     *
     * @param isGamePaused Flag indicating whether game is paused
     * @param ci           Ignored
     */
    @Inject(method = "tick", at = @At("RETURN"))
    public void tick(final boolean isGamePaused, @Nonnull final CallbackInfo ci) {
        if (!isGamePaused)
            return;

        final Iterator<Map.Entry<SoundInstance, ChannelAccess.ChannelHandle>> iterator = this.instanceToChannel.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<SoundInstance, ChannelAccess.ChannelHandle> entry = iterator.next();
            if (entry.getKey() instanceof ISoundInstance) {
                final ISoundInstance instance = (ISoundInstance) entry.getKey();
                // Skip non-config sounds
                if (instance.getSoundCategory() != Category.CONFIG)
                    continue;
                final ChannelAccess.ChannelHandle channelmanager$entry1 = entry.getValue();
                float f2 = this.options.getSoundSourceVolume(instance.getSource());
                if (f2 <= 0.0F) {
                    channelmanager$entry1.execute(Channel::stop);
                    iterator.remove();
                } else if (channelmanager$entry1.isStopped()) {
                    iterator.remove();
                    this.soundDeleteTime.remove(instance);

                    try {
                        this.instanceBySource.remove(instance.getSource(), instance);
                    } catch (RuntimeException ignore) {
                    }

                    if (instance instanceof TickableSoundInstance) {
                        this.tickingSounds.remove(instance);
                    }
                }
            }
        }
    }
}
