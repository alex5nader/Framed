package dev.alexnader.framed.mixin.client.local;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;
import dev.alexnader.framed.block.FrameSlotInfo;
import dev.alexnader.framed.block.entity.FrameBlockEntity;
import dev.alexnader.framed.block.frame.data.FrameData;
import dev.alexnader.framed.block.frame.data.Sections;
import dev.alexnader.framed.client.assets.overlay.Overlay;
import dev.alexnader.framed.client.transform.BaseApplier;
import dev.alexnader.framed.client.transform.FrameTransform;
import dev.alexnader.framed.client.transform.TransformResult;
import dev.alexnader.framed.client.util.FrameMeshGeneratorRenderContext;
import dev.alexnader.framed.mixin.mc.BakedQuadAccess;
import dev.alexnader.framed.util.Float4;
import grondag.frex.api.material.MaterialMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static dev.alexnader.framed.Framed.META;
import static dev.alexnader.framed.Framed.OVERLAYS;
import static dev.alexnader.framed.Framed.PROPERTIES;
import static dev.alexnader.framed.client.FramedClient.CLIENT_OVERLAYS;
import static dev.alexnader.framed.client.util.QuadUtil.calcCenter;
import static dev.alexnader.framed.util.FunctionalUtil.flatMapToInt;
import static dev.alexnader.framed.util.FunctionalUtil.mapToInt;

@Mixin(FrameBlockEntity.class)
@Environment(EnvType.CLIENT)
@SuppressWarnings("FieldMayBeFinal")
public abstract class FrameMeshHolderImpl extends LockableContainerBlockEntity implements RenderAttachmentBlockEntity {
    @Shadow private FrameData data;
    private static final Set<Property<?>> IGNORED_PROPERTIES;
    static {
        IGNORED_PROPERTIES = new ObjectOpenHashSet<>();
        IGNORED_PROPERTIES.add(PROPERTIES.HAS_REDSTONE);
        IGNORED_PROPERTIES.add(Properties.LIT);
    }

    private Block base;
//    private BlockStateMap<Mesh> meshes;
    private Mesh mesh;

    private FrameSlotInfo slotInfo;
    private Set<Property<?>> importantProperties;

