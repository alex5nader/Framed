package dev.alexnader.framity

import dev.alexnader.framity.util.WithId.MakeWithId
import dev.alexnader.framity.util.MinecraftUtil.RegistryExt
import dev.alexnader.framity.util.WithId
import net.minecraft.item.{Item, WallStandingBlockItem}
import net.minecraft.util.registry.Registry

package object item {
  val TorchFrameItem: WithId[Item] = new WallStandingBlockItem(block.TorchFrame, block.WallTorchFrame, new Item.Settings) withId Framity.id("torch_frame")

  def registerItems(): Seq[Item] = {
    Registry.ITEM.register(
      FramersHammer withId Framity.id("framers_hammer"),
      TorchFrameItem
    )
  }
}
