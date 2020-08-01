package dev.alexnader.framity

import dev.alexnader.framity.Framity.Mod
import dev.alexnader.framity.block.frame.{CullingFrameBlock, FrameBlock, SinglePart, SlabParts}
import dev.alexnader.framity.block_entity.{FrameEntityProvider, SlabFrameEntityProvider}
import dev.alexnader.framity.mod.{Registerer, WithId}
import dev.alexnader.framity.mod.WithId._
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.{Block, BlockState, DoorBlock, FenceBlock, FenceGateBlock, Material, SlabBlock, StairsBlock, TrapdoorBlock}
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.property.Properties

package object block {
  private val FrameSettings = FabricBlockSettings
    .of(Material.WOOD)
    .hardness(0.33f)
    .sounds(BlockSoundGroup.WOOD)
    .nonOpaque()
    .solidBlock((_, _, _) => false)
    .lightLevel((state: BlockState) => if (state.get(Properties.LIT)) 15 else 0)

  val BlockFrame: WithId[Block] = new Block(FrameSettings) with CullingFrameBlock with SinglePart with FrameEntityProvider withId "block_frame"
  val SlabFrame: WithId[Block] = new SlabBlock(FrameSettings) with CullingFrameBlock with SlabParts with SlabFrameEntityProvider withId "slab_frame"
  val StairsFrame: WithId[Block] = new StairsBlock(BlockFrame.getDefaultState, FrameSettings) with FrameBlock with SinglePart with FrameEntityProvider withId "stairs_frame"
  val FenceFrame: WithId[Block] = new FenceBlock(FrameSettings) with FrameBlock with SinglePart with FrameEntityProvider withId "fence_frame"
  val FenceGateFrame: WithId[Block] = new FenceGateBlock(FrameSettings) with FrameBlock with SinglePart with FrameEntityProvider withId "fence_gate_frame"
  val TrapdoorFrame: WithId[Block] = new TrapdoorBlock(FrameSettings) with FrameBlock with SinglePart with FrameEntityProvider withId "trapdoor_frame"
  val DoorFrame: WithId[Block] = new DoorBlock(FrameSettings) with FrameBlock with SinglePart with FrameEntityProvider withId "door_frame"
  val PathFrame: WithId[Block] = new PathBlock(FrameSettings) with FrameBlock with SinglePart with FrameEntityProvider withId "path_frame"

  def addBlocks(implicit registerer: Registerer): Unit = {
    registerer.addBlockWithItem(BlockFrame, Framity.ItemGroup)
    registerer.addBlockWithItem(SlabFrame, Framity.ItemGroup)
    registerer.addBlockWithItem(StairsFrame, Framity.ItemGroup)
    registerer.addBlockWithItem(FenceFrame, Framity.ItemGroup)
    registerer.addBlockWithItem(FenceGateFrame, Framity.ItemGroup)
    registerer.addBlockWithItem(TrapdoorFrame, Framity.ItemGroup)
    registerer.addBlockWithItem(DoorFrame, Framity.ItemGroup)
    registerer.addBlockWithItem(PathFrame, Framity.ItemGroup)
  }
}
