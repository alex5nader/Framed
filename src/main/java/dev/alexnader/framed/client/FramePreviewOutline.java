/*
 * MIT License
 *
 * Copyright (c) 2020 Hephaestus-Dev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

// This file is adapted from Hephaestus-Dev's Automotion, commit f3f5e3d.
// Permalinks:
// - https://github.com/Hephaestus-Dev/Automotion/blob/f3f5e3d1251a925efa2c3f32e67f7eaa0b732692/src/main/java/hephaestus/dev/automotion/common/item/GhostBlockItem.java
// - https://github.com/Hephaestus-Dev/Automotion/blob/f3f5e3d1251a925efa2c3f32e67f7eaa0b732692/src/main/java/hephaestus/dev/automotion/client/AutomotionRenderLayers.java
// - https://github.com/Hephaestus-Dev/Automotion/blob/f3f5e3d1251a925efa2c3f32e67f7eaa0b732692/src/main/java/hephaestus/dev/automotion/client/model/AutomotionModel.java

package dev.alexnader.framed.client;

import dev.alexnader.framed.block.frame.Frame;
import dev.alexnader.framed.mixin.mc.BlockItemAccess;
import dev.alexnader.framed.mixin.mc.WorldRendererAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.util.math.Vector4f;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static dev.alexnader.framed.Framed.BLOCKS;

@Environment(EnvType.CLIENT)
public class FramePreviewOutline extends RenderLayer {
    @SuppressWarnings("unused") // required by javac, unused bc of mixin
    public FramePreviewOutline(final String name, final VertexFormat vertexFormat, final int drawMode, final int expectedBufferSize, final boolean hasCrumbling, final boolean translucent, final Runnable startAction, final Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
        throw new IllegalStateException("Should not instantiate utility class");
    }

    @SuppressWarnings("java:S3776")
    public static void renderPreviewOutline(final WorldRenderContext context) {
        final MinecraftClient client = MinecraftClient.getInstance();

        final ClientPlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        final ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
        if (!(stack.getItem() instanceof BlockItem)) {
            return;
        }

        final Block block = ((BlockItem) stack.getItem()).getBlock();
        if (!(block instanceof Frame)) {
            return;
        }

        if (!(client.crosshairTarget instanceof BlockHitResult) || client.crosshairTarget.getType() == HitResult.Type.MISS || client.world == null) {
            return;
        }

        final BlockHitResult hitResult = (BlockHitResult) client.crosshairTarget;
        final ItemPlacementContext placementContext = new ItemPlacementContext(player, Hand.MAIN_HAND, stack, hitResult);
        final BlockState blockState = ((BlockItemAccess) player.getStackInHand(Hand.MAIN_HAND).getItem()).getPlacementStateProxy(placementContext);

        if (blockState == null) {
            return;
        }

        final BlockPos pos = placementContext.getBlockPos();
        final BakedModel model = client.getBlockRenderManager().getModel(blockState);
        final boolean valid;

        if (block == BLOCKS.DOOR_FRAME) {
            final BlockPos upPos = pos.up();
            final BlockState upState = blockState.with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
            final BakedModel upModel = client.getBlockRenderManager().getModel(upState);

            final boolean upValid = new ItemPlacementContext(player, Hand.MAIN_HAND, stack, hitResult.withBlockPos(upPos)).canReplaceExisting() || client.world.getBlockState(upPos).isAir();
            final boolean downValid = blockState.canPlaceAt(client.world, pos) && (placementContext.canPlace() || client.world.getBlockState(pos).isAir());
            if (!upValid) {
                valid = false;
            } else {
                valid = downValid;
            }

            renderPreview(context, client.world, upState, upPos, upModel, upValid && downValid);
        } else {
            valid = blockState.canPlaceAt(client.world, pos) && (placementContext.canPlace() || client.world.getBlockState(pos).isAir());
        }

        renderPreview(context, client.world, blockState, pos, model, valid);
    }

    private static void renderPreview(final WorldRenderContext context, final ClientWorld world, final BlockState blockState, final BlockPos pos, final BakedModel model, final boolean valid) {
        final MatrixStack matrixStack = context.matrixStack();
        final Camera camera = context.camera();
        final int ticks = ((WorldRendererAccess) context.worldRenderer()).ticks();
        final float tickDelta = context.tickDelta();

        for (int directionId  = 0; directionId <= 6; directionId++) {
            final List<BakedQuad> quads = model.getQuads(blockState, ModelHelper.faceFromIndex(directionId), world.random);

            matrixStack.push();
            matrixStack.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
            matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());

            if (blockState.contains(Properties.HORIZONTAL_FACING)) {
                matrixStack.translate(0.5, 0.5, 0.5);

                matrixStack.translate(-0.5, -0.5, -0.5);
            }

            final float scale = 1F - (1F / 60) + ((float) Math.sin((ticks + tickDelta) / 4.5F) / 60);
            matrixStack.translate(-0.5 * scale, -0.5 * scale, -0.5 * scale);
            matrixStack.scale(scale, scale, scale);
            matrixStack.translate(0.5 / scale, 0.5 / scale, 0.5 / scale);

            final int c = 0;
            final int r = valid ? c : 255;
            final int g = valid ? 255 : c;
            final int b = valid ? 64 : 0;
            final int a = 127;

            final MatrixStack.Entry entry = matrixStack.peek();

            //noinspection ConstantConditions // this method should be registered to AFTER_ENTITIES, which will have a non-null consumer
            final VertexConsumer faces = context.consumers().getBuffer(TexturedRenderLayers.getItemEntityTranslucentCull());
            for (final BakedQuad quad : quads) {
                render(quad, entry, faces, r, g, b, a);
            }
            matrixStack.pop();
        }
    }

    @SuppressWarnings({"SameParameterValue", "java:S1181"})
    private static void render(final BakedQuad quad, final MatrixStack.Entry entry, final VertexConsumer consumer, final float r, final float g, final float b, final float a) {
        final int[] is = quad.getVertexData();
        final Vec3i vec3i = quad.getFace().getVector();
        final Vector3f vector3f = new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
        final Matrix4f matrix4f = entry.getModel();
        vector3f.transform(entry.getNormal());

        final int j = is.length / 8;
        final MemoryStack memoryStack = MemoryStack.stackPush();
        Throwable var17 = null;

        try {
            final ByteBuffer byteBuffer = memoryStack.malloc(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSize());
            final IntBuffer intBuffer = byteBuffer.asIntBuffer();

            for(int k = 0; k < j; ++k) {
                intBuffer.clear();
                intBuffer.put(is, k * 8, 8);
                final float x = byteBuffer.getFloat(0);
                final float y = byteBuffer.getFloat(4);
                final float z = byteBuffer.getFloat(8);
                final float v;
                final float w;

                final int u = 0x00F000F0;
                v = byteBuffer.getFloat(16);
                w = byteBuffer.getFloat(20);
                final Vector4f vector4f = new Vector4f(x, y, z, 1.0F);
                vector4f.transform(matrix4f);
                consumer.vertex(vector4f.getX(), vector4f.getY(), vector4f.getZ(), r/255F, g/255F, b/255F, a/255F, v, w, OverlayTexture.DEFAULT_UV, u, vector3f.getX(), vector3f.getY(), vector3f.getZ());
            }
        } catch (final Throwable var38) {
            var17 = var38;
            throw var38;
        } finally {
            if (var17 != null) {
                try {
                    memoryStack.close();
                } catch (final Throwable var37) {
                    var17.addSuppressed(var37);
                }
            } else {
                memoryStack.close();
            }
        }
    }
}
