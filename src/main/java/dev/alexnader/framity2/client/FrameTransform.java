package dev.alexnader.framity2.client;

import com.mojang.datafixers.util.Pair;
import dev.alexnader.framity2.block.FrameSlotInfo;
import dev.alexnader.framity2.block.frame.Frame;
import dev.alexnader.framity2.client.assets.overlay.ColoredLike;
import dev.alexnader.framity2.client.assets.overlay.Offsetters;
import dev.alexnader.framity2.client.assets.overlay.Overlay;
import dev.alexnader.framity2.client.assets.overlay.TextureSource;
import dev.alexnader.framity2.util.Float4;
import grondag.jmx.api.QuadTransformRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static dev.alexnader.framity2.client.Framity2Client.CLIENT_OVERLAYS;
import static dev.alexnader.framity2.client.util.QuadUtil.calcCenter;

public abstract class FrameTransform implements RenderContext.QuadTransform {

    public static class Data {
        @Nullable
        Pair<SpritesSource, BlockState> sprites;
        @Nullable
        Overlay overlay;
        @Nullable
        Integer maybeCachedOverlayColor;
        int color;

        public Data(@Nullable Pair<SpritesSource, BlockState> sprites, @Nullable Overlay overlay, @Nullable Integer maybeCachedOverlayColor, int color) {
            this.sprites = sprites;
            this.overlay = overlay;
            this.maybeCachedOverlayColor = maybeCachedOverlayColor;
            this.color = color;
        }

        @Nullable
        public Pair<SpritesSource, BlockState> sprites() {
            return sprites;
        }

        @Nullable
        public Overlay overlay() {
            return overlay;
        }

        @Nullable
        public Integer maybeCachedOverlayColor() {
            return maybeCachedOverlayColor;
        }

        public int color() {
            return color;
        }
    }

    private final BlockState state;
    protected Data[] data;

    protected FrameTransform(BlockRenderView brv, BlockState state, BlockPos pos, Supplier<Random> randomSupplier) {
        this.state = state;

        if (!(state.getBlock() instanceof Frame)) {
            throw new IllegalArgumentException("Cannot apply frame transform to non-frame block " + state.getBlock() + ".");
        }

        //noinspection unchecked
        Stream<Pair<Optional<BlockState>, Optional<Identifier>>> attachment =
            (Stream<Pair<Optional<BlockState>, Optional<Identifier>>>) ((RenderAttachedBlockView) brv).getBlockEntityRenderAttachment(pos);

        //noinspection ConstantConditions
        data = attachment.map(pair -> {
            Optional<BlockState> maybeBaseState = pair.getFirst();

            int color;
            @Nullable Pair<SpritesSource, BlockState> sprites;

            if (maybeBaseState.isPresent()) {
                BlockState baseState = maybeBaseState.get();
                color = Optional.ofNullable(ColorProviderRegistry.BLOCK.get(baseState.getBlock()))
                    .map(prov -> prov.getColor(baseState, brv, pos, 1) | 0xFF000000)
                    .orElse(0xFFFFFFFF);
                sprites = Pair.of(
                    new SpritesSource(baseState, MinecraftClient.getInstance().getBlockRenderManager().getModel(baseState), randomSupplier.get()),
                    baseState
                );
            } else {
                color = 0xFFFFFFFF;
                sprites = null;
            }

            @Nullable Overlay overlay = pair.getSecond()
                .map(CLIENT_OVERLAYS::getOverlayFor)
                .orElse(null);

            @Nullable Integer cachedOverlayColor;
            if (overlay == null) {
                cachedOverlayColor = null;
            } else {
                ColoredLike coloredLike = overlay.coloredLike();
                if (coloredLike == null) {
                    cachedOverlayColor = null;
                } else {
                    cachedOverlayColor = Optional.ofNullable(ColorProviderRegistry.BLOCK.get(coloredLike.colorSource().getBlock()))
                        .map(prov -> prov.getColor(coloredLike.colorSource(), brv, pos, 1) | 0xFF000000)
                        .orElse(null);
                }
            }

            return new Data(sprites, overlay, cachedOverlayColor, color);
        }).toArray(Data[]::new);
    }

