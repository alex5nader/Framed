package dev.alexnader.framity.block.frame

import net.minecraft.block.BlockState
import net.minecraft.util.math.Direction

trait CullingFrameBlock extends FrameBlock {
  //noinspection ScalaDeprecation
  override def isSideInvisible(state: BlockState, stateFrom: BlockState, direction: Direction): Boolean = {
    super.isSideInvisible(state, stateFrom, direction) || (state == stateFrom)
  }
}
