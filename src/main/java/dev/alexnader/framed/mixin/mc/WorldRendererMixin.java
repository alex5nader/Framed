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
// - https://github.com/Hephaestus-Dev/Automotion/blob/f3f5e3d1251a925efa2c3f32e67f7eaa0b732692/src/main/java/hephaestus/dev/automotion/mixin/client/render/WorldRendererMixin.java

package dev.alexnader.framed.mixin.mc;

import dev.alexnader.framed.client.util.WorldRendererCallback;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

//TODO Remove once FabricMC/fabric #1182 is merged
@Mixin(WorldRenderer.class)
@Environment(EnvType.CLIENT)
public class WorldRendererMixin {
    @Shadow
    private @Nullable Frustum capturedFrustum;
    
    @Shadow
    private @Final BufferBuilderStorage bufferBuilders;
    
    @Shadow
    private int ticks;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDD)V", ordinal = 2, shift= At.Shift.AFTER))
    private void worldRendererCallback(final MatrixStack matrices, final float tickDelta, final long limitTime, final boolean renderBlockOutline, final Camera camera, final GameRenderer gameRenderer, final LightmapTextureManager lightmapTextureManager, final Matrix4f matrix4f, final CallbackInfo ci) {
        WorldRendererCallback.EVENT.invoker().render(bufferBuilders, (WorldRenderer) (Object) this, matrices, this.ticks, tickDelta, camera, capturedFrustum);
    }
}
