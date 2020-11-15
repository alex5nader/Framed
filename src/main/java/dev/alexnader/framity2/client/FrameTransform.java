package dev.alexnader.framity2.client;

import com.mojang.datafixers.util.Pair;
import dev.alexnader.framity2.block.FrameSlotInfo;
import dev.alexnader.framity2.block.frame.Frame;
import dev.alexnader.framity2.client.assets.ColorApplier;
import dev.alexnader.framity2.client.assets.overlay.Overlay;
import dev.alexnader.framity2.util.Float4;
import grondag.frex.api.material.MaterialMap;
import grondag.jmx.api.QuadTransformRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
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
import static dev.alexnader.framity2.util.FunctionalUtil.*;

@Environment(EnvType.CLIENT)
public final class FrameTransform implements RenderContext.QuadTransform {
    public static final QuadTransformRegistry.QuadTransformSource SOURCE = new QuadTransformRegistry.QuadTransformSource() {
        @Override
        public RenderContext.QuadTransform getForBlock(final BlockRenderView brv, final BlockState state, final BlockPos pos, final Supplier<Random> randomSupplier) {
            return new FrameTransform(brv, state, pos, randomSupplier);
        }

        @Nullable
        @Override
        public RenderContext.QuadTransform getForItem(final ItemStack itemStack, final Supplier<Random> supplier) {
            return null;
        }
    };

    private final EnumMap<Direction, Integer> transformedCount = new EnumMap<>(Direction.class);

