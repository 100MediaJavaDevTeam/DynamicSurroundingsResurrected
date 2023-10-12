package org.orecruncher.environs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundInit {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Environs.MOD_ID);

    public static final RegistryObject<SoundEvent> COYOTE = SOUNDS.register("coyote", ()->new SoundEvent(new ResourceLocation(Environs.MOD_ID, "coyote")));
    public static final RegistryObject<SoundEvent> CROW = SOUNDS.register("crow", ()->new SoundEvent(new ResourceLocation(Environs.MOD_ID, "crow")));
    public static final RegistryObject<SoundEvent> OWL = SOUNDS.register("owl", ()->new SoundEvent(new ResourceLocation(Environs.MOD_ID, "owl")));
    public static final RegistryObject<SoundEvent> RAPTOR = SOUNDS.register("raptor", ()->new SoundEvent(new ResourceLocation(Environs.MOD_ID, "raptor")));
    public static final RegistryObject<SoundEvent> BIGROCKFALL = SOUNDS.register("bigrockfall", ()->new SoundEvent(new ResourceLocation(Environs.MOD_ID, "bigrockfall")));
    public static final RegistryObject<SoundEvent> ROCKFALL = SOUNDS.register("rockfall", ()->new SoundEvent(new ResourceLocation(Environs.MOD_ID, "rockfall")));
    public static final RegistryObject<SoundEvent> WHALE = SOUNDS.register("whale", ()->new SoundEvent(new ResourceLocation(Environs.MOD_ID, "whale")));
    public static final RegistryObject<SoundEvent> ICE = SOUNDS.register("ice", ()->new SoundEvent(new ResourceLocation(Environs.MOD_ID, "ice")));
    public static final RegistryObject<SoundEvent> MONSTERGROWL = SOUNDS.register("monstergrowl", ()->new SoundEvent(new ResourceLocation(Environs.MOD_ID, "monstergrowl")));
    public static final RegistryObject<SoundEvent> WIND= SOUNDS.register("wind", () -> new SoundEvent(new ResourceLocation("environs:wind")));
    public static final RegistryObject<SoundEvent> DESERT_WIND= SOUNDS.register("desert.wind", () -> new SoundEvent(new ResourceLocation("environs:desert.wind")));
    public static final RegistryObject<SoundEvent> ARCTIC_WIND= SOUNDS.register("arctic.wind", () -> new SoundEvent(new ResourceLocation("environs:arctic.wind")));
    public static final RegistryObject<SoundEvent> OCEAN= SOUNDS.register("ocean", () -> new SoundEvent(new ResourceLocation("environs:ocean")));
    public static final RegistryObject<SoundEvent> JUNGLE= SOUNDS.register("jungle", () -> new SoundEvent(new ResourceLocation("environs:jungle")));
    public static final RegistryObject<SoundEvent> SAVANNA_DAY= SOUNDS.register("savanna.day", () -> new SoundEvent(new ResourceLocation("environs:savanna.day")));
    public static final RegistryObject<SoundEvent> SAVANNA_NIGHT= SOUNDS.register("savanna.night", () -> new SoundEvent(new ResourceLocation("environs:savanna.night")));
    public static final RegistryObject<SoundEvent> PLAINS= SOUNDS.register("plains", () -> new SoundEvent(new ResourceLocation("environs:plains")));
    public static final RegistryObject<SoundEvent> CRICKETS= SOUNDS.register("crickets", () -> new SoundEvent(new ResourceLocation("environs:crickets")));
    public static final RegistryObject<SoundEvent> FOREST= SOUNDS.register("forest", () -> new SoundEvent(new ResourceLocation("environs:forest")));
    public static final RegistryObject<SoundEvent> DEEP_FOREST= SOUNDS.register("deep_forest", () -> new SoundEvent(new ResourceLocation("environs:deep_forest")));
    public static final RegistryObject<SoundEvent> DEEP_CRICKETS= SOUNDS.register("deep_crickets", () -> new SoundEvent(new ResourceLocation("environs:deep_crickets")));
    public static final RegistryObject<SoundEvent> BEACH= SOUNDS.register("beach", () -> new SoundEvent(new ResourceLocation("environs:beach")));
    public static final RegistryObject<SoundEvent> RIVER= SOUNDS.register("river", () -> new SoundEvent(new ResourceLocation("environs:river")));
    public static final RegistryObject<SoundEvent> BOG= SOUNDS.register("bog", () -> new SoundEvent(new ResourceLocation("environs:bog")));
    public static final RegistryObject<SoundEvent> UNDERGROUND= SOUNDS.register("underground", () -> new SoundEvent(new ResourceLocation("environs:underground")));
    public static final RegistryObject<SoundEvent> UNDEROCEAN= SOUNDS.register("underocean", () -> new SoundEvent(new ResourceLocation("environs:underocean")));
    public static final RegistryObject<SoundEvent> UNDERDEEPOCEAN= SOUNDS.register("underdeepocean", () -> new SoundEvent(new ResourceLocation("environs:underdeepocean")));
    public static final RegistryObject<SoundEvent> UNDERRIVER= SOUNDS.register("underriver", () -> new SoundEvent(new ResourceLocation("environs:underriver")));
    public static final RegistryObject<SoundEvent> UNDERWATER= SOUNDS.register("underwater", () -> new SoundEvent(new ResourceLocation("environs:underwater")));
    public static final RegistryObject<SoundEvent> VILLAGE_ROOSTER= SOUNDS.register("village.rooster", () -> new SoundEvent(new ResourceLocation("environs:village.rooster")));
    public static final RegistryObject<SoundEvent> VILLAGE_ANVIL= SOUNDS.register("village.anvil", () -> new SoundEvent(new ResourceLocation("environs:village.anvil")));
    public static final RegistryObject<SoundEvent> VILLAGE_DOGBARK= SOUNDS.register("village.dogbark", () -> new SoundEvent(new ResourceLocation("environs:village.dogbark")));
    public static final RegistryObject<SoundEvent> THEEND= SOUNDS.register("theend", () -> new SoundEvent(new ResourceLocation("environs:theend")));
    public static final RegistryObject<SoundEvent> DIMENSION_NETHER= SOUNDS.register("dimension.nether", () -> new SoundEvent(new ResourceLocation("environs:dimension.nether")));
    public static final RegistryObject<SoundEvent> FROG= SOUNDS.register("frog", () -> new SoundEvent(new ResourceLocation("environs:frog")));
    public static final RegistryObject<SoundEvent> BULLFROG= SOUNDS.register("bullfrog", () -> new SoundEvent(new ResourceLocation("environs:bullfrog")));
    public static final RegistryObject<SoundEvent> BREATHING= SOUNDS.register("breathing", () -> new SoundEvent(new ResourceLocation("environs:breathing")));
    public static final RegistryObject<SoundEvent> SOULSAND= SOUNDS.register("soulsand", () -> new SoundEvent(new ResourceLocation("environs:soulsand")));
    public static final RegistryObject<SoundEvent> WATERFALL_0= SOUNDS.register("waterfall.0", () -> new SoundEvent(new ResourceLocation("environs:waterfall.0")));
    public static final RegistryObject<SoundEvent> WATERFALL_1= SOUNDS.register("waterfall.1", () -> new SoundEvent(new ResourceLocation("environs:waterfall.1")));
    public static final RegistryObject<SoundEvent> WATERFALL_2= SOUNDS.register("waterfall.2", () -> new SoundEvent(new ResourceLocation("environs:waterfall.2")));
    public static final RegistryObject<SoundEvent> WATERFALL_3= SOUNDS.register("waterfall.3", () -> new SoundEvent(new ResourceLocation("environs:waterfall.3")));
    public static final RegistryObject<SoundEvent> WATERFALL_4= SOUNDS.register("waterfall.4", () -> new SoundEvent(new ResourceLocation("environs:waterfall.4")));
    public static final RegistryObject<SoundEvent> WATERFALL_5= SOUNDS.register("waterfall.5", () -> new SoundEvent(new ResourceLocation("environs:waterfall.5")));
    public static final RegistryObject<SoundEvent> JUMP= SOUNDS.register("jump", () -> new SoundEvent(new ResourceLocation("environs:jump")));

}
