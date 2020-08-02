package dev.alexnader.framity

import dev.alexnader.framity.mod.Registerer
import dev.alexnader.framity.mod.WithId._
import net.minecraft.item.{Item, WallStandingBlockItem}

package object item {
  def addItems(implicit registerer: Registerer): Unit = {
    registerer.addItem(FramersHammer withId "framers_hammer", Framity.ItemGroup)

    registerer.addItem(new WallStandingBlockItem(block.TorchFrame, block.WallTorchFrame, new Item.Settings) withId "torch_frame", Framity.ItemGroup)
  }
}
