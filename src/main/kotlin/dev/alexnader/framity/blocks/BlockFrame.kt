package dev.alexnader.framity.blocks

import dev.alexnader.framity.*
import dev.alexnader.framity.block_entities.FrameEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.StairsBlock
import net.minecraft.block.enums.BlockHalf
import net.minecraft.block.enums.StairShape
import net.minecraft.entity.EntityContext
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.IWorld


class BlockFrame : BaseFrame() {
    override fun createBlockEntity(view: BlockView?) = FrameEntity(BLOCK_FRAME, BLOCK_FRAME_ENTITY)
}


