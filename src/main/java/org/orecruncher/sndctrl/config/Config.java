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

package org.orecruncher.sndctrl.config;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.orecruncher.dsurround.DynamicSurroundings;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.library.IndividualSoundConfig;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = SoundControl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Config {
    public static final Client CLIENT;
    private static final String CLIENT_CONFIG = DynamicSurroundings.MOD_ID + File.separator + SoundControl.MOD_ID + "-client.toml";
    public static final ForgeConfigSpec SPEC;

    static {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    private Config() {
    }

    private static void applyConfig() {
        CLIENT.update();
        SoundControl.LOGGER.setDebug(Config.CLIENT.logging.get_enableLogging());
        SoundControl.LOGGER.setTraceMask(Config.CLIENT.logging.get_flagMask());
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {
        applyConfig();
        SoundControl.LOGGER.debug("Loaded config file %s", configEvent.getConfig().getFileName());
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfig.Reloading configEvent) {
        SoundControl.LOGGER.debug("Config file changed %s", configEvent.getConfig().getFileName());
        applyConfig();
    }

    public static void setup() {
        // The subdir with the mod ID name should have already been created
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC, CLIENT_CONFIG);
    }

    public static class Trace {
        public static final int SOUND_PLAY = 0x1;
        public static final int BASIC_SOUND_PLAY = 0x2;
    }

    public static class Client {

        public static final List<String> defaultSoundConfig = ImmutableList.<String>builder()
                .add("minecraft:entity.sheep.ambient cull")
                .add("minecraft:entity.chicken.ambient cull")
                .add("minecraft:entity.cow.ambient cull")
                .add("minecraft:entity.pig.ambient cull")
                .add("minecraft:entity.llama.ambient cull")
                .add("minecraft:entity.wither.death cull 25")
                .add("minecraft:entity.ender_dragon.death cull 25")
                .build();

        public static final List<String> defaultStartupSounds = ImmutableList.<String>builder()
                .add("minecraft:entity.experience_orb.pickup")
                .add("minecraft:entity.chicken.egg")
                .add("minecraft:ambient.underwater.exit")
                .build();

        @Nonnull
        public final Sound sound;
        @Nonnull
        public final Logging logging;
        @Nonnull
        public final Effects effects;

        Client(@Nonnull final ForgeConfigSpec.Builder builder) {
            this.sound = new Sound(builder);
            this.effects = new Effects(builder);
            this.logging = new Logging(builder);
        }

        void update() {
            this.sound.update();
            this.effects.update();
            this.logging.update();
        }

        public static class Sound {

            public final BooleanValue enableEnhancedSounds;
            public final BooleanValue enableOcclusionCalcs;
            public final BooleanValue enableMonoConversion;
            public final IntValue cullInterval;
            public final IntValue backgroundThreadWorkers;
            public final BooleanValue enhancedWeather;
            public final ConfigValue<List<? extends String>> individualSounds;
            public final ConfigValue<List<? extends String>> startupSoundList;

            private boolean _enableEnhancedSounds;
            private boolean _enableOcclusionCalcs;
            private boolean _enableMonoConversion;
            private int _cullInterval;
            private int _backgroundThreadWorkers;
            private boolean _enhancedWeather;
            private List<IndividualSoundConfig> _individualSounds;
            private List<String> _startupSoundList;

            Sound(@Nonnull final ForgeConfigSpec.Builder builder) {
                builder.comment("General options for defining sound effects")
                        .push("Sound Options");

                this.enableEnhancedSounds = builder
                        .worldRestart()
                        .comment("Enable sound reverb and filtering")
                        .translation("sndctrl.cfg.sound.EnhancedSounds")
                        .define("Enable Enhanced Sounds", true);

                this.enableOcclusionCalcs = builder
                        .comment("Enable sound occlusion calculations (sound muffling when positioned behind blocks)")
                        .translation("sndctrl.cfg.sound.Occlusion")
                        .define("Enable Sound Occlusion Calculations", true);

                this.enableMonoConversion = builder
                        .comment("Enable conversion of stereo sounds to mono format for spacial play")
                        .translation("sndctrl.cfg.sound.MonoConversion")
                        .define("Enable Stereo to Mono Conversion", true);

                this.enhancedWeather = builder
                        .worldRestart()
                        .comment("Enable enhanced sounds for weather effects")
                        .translation("sndctrl.cfg.sound.EnhancedWeather")
                        .define("Enable Enhanced Weather Sounds", true);

                this.individualSounds = builder
                        .comment("Options to configure sounds on an individual basis")
                        .translation("sndctrl.cfg.sound.Individual")
                        .defineList("Individual Sound Config", defaultSoundConfig, s -> true);

                this.startupSoundList = builder
                        .comment("Possible sounds to play when client reaches main game menu")
                        .translation("sndctrl.cfg.sound.StartupSounds")
                        .defineList("Startup Sound List", defaultStartupSounds, s -> true);

                this.cullInterval = builder
                        .comment("Ticks between culled sound events (0 to disable culling)")
                        .translation("sndctrl.cfg.sound.CullInterval")
                        .defineInRange("Sound Culling Interval", 20, 0, Integer.MAX_VALUE);

                this.backgroundThreadWorkers = builder
                        .worldRestart()
                        .comment("Number of background threads to handle sound effect calculations (0 is default)")
                        .translation("sndctrl.cfg.sound.Threads")
                        .defineInRange("Background Workers", 0, 0, 8);

                builder.pop();
            }

            void update() {
                this._backgroundThreadWorkers = this.backgroundThreadWorkers.get();
                this._enableEnhancedSounds = this.enableEnhancedSounds.get();
                this._enableOcclusionCalcs = this.enableOcclusionCalcs.get();
                this._enableMonoConversion = this.enableMonoConversion.get();
                this._cullInterval = this.cullInterval.get();
                this._enhancedWeather = this.enhancedWeather.get();
                this._individualSounds = this.individualSounds.get().stream().map(IndividualSoundConfig::createFrom).filter(Objects::nonNull).collect(Collectors.toList());
                this._startupSoundList = this.startupSoundList.get().stream().map(Object::toString).collect(Collectors.toList());
            }

            public int get_backgroundThreadWorkers() {
                return this._backgroundThreadWorkers;
            }

            public boolean get_enableEnhancedSounds() {
                return this._enableEnhancedSounds;
            }

            public boolean get_enableOcclusionCalcs() {
                return this._enableOcclusionCalcs;
            }

            public boolean get_enableMonoConversion() {
                return this._enableMonoConversion;
            }

            public int get_cullInterval() {
                return this._cullInterval;
            }

            public boolean get_enhancedWeather() {
                return this._enhancedWeather;
            }

            public List<IndividualSoundConfig> get_individualSounds() {
                return this._individualSounds;
            }

            public List<String> get_startupSoundList() {
                return this._startupSoundList;
            }
        }

        public static class Effects {

            public final BooleanValue fixupRandoms;
            public final IntValue effectRange;

            private boolean _fixupRandoms;
            private int _effectRange;

            Effects(@Nonnull final ForgeConfigSpec.Builder builder) {
                builder.comment("Defines parameters for special effects")
                        .push("Effect Options");

                this.fixupRandoms = builder
                        .worldRestart()
                        .comment("Replace client side Randomizers with faster versions")
                        .translation("sndctrl.cfg.effects.Randoms")
                        .define("Replace Randoms", true);

                this.effectRange = builder
                        .worldRestart()
                        .comment("Block range of entity special effect handling")
                        .translation("sndctrl.cfg.effects.BlockRange")
                        .defineInRange("Block Range", 24, 16, 64);

                builder.pop();
            }

            void update() {
                this._fixupRandoms = this.fixupRandoms.get();
                this._effectRange = this.effectRange.get();
            }

            public boolean get_fixupRandoms() {
                return this._fixupRandoms;
            }

            public int get_effectRange() {
                return this._effectRange;
            }
        }

        public static class Logging {

            public final BooleanValue enableLogging;
            public final IntValue flagMask;

            private boolean _enableLogging;
            private int _flagMask;

            Logging(@Nonnull final ForgeConfigSpec.Builder builder) {
                builder.comment("Defines how Sound Control logging will behave")
                        .push("Logging Options");

                this.enableLogging = builder
                        .comment("Enables/disables debug logging of the mod")
                        .translation("sndctrl.cfg.logging.EnableDebug")
                        .define("Debug Logging", false);

                this.flagMask = builder
                        .comment("Bitmask for toggling various debug traces")
                        .translation("sndctrl.cfg.logging.FlagMask")
                        .defineInRange("Debug Flag Mask", 0, 0, Integer.MAX_VALUE);

                builder.pop();
            }

            void update() {
                this._enableLogging = this.enableLogging.get();
                this._flagMask = this.flagMask.get();
            }

            public boolean get_enableLogging() {
                return this._enableLogging;
            }

            public int get_flagMask() {
                return this._flagMask;
            }
        }
    }
}
