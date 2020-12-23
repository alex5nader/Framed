package dev.alexnader.framity.client.transform;

import dev.alexnader.framity.client.util.ToOptional;
import dev.alexnader.framity.mixin.mc.BakedQuadAccess;
import dev.alexnader.framity.util.Float4;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public abstract class BaseApplier implements ToOptional<BaseApplier> {
    public abstract boolean apply(MutableQuadView mqv, Direction dir, int index, Float4 us, Float4 vs, int color);

    public static final BaseApplier NONE = new BaseApplier() {
        @Override
        public boolean apply(final MutableQuadView mqv, final Direction dir, final int index, final Float4 us, final Float4 vs, final int color) {
            return false;
        }

        @Override
        public Optional<BaseApplier> toOptional() {
            return Optional.empty();
        }

        @Override
        public <T> T match(final Function<BaseApplier, T> some, final Supplier<T> none) {
            return none.get();
        }
    };

    public static class Some extends BaseApplier implements ToOptional.Some<BaseApplier> {
        private final Object2IntMap<Direction> sizes = new Object2IntOpenHashMap<>(7);
        private final Map<Direction, SpriteApplier[]> spriteAppliers = new HashMap<>(7);
        private final Map<Direction, MaterialApplier[]> materialAppliers = new HashMap<>(7);
        private final Map<Direction, LazyColorApplier[]> colorAppliers = new HashMap<>(7);

        public Some(final BlockState state, final BakedModel model, final Random r) {
            for (int i = 0; i <= 6; i++) {
                final Direction dir = ModelHelper.faceFromIndex(i);
                final List<BakedQuad> quads = model.getQuads(state, dir, r);
                final int size = quads.size();

                sizes.put(dir, size);
                final SpriteApplier[] spriteAppliers = this.spriteAppliers.computeIfAbsent(dir, x -> new SpriteApplier[size]);
                final MaterialApplier[] materialAppliers = this.materialAppliers.computeIfAbsent(dir, x -> new MaterialApplier[size]);
                final LazyColorApplier[] colorAppliers = this.colorAppliers.computeIfAbsent(dir, x -> new LazyColorApplier[size]);

                for (int j = 0; j < size; j++) {
                    final BakedQuad quad = quads.get(j);
                    final Sprite sprite = ((BakedQuadAccess) quad).sprite();

                    spriteAppliers[j] = new SpriteApplier.Some(sprite);
                    materialAppliers[j] = MaterialApplier.ofSpriteAndBlockState(sprite, state);

                    if (quad.hasColor()) {
                        colorAppliers[j] = new LazyColorApplier.Some();
                    } else {
                        colorAppliers[j] = LazyColorApplier.NONE;
                    }
                }
            }
        }

        @Override
        public boolean apply(final MutableQuadView mqv, final Direction dir, final int quadIndex, final Float4 us, final Float4 vs, final int color) {
            final int index = quadIndex % sizes.getInt(dir);
            materialAppliers.get(dir)[index].apply(mqv);
            colorAppliers.get(dir)[index].apply(mqv, color);
            return spriteAppliers.get(dir)[index].apply(mqv, us, vs);
        }
    }
}
