package dev.alexnader.framity2;

import dev.alexnader.framity2.block.entity.FrameBlockEntity;
import dev.alexnader.framity2.block.frame.data.Sections;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

import static dev.alexnader.framity2.Framity2.BLOCKS;
import static dev.alexnader.framity2.Framity2.META;

public class FramityBlockEntityTypes extends Registrar<BlockEntityType<?>> {
    public FramityBlockEntityTypes() {
        super(Registry.BLOCK_ENTITY_TYPE);
    }

    public final BlockEntityType<FrameBlockEntity> FRAME_BET = register(
        BlockEntityType.Builder.create(
            () -> new FrameBlockEntity(this.FRAME_BET, new Sections(1)),
            BLOCKS.BLOCK_FRAME,
            BLOCKS.STAIRS_FRAME,
            BLOCKS.FENCE_FRAME,
            BLOCKS.FENCE_GATE_FRAME,
            BLOCKS.TRAPDOOR_FRAME,
            BLOCKS.DOOR_FRAME
        ).build(null),
        META.id("frame")
    );

    public final BlockEntityType<FrameBlockEntity> SLAB_FRAME_BET = register(
        BlockEntityType.Builder.create(
            () -> new FrameBlockEntity(this.SLAB_FRAME_BET, new Sections(2)),
            BLOCKS.SLAB_FRAME
        ).build(null),
        META.id("slab_frame")
    );
}
