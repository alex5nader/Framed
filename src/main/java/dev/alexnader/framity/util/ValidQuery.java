package dev.alexnader.framity.util;

import dev.alexnader.framity.block.frame.Frame;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Function;

import static dev.alexnader.framity.Framity.OVERLAYS;
import static dev.alexnader.framity.Framity.SPECIAL_ITEMS;

public class ValidQuery {
    private ValidQuery() {}

    public static class ItemStackValidQuery {
        private final ItemStack stack;

        public ItemStackValidQuery(final ItemStack stack) {
            this.stack = stack;
        }

        public Optional<BlockState> isValidForBase(final Function<BlockItem, Optional<BlockState>> toState, final World world, final BlockPos pos) {
            final Item item = stack.getItem();

            if (item instanceof BlockItem) {
                final BlockItem blockItem = (BlockItem) item;
                if (blockItem.getBlock() instanceof Frame) {
                    return Optional.empty();
                } else {
                    return toState.apply(blockItem).filter(b -> checkIf(b).isValidForBase(world, pos));
                }
            } else {
                return Optional.empty();
            }
        }

        public boolean isValidForOverlay() {
            return OVERLAYS.hasOverlay(stack);
        }

        public boolean isValidForSpecial() {
            return SPECIAL_ITEMS.MAP.containsKey(stack.getItem());
        }
    }

    public static class BlockStateValidQuery {
        private final BlockState state;

        public BlockStateValidQuery(final BlockState state) {
            this.state = state;
        }

        public boolean isValidForBase(final World world, final BlockPos pos) {
            if (state.getBlock() instanceof BlockEntityProvider && state.getRenderType() != BlockRenderType.MODEL) {
                return false;
            }

            return state.getOutlineShape(world, pos).getBoundingBoxes().equals(VoxelShapes.fullCube().getBoundingBoxes());
        }
    }

    public static ItemStackValidQuery checkIf(final ItemStack stack) {
        return new ItemStackValidQuery(stack);
    }

    public static BlockStateValidQuery checkIf(final BlockState state) {
        return new BlockStateValidQuery(state);
    }
}
