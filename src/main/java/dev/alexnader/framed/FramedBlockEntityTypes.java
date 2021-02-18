package dev.alexnader.framed;

import dev.alexnader.framed.block.entity.FrameBlockEntity;
import dev.alexnader.framed.block.frame.Frame;
import dev.alexnader.framed.block.frame.data.Sections;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

import java.util.function.Supplier;

import static dev.alexnader.framed.Framed.BLOCKS;
import static dev.alexnader.framed.Framed.META;

public class FramedBlockEntityTypes extends Registrar<BlockEntityType<?>> {
    public FramedBlockEntityTypes() {
        super(Registry.BLOCK_ENTITY_TYPE);
    }

    // note: IDs must match those in BlockEntityMigration
    public final BlockEntityType<FrameBlockEntity> BLOCK_FRAME =          register("block_frame",          ()->this.BLOCK_FRAME,          BLOCKS.BLOCK_FRAME,          Blocks.OAK_PLANKS,         META.FRAME_SECTIONS);
    public final BlockEntityType<FrameBlockEntity> SLAB_FRAME =           register("slab_frame",           ()->this.SLAB_FRAME,           BLOCKS.SLAB_FRAME,           Blocks.OAK_SLAB,           META.SLAB_FRAME_SECTIONS);
    public final BlockEntityType<FrameBlockEntity> STAIRS_FRAME =         register("stairs_frame",         ()->this.STAIRS_FRAME,         BLOCKS.STAIRS_FRAME,         Blocks.OAK_STAIRS,         META.FRAME_SECTIONS);
    public final BlockEntityType<FrameBlockEntity> FENCE_FRAME =          register("fence_frame",          ()->this.FENCE_FRAME,          BLOCKS.FENCE_FRAME,          Blocks.OAK_FENCE,          META.FRAME_SECTIONS);
    public final BlockEntityType<FrameBlockEntity> FENCE_GATE_FRAME =     register("fence_gate_frame",     ()->this.FENCE_GATE_FRAME,     BLOCKS.FENCE_GATE_FRAME,     Blocks.OAK_FENCE_GATE,     META.FRAME_SECTIONS);
    public final BlockEntityType<FrameBlockEntity> TRAPDOOR_FRAME =       register("trapdoor_frame",       ()->this.TRAPDOOR_FRAME,       BLOCKS.TRAPDOOR_FRAME,       Blocks.OAK_TRAPDOOR,       META.FRAME_SECTIONS);
    public final BlockEntityType<FrameBlockEntity> DOOR_FRAME =           register("door_frame",           ()->this.DOOR_FRAME,           BLOCKS.DOOR_FRAME,           Blocks.OAK_DOOR,           META.FRAME_SECTIONS);
    public final BlockEntityType<FrameBlockEntity> PATH_FRAME =           register("path_frame",           ()->this.PATH_FRAME,           BLOCKS.PATH_FRAME,           Blocks.GRASS_PATH,         META.FRAME_SECTIONS);
    public final BlockEntityType<FrameBlockEntity> TORCH_FRAME =          register("torch_frame",          ()->this.TORCH_FRAME,          BLOCKS.TORCH_FRAME,          Blocks.TORCH,              META.FRAME_SECTIONS);
    public final BlockEntityType<FrameBlockEntity> WALL_TORCH_FRAME =     register("wall_torch_frame",     ()->this.WALL_TORCH_FRAME,     BLOCKS.WALL_TORCH_FRAME,     Blocks.WALL_TORCH,         META.FRAME_SECTIONS);
    public final BlockEntityType<FrameBlockEntity> PRESSURE_PLATE_FRAME = register("pressure_plate_frame", ()->this.PRESSURE_PLATE_FRAME, BLOCKS.PRESSURE_PLATE_FRAME, Blocks.OAK_PRESSURE_PLATE, META.FRAME_SECTIONS);
    public final BlockEntityType<FrameBlockEntity> WALL_FRAME =           register("wall_frame",           ()->this.WALL_FRAME,           BLOCKS.WALL_FRAME,           Blocks.COBBLESTONE_WALL,   META.FRAME_SECTIONS);
    public final BlockEntityType<FrameBlockEntity> LAYER_FRAME =          register("layer_frame",          ()->this.LAYER_FRAME,          BLOCKS.LAYER_FRAME,          Blocks.SNOW,               META.FRAME_SECTIONS);
    public final BlockEntityType<FrameBlockEntity> CARPET_FRAME =         register("carpet_frame",         ()->this.CARPET_FRAME,         BLOCKS.CARPET_FRAME,         Blocks.WHITE_CARPET,       META.FRAME_SECTIONS);
    public final BlockEntityType<FrameBlockEntity> PANE_FRAME =           register("pane_frame",           ()->this.PANE_FRAME,           BLOCKS.PANE_FRAME,           Blocks.GLASS_PANE,         META.FRAME_SECTIONS);

    private <F extends Block & Frame> BlockEntityType<FrameBlockEntity> register(String path, Supplier<BlockEntityType<FrameBlockEntity>> blockEntityType, F frame, Block base, Sections sections) {
        return register(
            BlockEntityType.Builder.create(
                () -> new FrameBlockEntity(blockEntityType.get(), base, sections),
                frame
            ).build(null),
            META.id(path)
        );
    }
}
