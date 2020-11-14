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
import java.util.OptionalInt;
import java.util.Random;

public class SpritesSource {
    @SuppressWarnings("unchecked")
    private final List<BakedQuad>[] quads = new List[7];

    public SpritesSource(final BlockState state, final BakedModel model, final Random r) {
        for (int i = 0; i <= 6; i++) {
            quads[i] = model.getQuads(state, ModelHelper.faceFromIndex(i), r);
        }
    }

    public Pair<Sprite, OptionalInt> getSpriteAndColor(final Direction dir, final int index, final int color) {
        final BakedQuad quad = quads[ModelHelper.toFaceIndex(dir)].get(index);
        return Pair.of(
            ((BakedQuadAccess) quad).sprite(),
            quad.hasColor() ? OptionalInt.of(color) : OptionalInt.empty()
        );
    }

    public int getCount(final Direction dir) {
        return quads[ModelHelper.toFaceIndex(dir)].size();
    }
}
