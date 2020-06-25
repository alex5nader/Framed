package dev.alexnader.framity.blocks

import dev.alexnader.framity.*
import dev.alexnader.framity.block_entities.FrameEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.state.StateManager
import net.minecraft.world.BlockView

/**
 * [BaseFrame] subclass for full block frames.
 */
class BlockFrame : Block(FRAME_SETTINGS), BlockEntityProvider {
    @Suppress("unused")
    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        super.appendProperties(builder)
    }

    override fun createBlockEntity(view: BlockView?) = FrameEntity(BLOCK_FRAME, BLOCK_FRAME_ENTITY)
}
