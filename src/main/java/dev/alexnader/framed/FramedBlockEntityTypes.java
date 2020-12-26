package dev.alexnader.framed;

import dev.alexnader.framed.block.entity.FrameBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

import static dev.alexnader.framed.Framed.BLOCKS;
import static dev.alexnader.framed.Framed.META;

public class FramedBlockEntityTypes extends Registrar<BlockEntityType<?>> {
    public FramedBlockEntityTypes() {
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
            BLOCKS.WALL_TORCH_FRAME,
            BLOCKS.PRESSURE_PLATE_FRAME
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
