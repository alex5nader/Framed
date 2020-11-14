package dev.alexnader.framity2.client;

import com.mojang.datafixers.util.Pair;
import dev.alexnader.framity2.mixin.mc.BakedQuadAccess;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;

import java.util.List;
import java.util.Random;

public class SpritesSource {
    @SuppressWarnings("unchecked")
    private final List<BakedQuad>[] quads = new List[6];

    public SpritesSource(BlockState state, BakedModel model, Random r) {
        for (int i = 0; i <= 6; i++) {
            quads[i] = model.getQuads(state, ModelHelper.faceFromIndex(i), r);
        }
    }

    public Pair<Sprite, Integer> getSpriteAndColor(Direction dir, int index, int color) {
        BakedQuad quad = quads[ModelHelper.toFaceIndex(dir)].get(index);
        return Pair.of(
            ((BakedQuadAccess) quad).sprite(),
            quad.hasColor() ? color : null
        );
    }

    public int getCount(Direction dir) {
        return quads[ModelHelper.toFaceIndex(dir)].size();
    }
}
