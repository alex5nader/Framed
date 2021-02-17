package dev.alexnader.framed.client.model;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class FrameModelVariantProvider implements ModelVariantProvider {
    private static final FrameModel FRAME_MODEL = new FrameModel();

    private final Set<ModelIdentifier> registered = new ObjectOpenHashSet<>();

    public void useFrameModelFor(Block... blocks) {
        for (Block block : blocks) {
            for (BlockState state : block.getStateManager().getStates()) {
                registered.add(BlockModels.getModelId(state));
            }
        }
    }

    @Override
    public @Nullable UnbakedModel loadModelVariant(ModelIdentifier modelIdentifier, ModelProviderContext modelProviderContext) {
        if (registered.contains(modelIdentifier)) {
            return FRAME_MODEL;
        } else {
            return null;
        }
    }
}
