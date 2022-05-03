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

package org.orecruncher.lib.fml;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.VersionChecker.CheckResult;
import net.minecraftforge.fml.VersionChecker.Status;
import net.minecraftforge.forgespi.language.IModInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Simple update checker that can be registered with the ClientLoginChecks manager to provide feedback to the player
 * in the chat window if a newer version of a mod is available.
 */
@OnlyIn(Dist.CLIENT)
public class UpdateChecker extends Checker {

    protected final String messageId;

    public UpdateChecker(@Nonnull final String modId) {
        this(modId, modId + ".msg.NewVersion");
    }

    public UpdateChecker(@Nonnull final String id, @Nonnull final String messageId) {
        super(id);
        this.messageId = messageId;
    }

    @Nullable
    private String getUpdateMessage(@Nonnull final String modId) {
        final Optional<IModInfo> mod = ForgeUtils.getModInfo(modId);
        if (!mod.isPresent())
            return null;
        final CheckResult result = VersionChecker.getResult(mod.get());
        if (result.status() != Status.OUTDATED)
            return null;
        final String t = result.target() != null ? result.target().toString() : "UNKNOWN";
        final String u = result.url() != null ? result.url() : "UNKNOWN";
        return I18n.get(this.messageId, mod.get().getDisplayName(), t, u);
    }

    @Nullable
    @Override
    public Component onClientLogin(@Nonnull final LocalPlayer player) {
        final String updateMessage = getUpdateMessage(this.modId);
        if (updateMessage != null) {
            try {
                return Component.Serializer.fromJson(updateMessage);
            } catch (@Nonnull final Throwable t) {
                t.printStackTrace();
            }
        }
        return null;
    }
}
