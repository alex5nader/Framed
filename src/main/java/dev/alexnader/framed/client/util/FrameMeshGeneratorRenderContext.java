package dev.alexnader.framed.client.util;

import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.render.model.BakedModel;

import java.util.function.Consumer;

public class FrameMeshGeneratorRenderContext implements RenderContext {
    private final Consumer<Mesh> meshConsumer;
    private final Consumer<BakedModel> fallbackConsumer;

    public FrameMeshGeneratorRenderContext(Consumer<Mesh> meshConsumer, Consumer<BakedModel> fallbackConsumer) {
        this.meshConsumer = meshConsumer;
        this.fallbackConsumer = fallbackConsumer;
    }

    @Override
    public Consumer<Mesh> meshConsumer() {
        return meshConsumer;
    }

    @Override
    public Consumer<BakedModel> fallbackConsumer() {
        return fallbackConsumer;
    }

    @Override
    public QuadEmitter getEmitter() {
        throw new IllegalStateException("TODO");
    }

    @Override
    public void pushTransform(QuadTransform transform) {
        throw new IllegalStateException("TODO");
    }

    @Override
    public void popTransform() {
        throw new IllegalStateException("TODO");
    }
}
