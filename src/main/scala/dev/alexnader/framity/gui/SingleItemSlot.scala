package dev.alexnader.framity.gui

import io.github.cottonmc.cotton.gui.ValidatedSlot
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack

object SingleItemSlot extends ((Inventory, Int, Int, Int) => SingleItemSlot) {
  override def apply(v1: Inventory, v2: Int, v3: Int, v4: Int): SingleItemSlot = new SingleItemSlot(v1, v2, v3, v4)
}

class SingleItemSlot(inventory: Inventory, index: Int, x: Int, y: Int) extends ValidatedSlot(inventory, index, x, y) {
  override def getMaxStackAmount: Int = 1

  override def getMaxStackAmount(stack: ItemStack): Int = 1
}
