package dev.alexnader.framed.client.model;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class FrameModel implements UnbakedModel, BakedModel, FabricBakedModel {
    private Sprite sprite;

    @Override public boolean isVanillaAdapter() {
        return false;
    }

    @Override public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {

    }

    @Override public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        return Collections.emptyList();
    }

    @Override public boolean useAmbientOcclusion() {
        return false;
    }

    @Override public boolean hasDepth() {
        return false;
    }

    @Override public boolean isSideLit() {
        return false;
    }

    @Override public boolean isBuiltin() {
        return false;
    }

    @Override public Sprite getSprite() {
        return sprite;
    }

    @Override public ModelTransformation getTransformation() {
        return ModelTransformation.NONE;
    }

    @Override public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }

    @Override public Collection<Identifier> getModelDependencies() {
        return Collections.emptyList();
    }

    @Override public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return Collections.emptyList();
    }

    @Nullable @Override public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        //noinspection deprecation
        sprite = textureGetter.apply(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, MissingSprite.getMissingSpriteId()));
        return this;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        Mesh mesh = (Mesh) ((RenderAttachedBlockView)blockView).getBlockEntityRenderAttachment(pos);
        if (mesh != null) {
            context.meshConsumer().accept(mesh);
        } else {
            System.out.println("mesh machine 🅱roke");
        }
    }
}
