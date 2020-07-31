package dev.alexnader.framity.block.frame

import dev.alexnader.framity.data.overlay.ItemStackOverlayQuery
import net.minecraft.block.{BlockEntityProvider, BlockRenderType, BlockState}
import net.minecraft.item.{BlockItem, ItemStack}
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.World

object Valid {
  implicit class ItemStackValidQuery(val stack: ItemStack) {
    def isValidForSpecial: Boolean = SpecialItems.map contains stack.getItem
    def isValidForOverlay: Boolean = stack.hasOverlay
    def isValidForBase(toState: BlockItem => Option[BlockState], world: World, pos: BlockPos): Option[BlockState] = {
      stack.getItem match {
        case item: BlockItem =>
          if (item.getBlock.isInstanceOf[FrameBlock]) {
            None
          } else {
            toState(item).filter(_.isValidForBase(world, pos))
          }
        case _ => None
      }
    }
  }

  implicit class BlockStateValidQuery(val state: BlockState) {
    def isValidForBase(world: World, pos: BlockPos): Boolean = {
      if (state.getBlock.isInstanceOf[BlockEntityProvider] && state.getRenderType != BlockRenderType.MODEL) {
        return false
      }

      val outlineShape = state.getOutlineShape(world, pos)

      outlineShape.getBoundingBoxes == VoxelShapes.fullCube().getBoundingBoxes
    }
  }
}
