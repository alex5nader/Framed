package dev.alexnader.framity

import dev.alexnader.framity.block.frame.{CullingFrameBlock, FrameBlock, SinglePart, SlabParts}
import dev.alexnader.framity.block_entity.{FrameEntityProvider, SlabFrameEntityProvider}
import dev.alexnader.framity.util.WithId
import dev.alexnader.framity.util.WithId.MakeWithId
import dev.alexnader.framity.util.MinecraftUtil.RegistryExt
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.mixin.`object`.builder.AbstractBlockSettingsAccessor
import net.minecraft.block.{AbstractBlock, Block, BlockState, DoorBlock, FenceBlock, FenceGateBlock, Material, SlabBlock, StairsBlock, TorchBlock, TrapdoorBlock, WallTorchBlock}
import net.minecraft.item.{BlockItem, Item}
import net.minecraft.particle.ParticleTypes
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.property.Properties
import net.minecraft.util.registry.Registry

package object block {
  private val FrameSettings: AbstractBlock.Settings = FabricBlockSettings
    .of(Material.WOOD)
    .hardness(0.33f)
    .sounds(BlockSoundGroup.WOOD)
    .nonOpaque()
    .solidBlock((_, _, _) => false)
    .lightLevel((state: BlockState) => if (state.get(Properties.LIT)) 15 else 0)

  private val TorchFrameSettings: AbstractBlock.Settings = {
    val settings = FabricBlockSettings.copyOf(FrameSettings)
      .noCollision()
      .breakInstantly()
      .lightLevel((state: BlockState) => if (state.get(Properties.LIT)) 15 else 14)
    settings.asInstanceOf[AbstractBlockSettingsAccessor].setMaterial(Material.SUPPORTED)
    settings
  }

  val BlockFrame: WithId[Block]     = new Block(FrameSettings)                                    with CullingFrameBlock with SinglePart with FrameEntityProvider     withId Framity.id("block_frame")
  val SlabFrame: WithId[Block]      = new SlabBlock(FrameSettings)                                with CullingFrameBlock with SlabParts  with SlabFrameEntityProvider withId Framity.id("slab_frame")
  val StairsFrame: WithId[Block]    = new StairsBlock(BlockFrame.getDefaultState, FrameSettings)  with FrameBlock        with SinglePart with FrameEntityProvider     withId Framity.id("stairs_frame")
  val FenceFrame: WithId[Block]     = new FenceBlock(FrameSettings)                               with FrameBlock        with SinglePart with FrameEntityProvider     withId Framity.id("fence_frame")
  val FenceGateFrame: WithId[Block] = new FenceGateBlock(FrameSettings)                           with FrameBlock        with SinglePart with FrameEntityProvider     withId Framity.id("fence_gate_frame")
  val TrapdoorFrame: WithId[Block]  = new TrapdoorBlock(FrameSettings)                            with FrameBlock        with SinglePart with FrameEntityProvider     withId Framity.id("trapdoor_frame")
  val DoorFrame: WithId[Block]      = new DoorBlock(FrameSettings)                                with FrameBlock        with SinglePart with FrameEntityProvider     withId Framity.id("door_frame")
  val PathFrame: WithId[Block]      = new PathBlock(FrameSettings)                                with CullingFrameBlock with SinglePart with FrameEntityProvider     withId Framity.id("path_frame")
  val TorchFrame: WithId[Block]     = new TorchBlock(TorchFrameSettings, ParticleTypes.FLAME)     with FrameBlock        with SinglePart with FrameEntityProvider     withId Framity.id("torch_frame")
  val WallTorchFrame: WithId[Block] = new WallTorchBlock(TorchFrameSettings, ParticleTypes.FLAME) with FrameBlock        with SinglePart with FrameEntityProvider     withId Framity.id("wall_torch_frame")

  def registerBlocks(): Seq[Item] = {
    def registerWithItem(blocks: WithId[Block]*): Seq[Item] =
      blocks map { block =>
        Registry.BLOCK.register(block)
        val item = new BlockItem(block, new Item.Settings)
        Registry.register(Registry.ITEM, block.id, item)
      }

    Registry.BLOCK.register(TorchFrame, WallTorchFrame)

    registerWithItem(BlockFrame,
      SlabFrame,
      StairsFrame,
      FenceFrame,
      FenceGateFrame,
      TrapdoorFrame,
      DoorFrame,
      PathFrame,
    )
  }
}
