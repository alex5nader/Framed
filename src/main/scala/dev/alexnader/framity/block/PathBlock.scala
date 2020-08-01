package dev.alexnader.framity.block

import net.minecraft.block.{AbstractBlock, Block, BlockState, ShapeContext}
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView

object PathBlock {
  private val Shape = Block.createCuboidShape(0, 0, 0, 16, 15, 16)
}

class PathBlock(settings: AbstractBlock.Settings) extends Block(settings) {
  //noinspection ScalaDeprecation
  override def hasSidedTransparency(state: BlockState): Boolean = true

  //noinspection ScalaDeprecation
  override def getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape = PathBlock.Shape

  //noinspection ScalaDeprecation
  override def canPathfindThrough(state: BlockState, world: BlockView, pos: BlockPos, `type`: NavigationType): Boolean = false
}
