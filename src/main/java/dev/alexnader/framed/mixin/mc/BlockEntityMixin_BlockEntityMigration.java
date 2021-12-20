package dev.alexnader.framed.mixin.mc;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

import static dev.alexnader.framed.Framed.BLOCKS;

@Mixin(BlockEntity.class)
public class BlockEntityMixin_BlockEntityMigration {
    @Unique
    private static final Map<Block, String> NEW_BLOCK_ENTITY_IDS = ImmutableMap.<Block, String>builder()
        .put(BLOCKS.BLOCK_FRAME, "framed:block_frame")
        .put(BLOCKS.STAIRS_FRAME, "framed:slab_frame")
        .put(BLOCKS.FENCE_FRAME, "framed:stairs_frame")
        .put(BLOCKS.FENCE_GATE_FRAME, "framed:fence_frame")
        .put(BLOCKS.TRAPDOOR_FRAME, "framed:fence_gate_frame")
        .put(BLOCKS.DOOR_FRAME, "framed:trapdoor_frame")
        .put(BLOCKS.PATH_FRAME, "framed:door_frame")
        .put(BLOCKS.TORCH_FRAME, "framed:path_frame")
        .put(BLOCKS.WALL_TORCH_FRAME, "framed:torch_frame")
        .put(BLOCKS.PRESSURE_PLATE_FRAME, "framed:wall_torch_frame")
        .put(BLOCKS.WALL_FRAME, "framed:pressure_plate_frame")
        .put(BLOCKS.LAYER_FRAME, "framed:wall_frame")
        .put(BLOCKS.CARPET_FRAME, "framed:layer_frame")
        .put(BLOCKS.PANE_FRAME, "framed:carpet_frame")
        .build();

    @Unique
    private static Block block;

    @Inject(method = "createFromTag", at = @At("HEAD"))
    private static void saveBlockArg(BlockState state, CompoundTag tag, CallbackInfoReturnable<BlockEntity> cir) {
        block = state.getBlock();
    }

    @ModifyVariable(method = "createFromTag", at = @At("FIELD"))
    private static String hijackId(String id) {
        if (id.equals("framed:frame")) {
            return NEW_BLOCK_ENTITY_IDS.get(block);
        } else {
            return id;
        }
    }
}
