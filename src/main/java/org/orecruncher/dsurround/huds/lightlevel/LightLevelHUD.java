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

package org.orecruncher.dsurround.huds.lightlevel;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;
import org.orecruncher.dsurround.DynamicSurroundings;
import org.orecruncher.dsurround.config.Config;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.TickCounter;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.gui.Color;
import org.orecruncher.lib.gui.ColorPalette;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.lib.particles.FrustumHelper;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = DynamicSurroundings.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LightLevelHUD {

    private static Font font;

    public enum Mode {
        BLOCK,
        SKY,
        BLOCK_SKY
    }

    public enum ColorSet {

        BRIGHT(ColorPalette.MC_GREEN, ColorPalette.MC_YELLOW, ColorPalette.MC_RED, ColorPalette.MC_DARKAQUA),
        DARK(ColorPalette.MC_DARKGREEN, ColorPalette.MC_GOLD, ColorPalette.MC_DARKRED, ColorPalette.MC_DARKBLUE);

        public final Color safe;
        public final Color caution;
        public final Color hazard;
        public final Color noSpawn;

        ColorSet(@Nonnull final Color safe, @Nonnull final Color caution, @Nonnull final Color hazard,
                         @Nonnull final Color noSpawn) {
            this.safe = safe;
            this.caution = caution;
            this.hazard = hazard;
            this.noSpawn = noSpawn;
        }
    }

    private static final class LightCoord {
        public int x;
        public double y;
        public int z;
        public int lightLevel;
        public int color;
    }

    private static boolean showHUD = false;

    private static final int ALLOCATION_SIZE = 2048;
    private static final ObjectArray<LightCoord> lightLevels = new ObjectArray<>(ALLOCATION_SIZE);
    private static final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
    private static int nextCoord = 0;

    private static final String[] lightLevelText = new String[16];
    private static final int[] margins = new int[16];

    static {
        for (int i = 0; i < ALLOCATION_SIZE; i++)
            lightLevels.add(new LightCoord());

        for (int i = 0; i < 16; i++)
            lightLevelText[i] = String.valueOf(i);
    }

    private static LightCoord nextCoord() {
        if (nextCoord == lightLevels.size())
            lightLevels.add(new LightCoord());
        return lightLevels.get(nextCoord++);
    }

    protected static boolean inFrustum(final double x, final double y, final double z) {
        return FrustumHelper.isLocationInFrustum(new Vec3(x, y, z));
    }

    protected static boolean renderLightLevel(@Nonnull final BlockState state, @Nonnull final BlockState below) {
        final Material stateMaterial = state.getMaterial();
        final Material belowMaterial = below.getMaterial();
        return !stateMaterial.isSolid() && !stateMaterial.isLiquid() && belowMaterial.isSolid();
    }

    protected static float heightAdjustment(@Nonnull final BlockState state, @Nonnull final BlockState below,
                                            @Nonnull final BlockPos pos) {
        if (state.getMaterial() == Material.AIR) {
            final VoxelShape shape = below.getCollisionShape(GameUtils.getWorld(), pos.below());
            return shape.isEmpty() ? 0 : (float) shape.max(Direction.Axis.Y) - 1;
        }

        final VoxelShape shape = below.getCollisionShape(GameUtils.getWorld(), pos);
        if (shape.isEmpty())
            return 0F;
        final float adjust = (float) (shape.max(Direction.Axis.Y));
        return state.getBlock() == Blocks.SNOW ? adjust + 0.125F : adjust;
    }

    protected static void updateLightInfo(@Nonnull final Vec3 position) {

        final Font fr = GameUtils.getMC().font;

        if (fr != font) {
            font = fr;
            for (int i = 0; i < 16; i++)
                margins[i] = -(font.width(lightLevelText[i]) + 1) / 2;
        }

        nextCoord = 0;

        final ColorSet colors = Config.CLIENT.lightLevel.colorSet.get();
        final Mode displayMode = Config.CLIENT.lightLevel.mode.get();
        final int skyLightSub = GameUtils.getWorld().getSkyDarken();
        final int rangeXZ = Config.CLIENT.lightLevel.range.get() * 2 + 1;
        final int rangeY = Config.CLIENT.lightLevel.range.get() + 1;
        final int originX = MathStuff.floor(position.x) - (rangeXZ / 2);
        final int originZ = MathStuff.floor(position.z) - (rangeXZ / 2);
        final int originY = MathStuff.floor(position.y) - (rangeY - 3);

        final ClientLevel world = GameUtils.getWorld();

        for (int dX = 0; dX < rangeXZ; dX++)
            for (int dZ = 0; dZ < rangeXZ; dZ++) {

                final int trueX = originX + dX;
                final int trueZ = originZ + dZ;

                BlockState lastState = null;

                for (int dY = 0; dY < rangeY; dY++) {

                    final int trueY = originY + dY;

                    if (trueY < 1 || !inFrustum(trueX, trueY, trueZ))
                        continue;

                    final BlockPos pos = new BlockPos(trueX, trueY, trueZ);
                    final BlockState state = world.getBlockState(pos);

                    if (lastState == null)
                        lastState = world.getBlockState(pos.below());

                    if (renderLightLevel(state, lastState)) {
                        mutable.set(trueX, trueY, trueZ);

                        final boolean mobSpawn = lastState.isValidSpawn(
                                GameUtils.getWorld(),
                                mutable,
                                SpawnPlacements.Type.ON_GROUND,
                                EntityType.ZOMBIE);

                        if (mobSpawn || !Config.CLIENT.lightLevel.hideSafe.get()) {
                            final int blockLight = world.getBrightness(LightLayer.BLOCK, mutable);
                            final int skyLight = world.getBrightness(LightLayer.SKY, mutable) - skyLightSub;
                            final int effective = Math.max(blockLight, skyLight);

                            final int result;
                            if (displayMode == Mode.BLOCK_SKY) {
                                result = effective;
                            } else if (displayMode == Mode.BLOCK)  {
                                result = blockLight;
                            } else {
                                result = skyLight;
                            }

                            Color color = colors.safe;
                            if (!mobSpawn) {
                                color = colors.noSpawn;
                            } else if (blockLight <= Config.CLIENT.lightLevel.lightSpawnThreshold.get()) {
                                if (effective > Config.CLIENT.lightLevel.lightSpawnThreshold.get())
                                    color = colors.caution;
                                else
                                    color = colors.hazard;
                            }

                            if (!(color == colors.safe && Config.CLIENT.lightLevel.hideSafe.get())) {
                                final LightCoord coord = nextCoord();
                                coord.x = trueX;
                                coord.y = trueY + heightAdjustment(state, lastState, mutable) + 0.002D;
                                coord.z = trueZ;
                                coord.lightLevel = result;
                                coord.color = color.rgbWithAlpha(254);
                            }
                        }
                    }

                    lastState = state;
                }
            }
    }

    public static void toggleDisplay() {
        showHUD = !showHUD;
        DynamicSurroundings.LOGGER.info("Light Level HUD: %s", Boolean.toString(showHUD));
    }

    @SubscribeEvent
    public static void doTick(@Nonnull final TickEvent.PlayerTickEvent event) {

        if (!showHUD || event.side == LogicalSide.SERVER || event.phase == TickEvent.Phase.END || GameUtils.getMC().isPaused())
            return;

        if (event.player == null || event.player.level == null)
            return;

        if (TickCounter.getTickCount() % 4 != 0)
            return;

        updateLightInfo(event.player.position());
    }

    public static void render(@Nonnull final PoseStack matrixStack, final float partialTicks) {
        if (!showHUD || nextCoord == 0)
            return;

        final Player player = GameUtils.getPlayer();
        if (player == null)
            return;

        drawStringRender(matrixStack, player);
    }

    private static void drawStringRender(@Nonnull final PoseStack matrixStack, @Nonnull final Player player) {

        final boolean thirdPerson = GameUtils.isThirdPersonView();
        Direction playerFacing = player.getDirection();
        if (thirdPerson)
            playerFacing = playerFacing.getOpposite();
        if (playerFacing == Direction.SOUTH || playerFacing == Direction.NORTH)
            playerFacing = playerFacing.getOpposite();
        final float rotationAngle = playerFacing.getOpposite().toYRot();

        final Quaternion rotY = Vector3f.YP.rotationDegrees(rotationAngle);
        final Quaternion rotX = Vector3f.XP.rotationDegrees(90);
        final Vec3 view = GameUtils.getMC().gameRenderer.getMainCamera().getPosition();
        matrixStack.pushPose();
        matrixStack.translate(-view.x(), -view.y(), -view.z());

//        RenderSystem.disableLighting();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.depthMask(true);

        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        final int yAdjust = -(font.lineHeight / 2);

        for (int i = 0; i < nextCoord; i++) {
            final LightCoord coord = lightLevels.get(i);
            final String text = lightLevelText[coord.lightLevel];
            final int margin = margins[coord.lightLevel];
            final float scale = 0.07F;

            matrixStack.pushPose();
            matrixStack.translate(coord.x + 0.5D, coord.y, coord.z + 0.5D);
            matrixStack.mulPose(rotY);
            matrixStack.mulPose(rotX);
            matrixStack.scale(-scale, -scale, scale);

            font.drawInBatch(
                    text,
                    margin,
                    yAdjust,
                    coord.color,
                    false,
                    matrixStack.last().pose(),
                    buffer,
                    false,
                    0,
                    15728880);

            matrixStack.popPose();
        }

        buffer.endBatch();
        matrixStack.popPose();
    }
}
