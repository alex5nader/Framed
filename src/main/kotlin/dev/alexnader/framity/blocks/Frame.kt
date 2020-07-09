package dev.alexnader.framity.blocks

import dev.alexnader.framity.util.FrameDataFormat
import net.minecraft.block.BlockState
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d

interface Frame {
    val format: FrameDataFormat
    fun getSlotFor(state: BlockState, posInBlock: Vec3d, side: Direction): Int
    fun slotIsValid(state: BlockState, slot: Int): Boolean
}
