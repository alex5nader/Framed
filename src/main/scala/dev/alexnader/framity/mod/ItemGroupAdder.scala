package dev.alexnader.framity.mod

import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

import scala.collection.mutable.ArrayBuffer

object ItemGroupAdder {
  implicit class InItemGroup[A](withId: WithId[A]) {
    def inItemGroup(adder: ItemGroupAdder): WithId[A] = {
      adder.contents.addOne(withId.id)
      withId
    }
  }
}

class ItemGroupAdder private(val id: Identifier, val makeIcon: () => ItemStack, val contents: ArrayBuffer[Identifier]) {
  def this(path: String, makeIcon: () => ItemStack)(implicit mod: Mod) = this(mod.id(path), makeIcon, ArrayBuffer.empty)
  def this(id: Identifier, makeIcon: () => ItemStack) = this(id, makeIcon, ArrayBuffer.empty)

  def addTo(registerer: Registerer): ItemGroupAdder = {
    registerer.addItemGroup(this)
    this
  }
}
