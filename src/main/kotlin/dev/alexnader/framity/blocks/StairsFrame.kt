package dev.alexnader.framity.blocks

import dev.alexnader.framity.BLOCK_FRAME
import dev.alexnader.framity.STAIRS_FRAME
import dev.alexnader.framity.STAIRS_FRAME_ENTITY
import dev.alexnader.framity.block_entities.FrameEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.StairsBlock
import net.minecraft.state.StateManager
import net.minecraft.world.BlockView

class StairsFrame : StairsBlock(BLOCK_FRAME.value.defaultState, FRAME_SETTINGS), BlockEntityProvider {
    override fun createBlockEntity(world: BlockView?) = FrameEntity(STAIRS_FRAME, STAIRS_FRAME_ENTITY)

    @Suppress("unused")
    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        super.appendProperties(builder)
    }
}