    protected int getPartIndex(MutableQuadView mqv, Direction dir) {
        return ((FrameSlotInfo) state.getBlock()).getRelativeSlotAt(
            state,
            new Vec3d(
                calcCenter(mqv::x),
                calcCenter(mqv::y),
                calcCenter(mqv::z)
            ),
            dir
        );
    }

    protected void applySpriteAndColor(MutableQuadView mqv, Sprite sprite, @Nullable Integer color, Float4 us, Float4 vs, int spriteIndex) {
        mqv.sprite(0, spriteIndex, MathHelper.lerp(us.a, sprite.getMinU(), sprite.getMaxU()), MathHelper.lerp(vs.a, sprite.getMinV(), sprite.getMaxV()));
        mqv.sprite(1, spriteIndex, MathHelper.lerp(us.b, sprite.getMinU(), sprite.getMaxU()), MathHelper.lerp(vs.b, sprite.getMinV(), sprite.getMaxV()));
        mqv.sprite(2, spriteIndex, MathHelper.lerp(us.c, sprite.getMinU(), sprite.getMaxU()), MathHelper.lerp(vs.c, sprite.getMinV(), sprite.getMaxV()));
        mqv.sprite(3, spriteIndex, MathHelper.lerp(us.d, sprite.getMinU(), sprite.getMaxU()), MathHelper.lerp(vs.d, sprite.getMinV(), sprite.getMaxV()));

        if (color != null) {
            mqv.spriteColor(spriteIndex, color, color, color, color);
        }
    }

    protected Pair<Float4, Float4> getUvs(MutableQuadView mqv, Direction dir) {
        IntStream us = IntStream.rangeClosed(0, 3);
        IntStream vs = IntStream.rangeClosed(0, 3);
        switch (dir) {
        case DOWN:
            return Pair.of(
                Float4.fromIterator(us.mapToDouble(i -> MathHelper.clamp(mqv.x(i), 0, 1)).iterator()),
                Float4.fromIterator(vs.mapToDouble(i -> 1 - MathHelper.clamp(mqv.z(i), 0, 1)).iterator())
            );
        case UP:
            return Pair.of(
                Float4.fromIterator(us.mapToDouble(i -> MathHelper.clamp(mqv.x(i), 0, 1)).iterator()),
                Float4.fromIterator(vs.mapToDouble(i -> MathHelper.clamp(mqv.z(i), 0, 1)).iterator())
            );
        case NORTH:
            return Pair.of(
                Float4.fromIterator(us.mapToDouble(i -> 1 - MathHelper.clamp(mqv.x(i), 0f, 1f)).iterator()),
                Float4.fromIterator(vs.mapToDouble(i -> 1 - MathHelper.clamp(mqv.y(i), 0f, 1f)).iterator())
            );
        case SOUTH:
            return Pair.of(
                Float4.fromIterator(us.mapToDouble(i -> MathHelper.clamp(mqv.x(i), 0f, 1f)).iterator()),
                Float4.fromIterator(vs.mapToDouble(i -> 1 - MathHelper.clamp(mqv.y(i), 0f, 1f)).iterator())
            );
        case EAST:
            return Pair.of(
                Float4.fromIterator(us.mapToDouble(i -> 1 - MathHelper.clamp(mqv.z(i), 0f, 1f)).iterator()),
                Float4.fromIterator(vs.mapToDouble(i -> 1 - MathHelper.clamp(mqv.y(i), 0f, 1f)).iterator())
            );
        case WEST:
            return Pair.of(
                Float4.fromIterator(us.mapToDouble(i -> MathHelper.clamp(mqv.z(i), 0f, 1f)).iterator()),
                Float4.fromIterator(vs.mapToDouble(i -> 1 - MathHelper.clamp(mqv.y(i), 0f, 1f)).iterator())
            );
        default:
            throw new IllegalArgumentException("Invalid direction: " + dir);
        }
    }

