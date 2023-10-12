/*
 *  Dynamic Surroundings
 *  Copyright (C) 2020  OreCruncher
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

package org.orecruncher.environs.handlers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.orecruncher.environs.config.Config;
import org.orecruncher.environs.fog.*;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.events.DiagnosticEvent;
import org.orecruncher.lib.math.LoggingTimerEMA;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class FogHandler extends HandlerBase {

    protected final LoggingTimerEMA render = new LoggingTimerEMA("Render Fog");

    protected HolisticFogRangeCalculator fogRange = new HolisticFogRangeCalculator();

    public FogHandler() {
        super("Fog Handler");
    }

    public static boolean doFog() {
        return Config.CLIENT.fog.enableFog.get() && CommonState.getDimensionInfo().hasFog();
    }

    @Override
    public void process(@Nonnull final Player player) {
        if (doFog()) {
            this.fogRange.tick();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void fogRenderEvent(final ViewportEvent.RenderFog event) {
        //todo: figure out if this is right
        if (event.getMode() == FogRenderer.FogMode.FOG_TERRAIN && doFog()) {
            final ProfilerFiller profiler = GameUtils.getMC().getProfiler();
            profiler.push("Environs Fog Render");
            this.render.begin();
            final FogType fluidState = event.getCamera().getFluidInCamera();
            if (fluidState == FogType.NONE) {
                final FogResult result = this.fogRange.calculate(event);
                RenderSystem.setShaderFogStart(result.getStart());
                RenderSystem.setShaderFogEnd(result.getEnd());
            }
            this.render.end();
            profiler.pop();
        }
    }

    @SubscribeEvent
    public void diagnostics(final DiagnosticEvent event) {
        if (Config.CLIENT.logging.enableLogging.get()) {
            if (doFog()) {
                event.getLeft().add("Fog Range: " + this.fogRange.toString());
                event.addRenderTimer(this.render);
            } else
                event.getLeft().add("FOG: IGNORED");
        }
    }

    @Override
    public void onConnect() {
        this.fogRange = new HolisticFogRangeCalculator();
        this.fogRange.add(new BiomeFogRangeCalculator());
        this.fogRange.add(new HazeFogRangeCalculator());
        this.fogRange.add(new MorningFogRangeCalculator());
        this.fogRange.add(new BedrockFogRangeCalculator());
        this.fogRange.add(new WeatherFogRangeCalculator());

//			this.fogRange
//					.add(new FixedFogRangeCalculator(this.theme.getMinFogDistance(), this.theme.getMaxFogDistance()));

    }

}
