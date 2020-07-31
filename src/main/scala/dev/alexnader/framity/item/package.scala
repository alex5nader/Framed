package dev.alexnader.framity

import dev.alexnader.framity.Framity.Mod
import dev.alexnader.framity.mod.Registerer.register
import dev.alexnader.framity.mod.WithId._

package object item {
  register item (FramersHammer withId "framers_hammer", Framity.ItemGroup)

  def init(): Unit = ()
}
