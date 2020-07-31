package dev.alexnader.framity.gui

import io.github.cottonmc.cotton.gui.ValidatedSlot
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import net.minecraft.inventory.Inventory

class WSingleItemSlot(inventory: Inventory, startIndex: Int, slotsWide: Int = 1, slotsHigh: Int = 1, big: Boolean = false)
  extends WItemSlot(inventory, startIndex, slotsWide, slotsHigh, big) {
  override def createSlotPeer(inventory: Inventory, index: Int, x: Int, y: Int): ValidatedSlot =
    new SingleItemSlot(inventory, index, x, y)
}
