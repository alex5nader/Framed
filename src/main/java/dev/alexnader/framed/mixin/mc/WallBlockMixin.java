package dev.alexnader.framed.mixin.mc;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallBlock;
import net.minecraft.block.enums.WallShape;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

import static net.minecraft.block.WallBlock.NORTH_SHAPE;

@Mixin(WallBlock.class)
public class WallBlockMixin extends Block {
    @Shadow @Final public static BooleanProperty UP;

    @Shadow @Final public static EnumProperty<WallShape> EAST_SHAPE;

    @Shadow @Final public static EnumProperty<WallShape> WEST_SHAPE;

    @Shadow @Final public static EnumProperty<WallShape> SOUTH_SHAPE;

    @SuppressWarnings("unused")
    public WallBlockMixin(Settings settings) {
        super(settings);
        throw new IllegalStateException("Mixin constructor should not run.");
    }

    @SuppressWarnings("unused")
    @Shadow
    private static VoxelShape method_24426(VoxelShape voxelShape, WallShape wallShape, VoxelShape voxelShape2, VoxelShape voxelShape3) {
        throw new IllegalStateException("Shadow method should not run.");
    }

    /**
     * @author Alex Habich
     * @reason Vanilla's implementation is hard-coded to only vary the properties that vanilla walls have.
     */
    @SuppressWarnings("DuplicatedCode") // modified from vanilla
    @Overwrite
    private Map<BlockState, VoxelShape> getShapeMap(float f, float g, float h, float i, float j, float k) {
        float l = 8.0F - f;
        float m = 8.0F + f;
        float n = 8.0F - g;
        float o = 8.0F + g;
        VoxelShape voxelShape = Block.createCuboidShape(l, 0.0D, l, m, h, m);
        VoxelShape voxelShape2 = Block.createCuboidShape(n, i, 0.0D, o, j, o);
        VoxelShape voxelShape3 = Block.createCuboidShape(n, i, n, o, j, 16.0D);
        VoxelShape voxelShape4 = Block.createCuboidShape(0.0D, i, n, o, j, o);
        VoxelShape voxelShape5 = Block.createCuboidShape(n, i, n, 16.0D, j, o);
        VoxelShape voxelShape6 = Block.createCuboidShape(n, i, 0.0D, o, k, o);
        VoxelShape voxelShape7 = Block.createCuboidShape(n, i, n, o, k, 16.0D);
        VoxelShape voxelShape8 = Block.createCuboidShape(0.0D, i, n, o, k, o);
        VoxelShape voxelShape9 = Block.createCuboidShape(n, i, n, 16.0D, k, o);
        ImmutableMap.Builder<BlockState, VoxelShape> builder = ImmutableMap.builder();

        for (BlockState state : stateManager.getStates()) {
            Boolean boolean_ = state.get(UP);
            WallShape wallShape = state.get(EAST_SHAPE);
            WallShape wallShape2 = state.get(NORTH_SHAPE);
            WallShape wallShape3 = state.get(WEST_SHAPE);
            WallShape wallShape4 = state.get(SOUTH_SHAPE);

            VoxelShape voxelShape10 = VoxelShapes.empty();
            voxelShape10 = method_24426(voxelShape10, wallShape, voxelShape5, voxelShape9);
            voxelShape10 = method_24426(voxelShape10, wallShape3, voxelShape4, voxelShape8);
            voxelShape10 = method_24426(voxelShape10, wallShape2, voxelShape2, voxelShape6);
            voxelShape10 = method_24426(voxelShape10, wallShape4, voxelShape3, voxelShape7);
            if (Boolean.TRUE.equals(boolean_)) {
                voxelShape10 = VoxelShapes.union(voxelShape10, voxelShape);
            }

            builder.put(state, voxelShape10);
        }
        return builder.build();
    }
}
