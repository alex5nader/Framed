package dev.alexnader.framity

import dev.alexnader.framity.mod.Registerer
import dev.alexnader.framity.mod.WithId._

package object item {
  def addItems(implicit registerer: Registerer): Unit = {
    registerer.addItem(FramersHammer withId "framers_hammer", Framity.ItemGroup)
  }
}
