package dev.alexnader.framity.mod

import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

import scala.collection.mutable.ArrayBuffer

class ItemGroupAdder private(val makeIcon: () => ItemStack, val contents: ArrayBuffer[Identifier]) {
  def this(makeIcon: () => ItemStack) = this(makeIcon, ArrayBuffer.empty)
}
