package dev.alexnader.framity

import dev.alexnader.framity.Framity.Mod
import dev.alexnader.framity.block._
import dev.alexnader.framity.block.frame.FrameData
import dev.alexnader.framity.mod.Registerer.register
import dev.alexnader.framity.mod.WithId
import dev.alexnader.framity.mod.WithId._
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.world.BlockView

package object block_entity {
  val FrameEntityType: WithId[BlockEntityType[FrameEntity]] = BlockEntityType.Builder.create[FrameEntity](
    () => new FrameEntity(FrameEntityType),
    BlockFrame, StairsFrame, FenceFrame, FenceGateFrame, TrapdoorFrame, DoorFrame
  ).build(null) withId "frame_entity_type"

  val SlabFrameEntityType: WithId[BlockEntityType[FrameEntity]] = BlockEntityType.Builder.create[FrameEntity](
    () => new FrameEntity(SlabFrameEntityType),
    SlabFrame
  ).build(null) withId "slab_frame_entity"

  val SlabSections = new FrameData.Sections(2)

  trait FrameEntityProvider extends BlockEntityProvider {
    override def createBlockEntity(world: BlockView): BlockEntity = new FrameEntity(FrameEntityType)
  }

  trait SlabFrameEntityProvider extends BlockEntityProvider {
    override def createBlockEntity(world: BlockView): BlockEntity = new FrameEntity(SlabFrameEntityType, SlabSections)
  }

  register blockEntityType FrameEntityType
  register blockEntityType SlabFrameEntityType

  def init(): Unit = ()
}
