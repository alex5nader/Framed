package dev.alexnader.framity.block.frame
import dev.alexnader.framity.block_entity.FrameEntity
import net.minecraft.block.BlockState
import net.minecraft.util.math.{Direction, Vec3d}

trait SinglePart extends FrameAccess {
  override def getRelativeSlotAt(state: BlockState, posInBlock: Vec3d, side: Direction): Int = 0

  override def absoluteSlotIsValid(frameEntity: FrameEntity, state: BlockState, slot: Int): Boolean = frameEntity.sections containsSlot slot
}