    @Override
    public boolean transform(final MutableQuadView mqv) {
        if (mqv.tag() == 1) {
            return true;
        }

        final Direction dir = mqv.lightFace();

        final int quadIndex = transformedCount.computeIfAbsent(dir, d -> 0);
        transformedCount.put(dir, quadIndex + 1);

        final int partIndex = getPartIndex(mqv, dir);

        final Data data = this.data[partIndex];

        final Pair<Float4, Float4> origUvs = getUvs(mqv, dir);

        // JMX defines models such that all quads appear twice
        // The first quad of a pair is used to render the base texture,
        // while the second quad is used to render the overlay.

        final Function<Pair<SpritesSource, BlockState>, Pair<Pair<Sprite, Optional<BlockState>>, OptionalInt>> findMaybeSpriteAndColor =
            pair -> pair.getFirst()
                .getSpriteAndColor(dir, quadIndex % pair.getFirst().getCount(dir), data.color)
                .mapFirst(s -> Pair.of(s, Optional.of(pair.getSecond())));

        if (quadIndex % 2 == 0) {
            if (!data.sprites.isPresent()) {
                return true; // no custom texture => stop transforming, show regular texture
            }

            final Float4 us = origUvs.getFirst();
            final Float4 vs = origUvs.getSecond();

            final Pair<Pair<Sprite, Optional<BlockState>>, OptionalInt> spriteAndColor = findMaybeSpriteAndColor.apply(data.sprites.get());

            final Sprite sprite = spriteAndColor.getFirst().getFirst();

            spriteAndColor.getFirst().getSecond().ifPresent(materialSource -> {
                final @Nullable RenderMaterial target = MaterialMap.get(materialSource).getMapped(sprite);

                if (target != null) {
                    mqv.material(target);
                }
            });

            applySpriteAndColor(mqv, sprite, spriteAndColor.getSecond(), us, vs);
            return true;
        } else {
            return data.overlay.match(
                overlay -> {
                    data.cachedOverlayColorApplier.apply(mqv);
                    return overlay.apply(mqv, origUvs.getFirst(), origUvs.getSecond(), dir);
                },
                () -> {
                    final Optional<Pair<Pair<Sprite, Optional<BlockState>>, OptionalInt>> maybeSpriteAndColor =
                        data.sprites.map(findMaybeSpriteAndColor);
                    final Float4 us = origUvs.getFirst();
                    final Float4 vs = origUvs.getSecond();

                    maybeSpriteAndColor.ifPresent(spriteAndColor -> {
                        final Sprite sprite = spriteAndColor.getFirst().getFirst();

                        spriteAndColor.getFirst().getSecond().ifPresent(materialSource -> {
                            final @Nullable RenderMaterial target = MaterialMap.get(materialSource).getMapped(sprite);

                            if (target != null) {
                                mqv.material(target);
                            }
                        });

                        applySpriteAndColor(mqv, sprite, spriteAndColor.getSecond(), us, vs);
                    });

                    return maybeSpriteAndColor.isPresent();
                }
            );

//            data.overlay.apply(mqv, origUvs.getFirst(), origUvs.getSecond(), dir);

//            final Optional<Overlay> maybeOverlay = data.overlay;
//
//            if (!maybeOverlay.isPresent()) {
//                maybeSpriteAndColor = data.sprites.map(findMaybeSpriteAndColor);
//
//                us = origUvs.getFirst();
//                vs = origUvs.getSecond();
//            } else {
//                final Overlay overlay = maybeOverlay.get();
//
//                overlay.textureSource().apply(mqv, us, vs, dir);
//
////                final Optional<TextureSource> maybeTextureSource = overlay.textureSource();
////                if (!maybeTextureSource.isPresent()) {
////                    maybeSpriteAndColor = Optional.empty();
////                } else {
////                    final TextureSource textureSource = maybeTextureSource.get();
////
////                    textureSource.apply(mqv, us, vs, dir);
////
////                    final Optional<TextureSource.Entry> maybeEntry = textureSource.entryFor(dir);
////
////                    if (!maybeEntry.isPresent()) {
////                        return false; // there is an overlay, but it doesn't have a texture for this direction
////                    }
////
////                    //noinspection deprecation
////                    maybeSpriteAndColor = maybeEntry.map(entry ->
////                        Pair.of(
////                            Pair.of(
////                                MinecraftClient.getInstance()
////                                    .getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
////                                    .apply(entry.texture),
////                                entry.materialSource
////                            ),
////                            data.maybeCachedOverlayColor
////                        )
////                    );
////                }
//
//                final Optional<Map<Direction, Offsetters>> maybeSidedOffsetters = overlay.sidedOffsetters();
//                if (!maybeSidedOffsetters.isPresent()) {
//                    us = origUvs.getFirst();
//                    vs = origUvs.getSecond();
//                } else {
//                    final Map<Direction, Offsetters> sidedOffsetters = maybeSidedOffsetters.get();
//
//                    final Offsetters offsetters = sidedOffsetters.get(dir);
//                    if (offsetters == null) {
//                        us = origUvs.getFirst();
//                        vs = origUvs.getSecond();
//                    } else {
//                        us = offsetters.u.map(o -> o.offset(origUvs.getFirst())).orElseGet(origUvs::getFirst);
//                        vs = offsetters.v.map(o -> o.offset(origUvs.getSecond())).orElseGet(origUvs::getSecond);
//                    }
//                }
//            }
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static class Data {
        public final Optional<Pair<SpritesSource, BlockState>> sprites;
        public final Overlay overlay;
        public final ColorApplier cachedOverlayColorApplier;
        public final int color;

        public Data(final Optional<Pair<SpritesSource, BlockState>> sprites, final Overlay overlay, final OptionalInt maybeCachedOverlayColor, final int color) {
            this.sprites = sprites;
            this.overlay = overlay;
            this.cachedOverlayColorApplier = ColorApplier.ofOptional(maybeCachedOverlayColor);
            this.color = color;
        }
    }

    private final BlockState state;
    protected Data[] data;

    protected FrameTransform(final BlockRenderView brv, final BlockState state, final BlockPos pos, final Supplier<Random> randomSupplier) {
        this.state = state;

        if (!(state.getBlock() instanceof Frame)) {
            throw new IllegalArgumentException("Cannot apply frame transform to non-frame block " + state.getBlock() + ".");
        }

        //noinspection unchecked
        final Stream<Pair<Optional<BlockState>, Optional<Identifier>>> attachment =
            (Stream<Pair<Optional<BlockState>, Optional<Identifier>>>) ((RenderAttachedBlockView) brv).getBlockEntityRenderAttachment(pos);

        //noinspection ConstantConditions
        data = attachment.map(pair -> {
            final Optional<BlockState> maybeBaseState = pair.getFirst();

            final int color;
            final Optional<Pair<SpritesSource, BlockState>> sprites;

            if (maybeBaseState.isPresent()) {
                final BlockState baseState = maybeBaseState.get();
                color = Optional.ofNullable(ColorProviderRegistry.BLOCK.get(baseState.getBlock()))
                    .map(prov -> prov.getColor(baseState, brv, pos, 1) | 0xFF000000)
                    .orElse(0xFFFFFFFF);
                sprites = Optional.of(Pair.of(
                    new SpritesSource(baseState, MinecraftClient.getInstance().getBlockRenderManager().getModel(baseState), randomSupplier.get()),
                    baseState
                ));
            } else {
                color = 0xFFFFFFFF;
                sprites = Optional.empty();
            }

            final Overlay overlay = pair.getSecond().map(CLIENT_OVERLAYS::getOverlayFor).orElse(Overlay.NONE);

            final OptionalInt cachedOverlayColor =
                flatMapToInt(
                    overlay.coloredLike(),
                    coloredLike -> mapToInt(
                        Optional.ofNullable(ColorProviderRegistry.BLOCK.get(coloredLike.colorSource().getBlock())),
                        prov -> prov.getColor(coloredLike.colorSource(), brv, pos, 1) | 0xFF000000
                    )
                );

            return new Data(sprites, overlay, cachedOverlayColor, color);
        }).toArray(Data[]::new);
    }

    protected int getPartIndex(final MutableQuadView mqv, final Direction dir) {
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

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected void applySpriteAndColor(final MutableQuadView mqv, final Sprite sprite, final OptionalInt maybeColor, final Float4 us, final Float4 vs) {
        mqv.sprite(0, 0, MathHelper.lerp(us.a, sprite.getMinU(), sprite.getMaxU()), MathHelper.lerp(vs.a, sprite.getMinV(), sprite.getMaxV()));
        mqv.sprite(1, 0, MathHelper.lerp(us.b, sprite.getMinU(), sprite.getMaxU()), MathHelper.lerp(vs.b, sprite.getMinV(), sprite.getMaxV()));
        mqv.sprite(2, 0, MathHelper.lerp(us.c, sprite.getMinU(), sprite.getMaxU()), MathHelper.lerp(vs.c, sprite.getMinV(), sprite.getMaxV()));
        mqv.sprite(3, 0, MathHelper.lerp(us.d, sprite.getMinU(), sprite.getMaxU()), MathHelper.lerp(vs.d, sprite.getMinV(), sprite.getMaxV()));

        if (maybeColor.isPresent()) {
            final int color = maybeColor.getAsInt();

            mqv.spriteColor(0, color, color, color, color);
        }
    }

    protected Pair<Float4, Float4> getUvs(final MutableQuadView mqv, final Direction dir) {
        final IntStream us = IntStream.rangeClosed(0, 3);
        final IntStream vs = IntStream.rangeClosed(0, 3);
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
}
