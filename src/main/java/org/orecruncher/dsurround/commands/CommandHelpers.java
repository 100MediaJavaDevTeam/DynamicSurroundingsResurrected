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

package org.orecruncher.dsurround.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.dsurround.DynamicSurroundings;
import org.orecruncher.dsurround.commands.dump.DumpCommand;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = DynamicSurroundings.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandHelpers {
    private CommandHelpers() {

    }

    @SubscribeEvent
    public static void registerCommands(@Nonnull final RegisterCommandsEvent event) {
        // Only register if its an integrated server environment
        if (event.getCommandSelection() == Commands.CommandSelection.INTEGRATED) {
            DumpCommand.register(event.getDispatcher());
        }
    }

    public static void scheduleOnClientThread(Runnable runnable) {
        final BlockableEventLoop<?> scheduler = LogicalSidedProvider.WORKQUEUE.get(LogicalSide.CLIENT);
        scheduler.submitAsync(runnable);
    }

    public static void sendSuccess(@Nonnull final CommandSourceStack source, @Nonnull final String command, @Nonnull String operation, @Nonnull String target) {
        final String key = String.format("command.dsurround.%s.success", command);
        source.sendSuccess(Component.translatable(key, operation, target), true);
    }

    public static void sendFailure(@Nonnull final CommandSourceStack source, @Nonnull final String command) {
        final String key = String.format("command.dsurround.%s.failure", command);
        source.sendSuccess(Component.translatable(key), true);
    }
}
