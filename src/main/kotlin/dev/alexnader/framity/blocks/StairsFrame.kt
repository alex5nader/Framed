package dev.alexnader.framity.blocks

import dev.alexnader.framity.STAIRS_FRAME
import dev.alexnader.framity.STAIRS_FRAME_ENTITY
import dev.alexnader.framity.adapters.IStairs
import dev.alexnader.framity.block_entities.FrameEntity
import dev.alexnader.framity.blocks.SlabFrame.Companion.DOWN_SHAPE
import dev.alexnader.framity.blocks.SlabFrame.Companion.UP_SHAPE
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.StairsBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.enums.BlockHalf
import net.minecraft.block.enums.StairShape
import net.minecraft.entity.EntityContext
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.IWorld

/**
 * [WaterloggableFrame] subclass for stairs frames.
 */
open class StairsFrame : WaterloggableFrame(), IStairs {
    companion object {
        /**
         * Combines [base] with other [VoxelShape]s based on value of [i].
         */
        private fun composeShape(
            i: Int, base: VoxelShape, northWest: VoxelShape, northEast: VoxelShape, southWest: VoxelShape, southEast: VoxelShape
        ): VoxelShape {
            var voxelShape: VoxelShape = base
            if (i and 1 != 0) {
                voxelShape = VoxelShapes.union(base, northWest)
            }
            if (i and 2 != 0) {
                voxelShape = VoxelShapes.union(voxelShape, northEast)
            }
            if (i and 4 != 0) {
                voxelShape = VoxelShapes.union(voxelShape, southWest)
            }
            if (i and 8 != 0) {
                voxelShape = VoxelShapes.union(voxelShape, southEast)
            }
            return voxelShape
        }

        /**
         * Generates all combinations of [base] and other [VoxelShape]s.
         */
        private fun composeShapes(
            base: VoxelShape, northWest: VoxelShape, northEast: VoxelShape, southWest: VoxelShape, southEast: VoxelShape
        ): Array<VoxelShape> =
            (0..16).map { i -> composeShape(i, base, northWest, northEast, southWest, southEast) }.toTypedArray()

        val TOP_SHAPES: Array<VoxelShape>
        val BOTTOM_SHAPES: Array<VoxelShape>

        init {
            val bottomNorthWest: VoxelShape = VoxelShapes.cuboid(0.0, 0.0, 0.0, 0.5, 0.5, 0.5)
            val bottomSouthWest: VoxelShape = VoxelShapes.cuboid(0.0, 0.0, 0.5, 0.5, 0.5, 1.0)
            val topNorthWest: VoxelShape = VoxelShapes.cuboid(0.0, 0.5, 0.0, 0.5, 1.0, 0.5)
            val topSouthWest: VoxelShape = VoxelShapes.cuboid(0.0, 0.5, 0.5, 0.5, 1.0, 1.0)
            val bottomNorthEast: VoxelShape = VoxelShapes.cuboid(0.5, 0.0, 0.0, 1.0, 0.5, 0.5)
            val bottomSouthEast: VoxelShape = VoxelShapes.cuboid(0.5, 0.0, 0.5, 1.0, 0.5, 1.0)
            val topNorthEast: VoxelShape = VoxelShapes.cuboid(0.5, 0.5, 0.0, 1.0, 1.0, 0.5)
            val topSouthEast: VoxelShape = VoxelShapes.cuboid(0.5, 0.5, 0.5, 1.0, 1.0, 1.0)

            TOP_SHAPES = composeShapes(
                UP_SHAPE,
                bottomNorthWest,
                bottomNorthEast,
                bottomSouthWest,
                bottomSouthEast
            )

            BOTTOM_SHAPES = composeShapes(
                DOWN_SHAPE,
                topNorthWest,
                topNorthEast,
                topSouthWest,
                topSouthEast
            )
        }
        val SHAPE_INDICES = arrayOf(12, 5, 3, 10, 14, 13, 7, 11, 13, 7, 11, 14, 8, 4, 1, 2, 4, 1, 2, 8)

        //
        /**
         * Determines whether or not the block to the direction [dir] of [pos] is stairs.
         * @see [net.minecraft.block.StairsBlock.method_10678]
         */
        private fun stairsNearby(state: BlockState?, view: BlockView?, pos: BlockPos?, dir: Direction?): Boolean {
            val blockState = view?.getBlockState(pos?.offset(dir))
            return !StairsBlock.isStairs(blockState) || blockState?.get(StairsBlock.FACING) != state?.get(
                StairsBlock.FACING
            ) || blockState?.get(StairsBlock.HALF) != state?.get(StairsBlock.HALF)
        }

        //
        /**
         * Calculates the shape a stair block at [pos] should have.
         * @see [net.minecraft.block.StairsBlock.method_10675]
         */
        private fun stairsShape(state: BlockState?, view: BlockView, pos: BlockPos): StairShape {
            val direction = state!!.get(StairsBlock.FACING)
            val blockState = view.getBlockState(pos.offset(direction))
            if (StairsBlock.isStairs(blockState) && state.get(StairsBlock.HALF) == blockState.get(
                    StairsBlock.HALF
                )
            ) {
                val direction2 = blockState.get(StairsBlock.FACING) as Direction
                if (direction2.axis != (state.get(StairsBlock.FACING) as Direction).axis && stairsNearby(
                        state, view, pos, direction2.opposite
                    )
                ) {
                    return if (direction2 == direction.rotateYCounterclockwise()) {
                        StairShape.OUTER_LEFT
                    } else StairShape.OUTER_RIGHT
                }
            }

            val blockState2 = view.getBlockState(pos.offset(direction.opposite))
            if (StairsBlock.isStairs(blockState2) && state.get(StairsBlock.HALF) == blockState2.get(
                    StairsBlock.HALF
                )
            ) {
                val direction3 = blockState2.get(StairsBlock.FACING) as Direction
                if (direction3.axis != (state.get(StairsBlock.FACING) as Direction).axis && stairsNearby(
                        state, view, pos, direction3
                    )
                ) {
                    return if (direction3 == direction.rotateYCounterclockwise()) {
                        StairShape.INNER_LEFT
                    } else StairShape.INNER_RIGHT
                }
            }

            return StairShape.STRAIGHT
        }
    }

