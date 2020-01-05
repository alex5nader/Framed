package dev.alexnader.framity.blocks

import dev.alexnader.framity.SLAB_FRAME
import dev.alexnader.framity.SLAB_FRAME_ENTITY
import dev.alexnader.framity.block_entities.FrameEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Waterloggable
import net.minecraft.entity.EntityContext
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView

class SlabFrame : WaterloggableFrame() {
    companion object {
        val NORTH_SHAPE: VoxelShape = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 1.0, 0.5)
        val SOUTH_SHAPE: VoxelShape = VoxelShapes.cuboid(0.0, 0.0, 0.5, 1.0, 1.0, 1.0)
        val EAST_SHAPE: VoxelShape = VoxelShapes.cuboid(0.5, 0.0, 0.0, 1.0, 1.0, 1.0)
        val WEST_SHAPE: VoxelShape = VoxelShapes.cuboid(0.0, 0.0, 0.0, 0.5, 1.0, 1.0)
        val DOWN_SHAPE: VoxelShape = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.5, 1.0)
        val UP_SHAPE: VoxelShape = VoxelShapes.cuboid(0.0, 0.5, 0.0, 1.0, 1.0, 1.0)
    }

    init {
        this.defaultState = this.defaultState
            .with(Properties.FACING, Direction.DOWN)
    }

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        val blockPos = ctx!!.blockPos
        val existingState = ctx.world?.getBlockState(blockPos)
        return if (existingState?.block == this.defaultState.block) {
            existingState?.with(Properties.FACING, existingState.get(Properties.FACING).opposite)
        } else {
            val hitPos = ctx.hitPos

            val (hPos, vPos) = when (ctx.side!!) {
                Direction.NORTH -> Pair(hitPos.x - blockPos.x, hitPos.y - blockPos.y)
                Direction.SOUTH -> Pair(1 - (hitPos.x - blockPos.x), hitPos.y - blockPos.y)
                Direction.EAST -> Pair(hitPos.z - blockPos.z, hitPos.y - blockPos.y)
                Direction.WEST -> Pair(1 - (hitPos.z - blockPos.z), hitPos.y - blockPos.y)
                Direction.UP -> Pair(hitPos.x - blockPos.x, hitPos.z - blockPos.z)
                Direction.DOWN -> Pair(1 - (hitPos.x - blockPos.x), 1 - (hitPos.z - blockPos.z))
            }

            val state = this.defaultState.with(Properties.WATERLOGGED, ctx.world.getFluidState(blockPos).fluid == Fluids.WATER)

            when {
                // center, ez
                0.33 <= hPos && hPos < 0.67 && 0.33 <= vPos && vPos < 0.67 -> state.with(Properties.FACING, ctx.side.opposite)
                // bottom
                hPos > vPos && (1 - hPos) > vPos -> state.with(
                    Properties.FACING, when (ctx.side!!) {
                        Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST -> Direction.DOWN
                        Direction.UP -> Direction.NORTH
                        Direction.DOWN -> Direction.SOUTH
                    })
                // top
                hPos < vPos && (1 - hPos) < vPos -> state.with(
                    Properties.FACING, when (ctx.side!!) {
                        Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST -> Direction.UP
                        Direction.UP -> Direction.SOUTH
                        Direction.DOWN -> Direction.NORTH
                    })
                // left
                vPos > hPos && (1 - vPos) > hPos -> state.with(
                    Properties.FACING, when (ctx.side!!) {
                        Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST -> ctx.side.rotateYCounterclockwise()
                        Direction.UP -> Direction.WEST
                        Direction.DOWN -> Direction.EAST
                    })
                // right
                vPos < hPos && (1 - vPos) < hPos -> state.with(
                    Properties.FACING, when (ctx.side!!) {
                        Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST -> ctx.side.rotateYClockwise()
                        Direction.UP -> Direction.EAST
                        Direction.DOWN -> Direction.WEST
                    })
                else -> null
            }
        }
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        super.appendProperties(builder)
        builder?.add(Properties.FACING)
    }

    override fun getOutlineShape(state: BlockState?, view: BlockView?, pos: BlockPos?, ePos: EntityContext?) =
        when (state?.get(Properties.FACING)!!) {
            Direction.NORTH -> NORTH_SHAPE
            Direction.SOUTH -> SOUTH_SHAPE
            Direction.EAST -> EAST_SHAPE
            Direction.WEST -> WEST_SHAPE
            Direction.DOWN -> DOWN_SHAPE
            Direction.UP -> UP_SHAPE
        }

    override fun hasSidedTransparency(state: BlockState?) = true

    override fun createBlockEntity(view: BlockView?) = FrameEntity(SLAB_FRAME, SLAB_FRAME_ENTITY)

    override fun isSideInvisible(state: BlockState, neighbor: BlockState, facing: Direction?): Boolean {
        if (neighbor.block != this)
            return false
        val selfFacing = state.get(Properties.FACING)
        val otherFacing = neighbor.get(Properties.FACING)
        return selfFacing == facing && selfFacing.opposite == otherFacing
    }
}
