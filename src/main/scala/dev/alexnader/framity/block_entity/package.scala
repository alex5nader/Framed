package dev.alexnader.framity

import dev.alexnader.framity.block._
import dev.alexnader.framity.block.frame.FrameData
import dev.alexnader.framity.util.WithId.MakeWithId
import dev.alexnader.framity.util.WithId
import dev.alexnader.framity.util.MinecraftUtil.RegistryExt
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.util.registry.Registry
import net.minecraft.world.BlockView

package object block_entity {
  val FrameEntityType: WithId[BlockEntityType[FrameEntity]] = BlockEntityType.Builder.create[FrameEntity](
    () => new FrameEntity(FrameEntityType),
    BlockFrame, StairsFrame, FenceFrame, FenceGateFrame, TrapdoorFrame, DoorFrame
  ).build(null) withId Framity.id("frame_entity_type")

  val SlabFrameEntityType: WithId[BlockEntityType[FrameEntity]] = BlockEntityType.Builder.create[FrameEntity](
    () => new FrameEntity(SlabFrameEntityType),
    SlabFrame
  ).build(null) withId Framity.id("slab_frame_entity")

  val SlabSections = new FrameData.Sections(2)

  trait FrameEntityProvider extends BlockEntityProvider {
    override def createBlockEntity(world: BlockView): BlockEntity = new FrameEntity(FrameEntityType)
  }

  trait SlabFrameEntityProvider extends BlockEntityProvider {
    override def createBlockEntity(world: BlockView): BlockEntity = new FrameEntity(SlabFrameEntityType, SlabSections)
  }

  def registerBlockEntityTypes(): Unit = {
    Registry.BLOCK_ENTITY_TYPE.register(FrameEntityType, SlabFrameEntityType)
  }
}