    public static class NonFrex extends FrameTransform {
        public static QuadTransformRegistry.QuadTransformSource SOURCE = new QuadTransformRegistry.QuadTransformSource() {
            @Override
            public RenderContext.QuadTransform getForBlock(BlockRenderView brv, BlockState state, BlockPos pos, Supplier<Random> randomSupplier) {
                return new NonFrex(brv, state, pos, randomSupplier);
            }

            @Override
            public RenderContext.QuadTransform getForItem(ItemStack itemStack, Supplier<Random> supplier) {
                return null;
            }
        };

        private final EnumMap<Direction, Integer> transformedCount = new EnumMap<>(Direction.class);

        public NonFrex(BlockRenderView brv, BlockState state, BlockPos pos, Supplier<Random> randomSupplier) {
            super(brv, state, pos, randomSupplier);
        }

        @Override
        public boolean transform(MutableQuadView mqv) {
            if (mqv.tag() == 1) {
                return true;
            }

            Direction dir = mqv.lightFace();

            int quadIndex = transformedCount.computeIfAbsent(dir, d -> 0);
            transformedCount.put(dir, quadIndex + 1);

            int partIndex = getPartIndex(mqv, dir);

            Data data = this.data[partIndex];

            Pair<Float4, Float4> origUvs = getUvs(mqv, dir);

            @Nullable Pair<Sprite, Integer> maybeSpriteAndColor;
            Float4 us, vs;

            // Non-Frex models are defined such that all quads appear twice
            // The first quad of a pair is used to render the base texture,
            // while the second quad is used to render the overlay.

            Function<SpritesSource, Pair<Sprite, Integer>> findMaybeSpriteAndColor =
                sprites -> sprites.getSpriteAndColor(dir, quadIndex % sprites.getCount(dir), data.color);

            if (quadIndex % 2 == 0) {
                if (data.sprites == null) {
                    return true; // no custom texture => stop transforming, show regular texture
                }

                maybeSpriteAndColor = findMaybeSpriteAndColor.apply(data.sprites.getFirst());
                us = origUvs.getFirst();
                vs = origUvs.getSecond();
            } else {
                Overlay overlay = data.overlay;

                if (overlay == null) {
                    @Nullable Pair<SpritesSource, BlockState> maybeSprites = data.sprites;
                    if (maybeSprites == null) {
                        maybeSpriteAndColor = null;
                    } else {
                        maybeSpriteAndColor = findMaybeSpriteAndColor.apply(maybeSprites.getFirst());
                    }

                    us = origUvs.getFirst();
                    vs = origUvs.getSecond();
                } else {
                    TextureSource textureSource = overlay.textureSource();
                    if (textureSource == null) {
                        maybeSpriteAndColor = null;
                    } else {
                        Identifier spriteId = textureSource.textureFor(dir);
                        if (spriteId == null) {
                            return false; // there is an overlay, but it doesn't have a texture for this direction
                        }
                        //noinspection deprecation
                        maybeSpriteAndColor = Pair.of(
                            MinecraftClient.getInstance()
                                .getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
                                .apply(spriteId),
                            data.maybeCachedOverlayColor
                        );
                    }

                    Map<Direction, Offsetters> sidedOffsetters = overlay.sidedOffsetters();
                    if (sidedOffsetters == null) {
                        us = origUvs.getFirst();
                        vs = origUvs.getSecond();
                    } else {
                        Offsetters offsetters = sidedOffsetters.get(dir);
                        if (offsetters == null) {
                            us = origUvs.getFirst();
                            vs = origUvs.getSecond();
                        } else {
                            us = offsetters.u.map(o -> o.offset(origUvs.getFirst())).orElseGet(origUvs::getFirst);
                            vs = offsetters.v.map(o -> o.offset(origUvs.getSecond())).orElseGet(origUvs::getSecond);
                        }
                    }
                }
            }

            if (maybeSpriteAndColor != null) {
                applySpriteAndColor(mqv, maybeSpriteAndColor.getFirst(), maybeSpriteAndColor.getSecond(), us, vs, 0);
                return true;
            } else {
                return false;
            }
        }
    }
}
