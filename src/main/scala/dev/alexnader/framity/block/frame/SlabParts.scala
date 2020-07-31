package dev.alexnader.framity.block.frame

import dev.alexnader.framity.block.frame.FrameData.Sections.SlotSectionConversions
import dev.alexnader.framity.block_entity.FrameEntity
import net.minecraft.block.BlockState
import net.minecraft.block.enums.SlabType
import net.minecraft.state.property.Properties
import net.minecraft.util.math.{Direction, Vec3d}

object SlabParts {
  val LowerSlot = 0
  val UpperSlot = 1
}

trait SlabParts extends FrameAccess {
  import SlabParts.{LowerSlot, UpperSlot}

  override def getRelativeSlotAt(state: BlockState, posInBlock: Vec3d, side: Direction): Int = side match {
    case Direction.UP => if (posInBlock.y == 0.5) LowerSlot else UpperSlot
    case Direction.DOWN => if (posInBlock.y == 0.5) UpperSlot else LowerSlot
    case _ => if (posInBlock.y < 0.5) LowerSlot else UpperSlot
  }

  override def absoluteSlotIsValid(frameEntity: FrameEntity, state: BlockState, slot: Int): Boolean = {
    val wantedSlot = state.get(Properties.SLAB_TYPE) match {
      case SlabType.DOUBLE => return true
      case SlabType.TOP => UpperSlot
      case SlabType.BOTTOM => LowerSlot
    }
    frameEntity.sections.findSectionIndexOf(slot) match {
      case FrameData.Sections.BaseIndex => slot.toAbsolute(frameEntity.sections.base) == wantedSlot
      case FrameData.Sections.OverlayIndex => slot.toAbsolute(frameEntity.sections.overlay) == wantedSlot
      case FrameData.Sections.SpecialIndex => true
      case _ => throw new IllegalArgumentException(s"Invalid slot for slab frame: $slot")
    }
  }
}