    init {
        this.defaultState = this.defaultState
            .with(StairsBlock.FACING, Direction.SOUTH)
            .with(StairsBlock.HALF, BlockHalf.BOTTOM)
            .with(StairsBlock.SHAPE, StairShape.STRAIGHT)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        super.appendProperties(builder)
        builder?.add(StairsBlock.FACING)
        builder?.add(StairsBlock.HALF)
        builder?.add(StairsBlock.SHAPE)
    }

    override fun getStateForNeighborUpdate(
        state: BlockState?,
        facing: Direction?,
        neighborState: BlockState?,
        world: IWorld?,
        pos: BlockPos?,
        neighborPos: BlockPos?
    ): BlockState? {
        @Suppress("deprecation")
        return if (facing!!.axis.isHorizontal) (state!!.with(
            StairsBlock.SHAPE, stairsShape(state, world!!, pos!!)
        ) as BlockState) else super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos)
    }

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        val direction = ctx!!.side
        val blockPos = ctx.blockPos
        val blockState = super.getPlacementState(ctx)
            ?.with(StairsBlock.FACING, ctx.playerFacing)
            ?.with(StairsBlock.HALF, if (direction != Direction.DOWN && (direction == Direction.UP || ctx.hitPos.y - blockPos.y.toDouble() <= 0.5)) BlockHalf.BOTTOM else BlockHalf.TOP)
        val shape = stairsShape(blockState, ctx.world, blockPos)
        return blockState?.with(StairsBlock.SHAPE, shape)
    }

    override fun getOutlineShape(state: BlockState?, view: BlockView?, pos: BlockPos?, ePos: EntityContext?): VoxelShape? {
        val facing = state!!.get(StairsBlock.FACING)
        if (facing == Direction.UP || facing == Direction.DOWN) return VoxelShapes.empty()
        return (if (state.get(StairsBlock.HALF) == BlockHalf.TOP) TOP_SHAPES else BOTTOM_SHAPES)[SHAPE_INDICES[(state.get(StairsBlock.SHAPE) as StairShape).ordinal * 4 + (state.get(
            StairsBlock.FACING
        ) as Direction).horizontal]]
    }

    override fun hasSidedTransparency(state: BlockState?) = true

    override fun isSideInvisible(state: BlockState, neighbor: BlockState, facing: Direction?): Boolean {
        return neighbor.block == this
                && (state.get(StairsBlock.HALF) == neighbor.get(StairsBlock.HALF)
                    || state.get(StairsBlock.FACING) == neighbor.get(StairsBlock.FACING))
    }

    override fun createBlockEntity(view: BlockView?): BlockEntity = FrameEntity(STAIRS_FRAME, STAIRS_FRAME_ENTITY)
}
