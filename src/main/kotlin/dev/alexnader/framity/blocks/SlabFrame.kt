package dev.alexnader.framity.blocks

import dev.alexnader.framity.SLAB_FRAME
import dev.alexnader.framity.SLAB_FRAME_ENTITY
import dev.alexnader.framity.block_entities.FrameEntity
import net.minecraft.block.*
import net.minecraft.state.StateManager
import net.minecraft.world.BlockView

class SlabFrame : SlabBlock(FRAME_SETTINGS), BlockEntityProvider {
    override fun createBlockEntity(view: BlockView?) = FrameEntity(SLAB_FRAME, SLAB_FRAME_ENTITY)

    @Suppress("unused")
    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        super.appendProperties(builder)
    }
}
