package dev.alexnader.framity.blocks

import dev.alexnader.framity.SLOPE_FRAME
import dev.alexnader.framity.SLOPE_FRAME_ENTITY
import dev.alexnader.framity.block_entities.FrameEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.StairsBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.state.StateManager
import net.minecraft.world.BlockView

class SlopeFrame : StairsFrame() {
    override fun createBlockEntity(view: BlockView?) = FrameEntity(SLOPE_FRAME, SLOPE_FRAME_ENTITY)
}