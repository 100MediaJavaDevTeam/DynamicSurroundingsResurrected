/*
 * Dynamic Surroundings]
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

package org.orecruncher.lib;

import net.minecraft.ChatFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.dsurround.DynamicSurroundings;
import org.orecruncher.lib.events.DiagnosticEvent;
import org.orecruncher.lib.math.LoggingTimerEMA;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.lib.math.TimerEMA;

import javax.annotation.Nonnull;

/**
 * Diagnostic helper that fires an event every tick to collect data that will be rendered on the diagnostics
 * screen (F3).
 */
@Mod.EventBusSubscriber(modid = DynamicSurroundings.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class Diagnostics {

    private static final TimerEMA clientTick = new TimerEMA("Client Tick");
    private static final TimerEMA lastTick = new TimerEMA("Last Tick");
    private static final LoggingTimerEMA diagnostics = new LoggingTimerEMA("Diagnostics");
    private static long lastTickMark = -1;
    private static long timeMark = 0;
    private static float tps = 0;

    private static DiagnosticEvent lastEvent = new DiagnosticEvent();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void startTick(@Nonnull final TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            timeMark = System.nanoTime();
            if (lastTickMark != -1) {
                lastTick.update(timeMark - lastTickMark);
                tps = MathStuff.clamp((float) (50F / lastTick.getMSecs() * 20F), 0F, 20F);
            }
            lastTickMark = timeMark;
        }

    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void endTick(@Nonnull final TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            final long delta = System.nanoTime() - timeMark;
            clientTick.update(delta);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onClientTick(@Nonnull final TickEvent.ClientTickEvent event) {
        if (GameUtils.displayDebug()) {
            diagnostics.begin();

            final DiagnosticEvent evt = new DiagnosticEvent();

            evt.getRight().add("");
            evt.getRight().add(ChatFormatting.LIGHT_PURPLE + clientTick.toString());
            evt.getRight().add(ChatFormatting.LIGHT_PURPLE + lastTick.toString());
            evt.getRight().add(ChatFormatting.LIGHT_PURPLE + diagnostics.toString());
            evt.getRight().add(ChatFormatting.LIGHT_PURPLE + String.format("TPS:%7.3fms", tps));

            MinecraftForge.EVENT_BUS.post(evt);
            lastEvent = evt;

            if (!lastEvent.getTimers().isEmpty()) {
                for (final TimerEMA timer : lastEvent.getTimers())
                    evt.getRight().add(ChatFormatting.GREEN + timer.toString());
            }

            if (!lastEvent.getRenderTimers().isEmpty()) {
                for (final TimerEMA timer : lastEvent.getRenderTimers())
                    evt.getRight().add(ChatFormatting.AQUA + timer.toString());
            }

            diagnostics.end();
        }
    }

    @SubscribeEvent
    public static void onGatherText(@Nonnull final RenderGameOverlayEvent.Text event) {
        if (GameUtils.displayDebug()) {
            if (!lastEvent.getLeft().isEmpty())
                event.getLeft().addAll(lastEvent.getLeft());

            if (!lastEvent.getRight().isEmpty())
                event.getRight().addAll(lastEvent.getRight());
        }
    }
}
