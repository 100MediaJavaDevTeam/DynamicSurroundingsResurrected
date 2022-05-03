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

package org.orecruncher.lib.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.fml.ModLoadingContext;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;

@OnlyIn(Dist.CLIENT)
public class ConfigGui {

    public static void registerConfigGui(@Nonnull final BiFunction<Minecraft, Screen, Screen> factory) {
        final ModLoadingContext context = ModLoadingContext.get();
        context.registerExtensionPoint(
                ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiHandler.ConfigGuiFactory(factory));
    }

    public static class InstallClothGuiFactory implements BiFunction<Minecraft, Screen, Screen> {

        // Resources for displaying info about getting ClothAPI
        private static final Component title = new TranslatableComponent("dsurround.dialog.missingcloth.title");
        private static final Component description = new TranslatableComponent("dsurround.dialog.missingcloth.description");

        @Override
        public Screen apply(@Nonnull final Minecraft minecraft, @Nonnull final Screen screen) {
            return new InstallClothGui(screen, title, description);
        }
    }

    // Swipe the disconnected from server dialog.  All this to replace the button resource...maybe I will fancy it
    // up with dancing creepers or something.
    private static class InstallClothGui extends Screen {
        private final Component iTextComponent;
        private MultiLineLabel message = MultiLineLabel.EMPTY;
        private final Screen nextScreen;
        private int textHeight;

        public InstallClothGui(Screen p_i242056_1_, Component p_i242056_2_, Component p_i242056_3_) {
            super(p_i242056_2_);
            this.nextScreen = p_i242056_1_;
            this.iTextComponent = p_i242056_3_;
        }

        public boolean shouldCloseOnEsc() {
            return false;
        }

        protected void init() {
            this.message = MultiLineLabel.create(this.font, this.iTextComponent, this.width - 50);
            this.textHeight = this.message.getLineCount() * 9;
            this.addRenderableWidget(new Button(this.width / 2 - 100, Math.min(this.height / 2 + this.textHeight / 2 + 9, this.height - 30), 200, 20, CommonComponents.GUI_DONE, (p_213033_1_) -> {
                this.minecraft.setScreen(this.nextScreen);
            }));
        }

        public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            this.renderBackground(matrixStack);
            drawCenteredString(matrixStack, this.font, this.title, this.width / 2, this.height / 2 - this.textHeight / 2 - 9 * 2, 11184810);
            this.message.renderCentered(matrixStack, this.width / 2, this.height / 2 - this.textHeight / 2);
            super.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }
}
