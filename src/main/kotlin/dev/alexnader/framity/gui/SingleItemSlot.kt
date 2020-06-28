package dev.alexnader.framity.gui

import io.github.cottonmc.cotton.gui.ValidatedSlot
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack

open class SingleItemSlot(
    inventory: Inventory,
    startIndex: Int,
    slotsWide: Int = 1,
    slotsHigh: Int = 1,
    big: Boolean = false
) : WItemSlot(inventory, startIndex, slotsWide, slotsHigh, big) {
    override fun createSlotPeer(inventory: Inventory, index: Int, x: Int, y: Int) =
        SingleSlot(inventory, index, x, y)
}

class SingleSlot(inventory: Inventory, index: Int, x: Int, y: Int) : ValidatedSlot(inventory, index, x, y) {
    override fun getMaxStackAmount() =
        1

    override fun getMaxStackAmount(stack: ItemStack?) =
        1
}