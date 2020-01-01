package dev.alexnader.framity.blocks

import dev.alexnader.framity.FENCE_FRAME
import dev.alexnader.framity.FENCE_FRAME_ENTITY
import dev.alexnader.framity.block_entities.FrameEntity
import net.minecraft.world.BlockView

class FenceFrame : BaseFrame() {
    companion object {

    }

    override fun createBlockEntity(view: BlockView?) = FrameEntity(FENCE_FRAME, FENCE_FRAME_ENTITY)
}
