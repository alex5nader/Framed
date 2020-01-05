package dev.alexnader.framity.blocks

import dev.alexnader.framity.FENCE_FRAME
import dev.alexnader.framity.FENCE_FRAME_ENTITY
import dev.alexnader.framity.adapters.buildMesh
import dev.alexnader.framity.block_entities.FrameEntity
import dev.alexnader.framity.model.cube
import dev.alexnader.framity.model.cube16
import net.fabricmc.fabric.api.renderer.v1.Renderer
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.FenceGateBlock
import net.minecraft.block.HorizontalConnectedBlock
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.tag.BlockTags
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.IWorld

class FenceFrame : HorizontalConnectedFrame(CENTER_GEOMETRY, NORTH_GEOMETRY, CENTER_COLLISION, NORTH_COLLISION) {
    companion object {
        val CENTER_GEOMETRY: VoxelShape = VoxelShapes.cuboid(0.375, 0.0, 0.375, 0.625, 1.0, 0.625)
        val NORTH_GEOMETRY: VoxelShape = VoxelShapes.union(
            VoxelShapes.cuboid(0.4375, 0.75, 0.0, 0.5625, 0.9375, 0.5625),
            VoxelShapes.cuboid(0.4375, 0.375, 0.0, 0.5625, 0.5625, 0.5625)
        )
        val CENTER_COLLISION: VoxelShape = VoxelShapes.cuboid(0.375, 0.0, 0.375, 0.625, 1.5, 0.625)
        val NORTH_COLLISION: VoxelShape = VoxelShapes.union(
            VoxelShapes.cuboid(0.4375, 0.75, 0.0, 0.5625, 1.5, 0.5625),
            VoxelShapes.cuboid(0.4375, 0.375, 0.0, 0.5625, 0.5625, 0.5625)
        )

        fun getItemMesh(renderer: Renderer) = renderer.buildMesh { qe ->
            qe.cube16(6f, 0f, 0f, 10f, 16f, 4f, -1)
                .cube16(6f, 0f, 12f, 10f, 16f, 16f, -1)
                .cube16(7f, 13f, -2f, 9f, 15f, 18f, -1)
                .cube16(7f, 5f, -2f, 9f, 7f, 18f, -1)
        }
    }

    init {
        this.defaultState = this.defaultState.with(Properties.UP, true)
    }

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        val world = ctx!!.world
        val pos = ctx.blockPos
        val northPos = pos.north()
        val eastPos = pos.east()
        val southPos = pos.south()
        val westPos = pos.west()
        val northState = world.getBlockState(northPos)
        val eastState = world.getBlockState(eastPos)
        val southState = world.getBlockState(southPos)
        val westState = world.getBlockState(westPos)
        return super.getPlacementState(ctx)
            ?.with(HorizontalConnectedBlock.NORTH, this.canConnect(northState, northState.isSideSolidFullSquare(world, northPos, Direction.SOUTH), Direction.SOUTH))
            ?.with(HorizontalConnectedBlock.EAST, this.canConnect(eastState, eastState.isSideSolidFullSquare(world, eastPos, Direction.WEST), Direction.WEST))
            ?.with(HorizontalConnectedBlock.SOUTH, this.canConnect(southState, southState.isSideSolidFullSquare(world, southPos, Direction.NORTH), Direction.NORTH))
            ?.with(HorizontalConnectedBlock.WEST, this.canConnect(westState, westState.isSideSolidFullSquare(world, westPos, Direction.EAST), Direction.EAST))
    }

    private fun canConnect(state: BlockState?, neighborIsFullSquare: Boolean?, dir: Direction): Boolean {
        return !canConnect(state?.block)
                && (neighborIsFullSquare == true)
                || (state?.block?.matches(BlockTags.FENCES) == true && state.material == this.material)
                || (state?.block is FenceGateBlock && FenceGateBlock.canWallConnect(state, dir))
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        super.appendProperties(builder)
        builder?.add(Properties.UP)
        builder?.add(HorizontalConnectedBlock.NORTH)
        builder?.add(HorizontalConnectedBlock.EAST)
        builder?.add(HorizontalConnectedBlock.SOUTH)
        builder?.add(HorizontalConnectedBlock.WEST)
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
        val newState = super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos)

        return if (facing?.axis?.type == Direction.Type.HORIZONTAL) {
            newState?.with(FACING_PROPERTIES[facing], this.canConnect(neighborState, neighborState?.isSideSolidFullSquare(world, neighborPos, facing.opposite), facing.opposite))
        } else {
            newState
        }
    }

    override fun createBlockEntity(view: BlockView?) = FrameEntity(FENCE_FRAME, FENCE_FRAME_ENTITY)
}
