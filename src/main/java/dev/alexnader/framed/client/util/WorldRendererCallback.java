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
// - https://github.com/Hephaestus-Dev/Automotion/blob/f3f5e3d1251a925efa2c3f32e67f7eaa0b732692/src/main/java/hephaestus/dev/automotion/client/WorldRendererCallback.java

package dev.alexnader.framed.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;

import javax.annotation.Nullable;

@Environment(EnvType.CLIENT)
public interface WorldRendererCallback {
    Event<WorldRendererCallback> EVENT = EventFactory.createArrayBacked(
        WorldRendererCallback.class,
        callbacks -> (bufferBuilders, renderer, matrixStack, ticks, tickDelta, camera, frustrum) -> {
            for (WorldRendererCallback callback : callbacks) {
                callback.render(bufferBuilders, renderer, matrixStack, ticks, tickDelta, camera, frustrum);
            }
        }
    );

    void render(BufferBuilderStorage bufferBuilders, WorldRenderer renderer, MatrixStack matrixStack, int ticks, float tickDelta, Camera camera, @Nullable Frustum frustum);
}
