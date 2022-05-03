/*
 *  Dynamic Surroundings: Mob Effects
 *  Copyright (C) 2019  OreCruncher
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.mobeffects.library;

import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.orecruncher.dsurround.DynamicSurroundings;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.lib.resource.IResourceAccessor;
import org.orecruncher.lib.resource.ResourceUtils;
import org.orecruncher.lib.service.IModuleService;
import org.orecruncher.lib.service.ModuleServiceManager;
import org.orecruncher.lib.validation.MapValidator;
import org.orecruncher.lib.validation.Validators;
import org.orecruncher.mobeffects.MobEffects;
import org.orecruncher.mobeffects.config.Config;
import org.orecruncher.mobeffects.library.config.EntityConfig;
import org.orecruncher.sndctrl.api.acoustics.Library;
import org.orecruncher.sndctrl.api.sound.SoundBuilder;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(
        modid = MobEffects.MOD_ID,
        value = {Dist.CLIENT},
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class EffectLibrary {

    private static final IModLog LOGGER = MobEffects.LOGGER.createChild(EffectLibrary.class);

    private static final ResourceLocation PLAYER = new ResourceLocation("minecraft:player");
    private static final EntityEffectInfo DEFAULT = new EntityEffectInfo();
    private static EntityEffectInfo playerEffects = DEFAULT;

    private static final Object2ObjectOpenHashMap<ResourceLocation, EntityEffectInfo> effectConfiguration = new Object2ObjectOpenHashMap<>();
    private static final Reference2ObjectOpenHashMap<Class<? extends Entity>, EntityEffectInfo> effects = new Reference2ObjectOpenHashMap<>();
    private static final Set<ResourceLocation> blockedSounds = new ObjectOpenHashSet<>();

    private static final Map<ResourceLocation, SoundEvent> soundReplace = new Object2ObjectOpenHashMap<>();

    private EffectLibrary() {

    }

    static void initialize() {
        ModuleServiceManager.instance().add(new EffectLibraryService());
    }

    public static boolean hasEffect(@Nonnull final Entity entity, @Nonnull final ResourceLocation loc) {
        return getEffectInfo(entity).effects.contains(loc);
    }

    @Nonnull
    private static EntityEffectInfo getEffectInfo(@Nonnull final Entity entity) {
        if (entity instanceof Player)
            return playerEffects;
        EntityEffectInfo eei = effects.get(entity.getClass());
        if (eei == null) {
            // Do we have a config for it?
            eei = effectConfiguration.get(entity.getType().getRegistryName());
            if (eei == null)
                eei = DEFAULT;
            effects.put(entity.getClass(), eei);
        }
        return eei;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void soundPlay(@Nonnull PlaySoundEvent e) {
        final SoundInstance theSound = e.getSound();
        if (theSound != null) {
            final ResourceLocation soundResource = theSound.getLocation();
            if (blockedSounds.contains(soundResource)) {
                e.setSound(null);
//                e.setResult(null);
            } else {
                final SoundEvent evt = soundReplace.get(soundResource);
                if (evt != null) {
                    e.setSound(SoundBuilder.builder(evt).from((AbstractSoundInstance) theSound).build());
//                    e.setResult(SoundBuilder.builder(evt).from((AbstractSoundInstance) theSound).build());
                }
            }
        }
    }

    private static class EffectLibraryService implements IModuleService {

        private static final Type entityConfigType = TypeToken.getParameterized(Map.class, String.class, EntityConfig.class).getType();

        static {
            Validators.registerValidator(entityConfigType, new MapValidator<String, EntityConfig>());
        }

        @Override
        public String name() {
            return "MobEffectsLibrary";
        }

        @Override
        public void start() {

            // Seed our configuration with known entities that have defaults
            ForgeRegistries.ENTITIES.forEach(e -> {
                if (e.getCategory() != MobCategory.MISC)
                    effectConfiguration.put(e.getRegistryName(), DEFAULT);
            });

            // Apply configuration.  These will replace defaults as needed.
            final Collection<IResourceAccessor> configs = ResourceUtils.findConfigs(DynamicSurroundings.MOD_ID, DynamicSurroundings.DATA_PATH, "mobeffects.json");

            IResourceAccessor.process(configs, accessor -> {
                Map<String, EntityConfig> cfg = accessor.as(entityConfigType);
                for (final Map.Entry<String, EntityConfig> kvp : cfg.entrySet()) {
                    final EntityEffectInfo eei = new EntityEffectInfo(kvp.getValue());
                    final ResourceLocation loc = Library.resolveResource(MobEffects.MOD_ID, kvp.getKey());
                    effectConfiguration.put(loc, eei);
                    if (loc.equals(PLAYER)) {
                        playerEffects = eei;
                    }

                    for (final String r : kvp.getValue().blockedSounds) {
                        try {
                            blockedSounds.add(new ResourceLocation(r));
                        } catch (@Nonnull final Throwable t) {
                            MobEffects.LOGGER.error(t, "Not a valid sound resource location: %s", r);
                        }
                    }
                }
            });

            // Replace our bow loose sounds
            final ResourceLocation bowLoose = new ResourceLocation(MobEffects.MOD_ID, "bow.loose");
            Library.getSound(bowLoose).ifPresent(se -> {
                soundReplace.put(new ResourceLocation("minecraft:entity.arrow.shoot"), se);
                soundReplace.put(new ResourceLocation("minecraft:entity.skeleton.shoot"), se);
            });
        }

        @Override
        public void log() {
            if (Config.CLIENT.logging.enableLogging.get()) {
                MobEffects.LOGGER.debug("MobEffect Registry");
                MobEffects.LOGGER.debug("==================");
                for (final Object2ObjectMap.Entry<ResourceLocation, EntityEffectInfo> kvp : effectConfiguration.object2ObjectEntrySet()) {
                    MobEffects.LOGGER.debug("%s: %s", kvp.getKey(), kvp.getValue());
                }
            }
        }

        @Override
        public void stop() {
            effectConfiguration.clear();
            effects.clear();
            blockedSounds.clear();
            soundReplace.clear();
        }
    }
}
