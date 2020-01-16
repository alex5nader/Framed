package dev.alexnader.framity.blocks

import dev.alexnader.framity.*
import dev.alexnader.framity.block_entities.FrameEntity
import net.minecraft.world.BlockView

/**
 * [BaseFrame] subclass for full block frames.
 */
class BlockFrame : BaseFrame() {
    override fun createBlockEntity(view: BlockView?) = FrameEntity(BLOCK_FRAME, BLOCK_FRAME_ENTITY)
}
