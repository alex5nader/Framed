package dev.alexnader.framity2.client.transform;

import com.mojang.datafixers.util.Pair;
import dev.alexnader.framity2.block.FrameSlotInfo;
import dev.alexnader.framity2.block.frame.data.FrameData;
import dev.alexnader.framity2.client.assets.overlay.Overlay;
import dev.alexnader.framity2.util.Float4;
import grondag.jmx.api.QuadTransformRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

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

        @Override
        public RenderContext.QuadTransform getForItem(final ItemStack stack, final Supplier<Random> randomSupplier) {
            final FrameSlotInfo slotInfo = (FrameSlotInfo) ((BlockItem) stack.getItem()).getBlock();
            if (!stack.hasTag()) {
                return new FrameTransform(slotInfo, new FrameData(slotInfo.sections()), randomSupplier);
            } else {
                //noinspection ConstantConditions // any frame with a tag *should* have these keys
                return new FrameTransform(
                    slotInfo,
                    FrameData.fromTag(stack.getSubTag("BlockEntityTag").getCompound("frameData")),
                    randomSupplier
                );
            }
        }
    };

    private final EnumMap<Direction, Integer> transformedCount = new EnumMap<>(Direction.class);

    @Override
    public boolean transform(final MutableQuadView mqv) {
        if (mqv.tag() == 0) {
            return true;
        }

        final Direction dir = mqv.lightFace();

        final int quadIndex = transformedCount.computeIfAbsent(dir, d -> 0);
        transformedCount.put(dir, quadIndex + 1);

        final int partIndex = getPartIndex(mqv, dir);

        final Data data = this.data[partIndex];

        final Pair<Float4, Float4> origUvs = getUvs(mqv, dir);

        if (mqv.tag() == 1) {
            data.baseApplier.apply(mqv, dir, quadIndex, origUvs.getFirst(), origUvs.getSecond(), data.baseColor);
            // ignore return value of baseApplier.apply because it will return false when there's no custom texture
            // this is bad, because then the regular frame texture wouldn't show either (which should be the case when there's no custom texture)
            return true;
        } else if (mqv.tag() == 2) {
            return data.overlay.match(
                overlay -> {
                    data.overlayColorApplier.apply(mqv);
                    return overlay.apply(mqv, origUvs.getFirst(), origUvs.getSecond(), dir);
                },
                () -> data.baseApplier.apply(mqv, dir, quadIndex, origUvs.getFirst(), origUvs.getSecond(), data.baseColor)
            );
        } else {
            return false;
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static class Data {
        public final BaseApplier baseApplier;
        public final Overlay overlay;
        public final ColorApplier overlayColorApplier;
        public final int baseColor;

        public Data(final BaseApplier baseApplier, final Overlay overlay, final OptionalInt maybeCachedOverlayColor, final int baseColor) {
            this.baseApplier = baseApplier;
            this.overlay = overlay;
            this.overlayColorApplier = ColorApplier.ofOptional(maybeCachedOverlayColor);
            this.baseColor = baseColor;
        }
    }

    private final FrameSlotInfo slotInfo;
    private final Data[] data;

    private FrameTransform(final FrameSlotInfo slotInfo, final BlockRenderView brv, final BlockPos pos, final Supplier<Random> randomSupplier, final List<Pair<Optional<BlockState>, Optional<Identifier>>> attachment) {
        this.slotInfo = slotInfo;

        data = attachment.stream().map(pair -> {
            final Optional<BlockState> maybeBaseState = pair.getFirst();

            final int color;
            final BaseApplier baseApplier;

            if (maybeBaseState.isPresent()) {
                final BlockState baseState = maybeBaseState.get();
                color = Optional.ofNullable(ColorProviderRegistry.BLOCK.get(baseState.getBlock()))
                    .map(prov -> prov.getColor(baseState, brv, pos, 1) | 0xFF000000)
                    .orElse(0xFFFFFFFF);
                baseApplier = new BaseApplier.Some(baseState, MinecraftClient.getInstance().getBlockRenderManager().getModel(baseState), randomSupplier.get());
            } else {
                color = 0xFFFFFFFF;
                baseApplier = BaseApplier.NONE;
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

            return new Data(baseApplier, overlay, cachedOverlayColor, color);
        }).toArray(Data[]::new);
    }

    private FrameTransform(final FrameSlotInfo slotInfo, final FrameData frameData, final Supplier<Random> randomSupplier) {
        //noinspection ConstantConditions // player cannot be null while rendering, stack must have tag or this constructor will not run
        this(
            slotInfo,
            MinecraftClient.getInstance().player.clientWorld,
            MinecraftClient.getInstance().player.getBlockPos(),
            randomSupplier,
            frameData.toRenderAttachment()
        );
    }

    private FrameTransform(final BlockRenderView brv, final BlockState state, final BlockPos pos, final Supplier<Random> randomSupplier) {
        //noinspection unchecked,ConstantConditions
        this(
            (FrameSlotInfo) state.getBlock(),
            brv,
            pos,
            randomSupplier,
            (List<Pair<Optional<BlockState>, Optional<Identifier>>>) ((RenderAttachedBlockView) brv).getBlockEntityRenderAttachment(pos)
        );
    }

    protected int getPartIndex(final MutableQuadView mqv, final Direction dir) {
        return slotInfo.getRelativeSlotAt(
            new Vec3d(
                calcCenter(mqv::x),
                calcCenter(mqv::y),
                calcCenter(mqv::z)
            ),
            dir
        );
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
