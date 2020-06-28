package dev.alexnader.framity.util

import dev.alexnader.framity.block_entities.FrameEntity
import dev.alexnader.framity.data.hasOverlay
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.ShapeContext
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.World

fun validForOther(stack: ItemStack) =
    stack.item in FrameEntity.OTHER_ITEM_DATA

fun validForOverlay(stack: ItemStack) =
    hasOverlay(stack)

fun validForBase(stack: ItemStack, toState: (BlockItem) -> BlockState, world: World, pos: BlockPos): BlockState? {
    val item = stack.item
    if (item == null || item !is BlockItem) {
        return null
    }

    if (FramesTag.contains(item.block)) {
        return null
    }

    val state = toState(item)

    return if (validStateForBase(state, world, pos)) {
        state
    } else {
        null
    }
}

fun validStateForBase(state: BlockState, world: World, pos: BlockPos): Boolean {
    @Suppress("deprecation")
    if (state.block is BlockWithEntity && state.block.getRenderType(state) != BlockRenderType.MODEL) {
        return false
    }

    @Suppress("deprecation")
    val outlineShape = state.block.getOutlineShape(state, world, pos, ShapeContext.absent())

    if (VoxelShapes.fullCube().boundingBoxes != outlineShape.boundingBoxes) {
        return false
    }

    return true
}