    private FrameMeshHolderImpl(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
        throw new IllegalStateException("Mixin constructor should not run.");
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    void constructor(BlockEntityType<?> type, Block base, Sections sections, CallbackInfo ci) {
        this.base = base;

        Set<Property<?>> importantProperties = new ObjectOpenHashSet<>(base.getStateManager().getProperties());
        importantProperties.removeAll(IGNORED_PROPERTIES);
        this.importantProperties = ImmutableSet.copyOf(importantProperties);
    }

    @Inject(method = "markDirty", at = @At("TAIL"))
    void triggerRebuild(CallbackInfo ci) {
        rebuild();
    }

    @Override
    public @Nullable Object getRenderAttachmentData() {
        return mesh;
    }

    private static <T extends Comparable<T>> BlockState copy(BlockState into, BlockState from, Property<T> property) {
        return into.with(property, from.get(property));
    }

    private int getPartIndex(final QuadView quad, final Direction dir) {
        return slotInfo.getRelativeSlotAt(
            new Vec3d(
                calcCenter(quad::x),
                calcCenter(quad::y),
                calcCenter(quad::z)
            ),
            dir
        );
    }

    protected Pair<Float4, Float4> getUvs(final QuadView quad, final Direction dir) {
        final IntStream us = IntStream.rangeClosed(0, 3);
        final IntStream vs = IntStream.rangeClosed(0, 3);
        switch (dir) {
        case DOWN:
            return Pair.of(
                Float4.fromIterator(us.mapToDouble(i -> MathHelper.clamp(quad.x(i), 0, 1)).iterator()),
                Float4.fromIterator(vs.mapToDouble(i -> 1 - MathHelper.clamp(quad.z(i), 0, 1)).iterator())
            );
        case UP:
            return Pair.of(
                Float4.fromIterator(us.mapToDouble(i -> MathHelper.clamp(quad.x(i), 0, 1)).iterator()),
                Float4.fromIterator(vs.mapToDouble(i -> MathHelper.clamp(quad.z(i), 0, 1)).iterator())
            );
        case NORTH:
            return Pair.of(
                Float4.fromIterator(us.mapToDouble(i -> 1 - MathHelper.clamp(quad.x(i), 0f, 1f)).iterator()),
                Float4.fromIterator(vs.mapToDouble(i -> 1 - MathHelper.clamp(quad.y(i), 0f, 1f)).iterator())
            );
        case SOUTH:
            return Pair.of(
                Float4.fromIterator(us.mapToDouble(i -> MathHelper.clamp(quad.x(i), 0f, 1f)).iterator()),
                Float4.fromIterator(vs.mapToDouble(i -> 1 - MathHelper.clamp(quad.y(i), 0f, 1f)).iterator())
            );
        case EAST:
            return Pair.of(
                Float4.fromIterator(us.mapToDouble(i -> 1 - MathHelper.clamp(quad.z(i), 0f, 1f)).iterator()),
                Float4.fromIterator(vs.mapToDouble(i -> 1 - MathHelper.clamp(quad.y(i), 0f, 1f)).iterator())
            );
        case WEST:
            return Pair.of(
                Float4.fromIterator(us.mapToDouble(i -> MathHelper.clamp(quad.z(i), 0f, 1f)).iterator()),
                Float4.fromIterator(vs.mapToDouble(i -> 1 - MathHelper.clamp(quad.y(i), 0f, 1f)).iterator())
            );
        default:
            throw new IllegalArgumentException("Invalid direction: " + dir);
        }
    }

    private void idk(FrameTransform.Data[] allData, EnumMap<Direction, Integer> transformedCount, MeshBuilder builder, QuadEmitter[] qe, Consumer<MutableQuadView> copyFromBase) {
        // setup
        copyFromBase.accept(qe[0]);

        final Direction dir = qe[0].lightFace();

        final int quadIndex = transformedCount.computeIfAbsent(dir, d -> 0);
        transformedCount.put(dir, quadIndex + 1);

        final int partIndex = getPartIndex(qe[0], dir);

        final FrameTransform.Data data = allData[partIndex];

        final Pair<Float4, Float4> origUvs = getUvs(qe[0], dir);

        // base layer
        // base is already copied

        TransformResult result = data.baseApplier.apply(qe[0], dir, quadIndex, origUvs.getFirst(), origUvs.getSecond(), data.baseColor);
        // emit quad even when NOTHING_TO_DO so that regular frame texture shows
        switch (result.status) {
        case DID_SOMETHING:
        case NOTHING_TO_DO:
            qe[0].emit();
            break;
        case FAILED:
            META.LOGGER.warn("An error occurred with a frame: " + result.message);
            qe[0] = builder.getEmitter();
            // don't emit
            break;
        }

        // overlay layer
        copyFromBase.accept(qe[0]);

        TransformResult result2 = data.overlay.match(
            overlay -> {
                data.overlayColorApplier.apply(qe[0]);
                return overlay.apply(qe[0], origUvs.getFirst(), origUvs.getSecond(), dir);
            },
            () -> data.baseApplier.apply(qe[0], dir, quadIndex, origUvs.getFirst(), origUvs.getSecond(), data.baseColor)
        );

        switch (result.status) {
        case DID_SOMETHING:
            qe[0].emit();
            break;
        case FAILED:
            META.LOGGER.warn("An error occurred with a frame: " + result2.message);
        case NOTHING_TO_DO:
            qe[0] = builder.getEmitter();
            break;
        }

        qe[0].emit();
    }

    public void rebuild() {
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        if (renderer == null || world == null) {
            mesh = null;
            return;
        }

        slotInfo = (FrameSlotInfo) getCachedState().getBlock();

        BlockModels models = MinecraftClient.getInstance().getBakedModelManager().getBlockModels();

        BlockState state;
        {
            BlockState frameState = getCachedState();
            BlockState state2 = base.getDefaultState();
            for (Property<?> property : importantProperties) {
                state2 = copy(state2, frameState, property);
            }
            state = state2;
        }

        MeshBuilder builder = renderer.meshBuilder();
        QuadEmitter[] qe = {builder.getEmitter()};

        FabricBakedModel fabricModel = (FabricBakedModel) models.getModel(state);

        EnumMap<Direction, Integer> transformedCount = new EnumMap<>(Direction.class);

        Random random = new Random();
        //noinspection UnstableApiUsage // should be replaced with loop eventually
        FrameTransform.Data[] allData =
            Streams.zip(
                Arrays.stream(this.data.baseStates()),
                this.data.overlayItems().stream().map(i -> i.flatMap(OVERLAYS::getOverlayId)),
                Pair::new
            )
            .map(pair -> {
                final Optional<BlockState> maybeBaseState = pair.getFirst();

                final int color;
                final BaseApplier baseApplier;

                if (maybeBaseState.isPresent()) {
                    final BlockState baseState = maybeBaseState.get();
                    color = Optional.ofNullable(ColorProviderRegistry.BLOCK.get(baseState.getBlock()))
                        .map(prov -> prov.getColor(baseState, world, pos, 1) | 0xFF000000)
                        .orElse(0xFFFFFFFF);
                    baseApplier = new BaseApplier.Some(baseState, MinecraftClient.getInstance().getBlockRenderManager().getModel(baseState), random);
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
                            prov -> prov.getColor(coloredLike.colorSource(), world, pos, 1) | 0xFF000000
                        )
                    );

                return new FrameTransform.Data(baseApplier, overlay, cachedOverlayColor, color);
            }).toArray(FrameTransform.Data[]::new);

        fabricModel.emitBlockQuads(world, state, pos, Random::new, new FrameMeshGeneratorRenderContext(
            mesh -> mesh.forEach(quad -> idk(allData, transformedCount, builder, qe, quad::copyTo)),
            bakedModel -> {
                Random r = new Random();
                for (int i = 0; i <= 6; i++) {
                    Direction dir = ModelHelper.faceFromIndex(i);
                    for (BakedQuad quad : bakedModel.getQuads(state, dir, r)) {
                        idk(allData, transformedCount, builder, qe, qe2 -> qe2.fromVanilla(quad, MaterialMap.get(state).getMapped(((BakedQuadAccess)quad).sprite()), dir));
                    }
                }
            }
        ));

        mesh = builder.build();
    }
}
