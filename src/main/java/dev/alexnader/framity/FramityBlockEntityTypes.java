package dev.alexnader.framity;

import dev.alexnader.framity.block.entity.FrameBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

import static dev.alexnader.framity.Framity.BLOCKS;
import static dev.alexnader.framity.Framity.META;

public class FramityBlockEntityTypes extends Registrar<BlockEntityType<?>> {
    public FramityBlockEntityTypes() {
        super(Registry.BLOCK_ENTITY_TYPE);
    }

    public final BlockEntityType<FrameBlockEntity> FRAME = register(
        BlockEntityType.Builder.create(
            () -> new FrameBlockEntity(this.FRAME, META.FRAME_SECTIONS),
            BLOCKS.BLOCK_FRAME,
            BLOCKS.STAIRS_FRAME,
            BLOCKS.FENCE_FRAME,
            BLOCKS.FENCE_GATE_FRAME,
            BLOCKS.TRAPDOOR_FRAME,
            BLOCKS.DOOR_FRAME,
            BLOCKS.PATH_FRAME,
            BLOCKS.TORCH_FRAME,
            BLOCKS.WALL_TORCH_FRAME
        ).build(null),
        META.id("frame")
    );

    public final BlockEntityType<FrameBlockEntity> SLAB_FRAME = register(
        BlockEntityType.Builder.create(
            () -> new FrameBlockEntity(this.SLAB_FRAME, META.SLAB_FRAME_SECTIONS),
            BLOCKS.SLAB_FRAME
        ).build(null),
        META.id("slab_frame")
    );
}
