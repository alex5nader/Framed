package dev.alexnader.framity.gui.dsl

import io.github.cottonmc.cotton.gui.ValidatedSlot
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack

object Slot {
  object slot extends SingleSlotBuilder

  trait SingleSlotBuilder { outer =>
    def filter(stack: ItemStack): Boolean = true
    def createSlotPeer(inventory: Inventory, index: Int, x: Int, y: Int): ValidatedSlot = new ValidatedSlot(inventory, index, x, y)

    class LinkedTo(inventory: Inventory) {
      def at(_slot: Int): WItemSlot = {
        val slot = new WItemSlot(inventory, _slot, 1, 1, false) {
          override def createSlotPeer(inventory: Inventory, index: Int, x: Int, y: Int): ValidatedSlot = outer.createSlotPeer(inventory, index, x, y)
        }
        slot.setFilter(stack => outer.filter(stack))
        slot
      }
    }

    def using(_createSlotPeer: (Inventory, Int, Int, Int) => ValidatedSlot): SingleSlotBuilder = new SingleSlotBuilder {
      override def filter(stack: ItemStack): Boolean = outer.filter(stack)
      override def createSlotPeer(inventory: Inventory, index: Int, x: Int, y: Int): ValidatedSlot = _createSlotPeer(inventory, index, x, y)
    }

    def withFilter(_filter: ItemStack => Boolean): SingleSlotBuilder = new SingleSlotBuilder {
      override def filter(stack: ItemStack): Boolean = _filter(stack)
      override def createSlotPeer(inventory: Inventory, index: Int, x: Int, y: Int): ValidatedSlot = outer.createSlotPeer(inventory, index, x,  y)
    }

    def linkedTo(inventory: Inventory): LinkedTo = new LinkedTo(inventory)
  }

  object slotRow extends SlotRowBuilder

  trait SlotRowBuilder { outer =>
    def filter(stack: ItemStack): Boolean = true
    def createSlotPeer(inventory: Inventory, index: Int, x: Int, y: Int): ValidatedSlot = new ValidatedSlot(inventory, index, x, y)

    class LinkedTo(inventory: Inventory) {
      def makeSlot(from: Int, to: Int): WItemSlot = {
        val slot = new WItemSlot(inventory, from, to - from, 1, false) {
          override def createSlotPeer(inventory: Inventory, index: Int, x: Int, y: Int): ValidatedSlot = outer.createSlotPeer(inventory, index, x, y)
        }
        slot.setFilter(stack => outer.filter(stack))
        slot
      }

      class From(from: Int) {
        def to(to: Int): WItemSlot = makeSlot(from, to)
      }

      def from(from: Int): From = new From(from)

      def overRange(range: Range): WItemSlot = makeSlot(range.start, range.end)
    }

    def using(_createSlotPeer: (Inventory, Int, Int, Int) => ValidatedSlot): SlotRowBuilder = new SlotRowBuilder {
      override def filter(stack: ItemStack): Boolean = outer.filter(stack)
      override def createSlotPeer(inventory: Inventory, index: Int, x: Int, y: Int): ValidatedSlot = _createSlotPeer(inventory, index, x, y)
    }

    def withFilter(_filter: ItemStack => Boolean): SlotRowBuilder = new SlotRowBuilder {
      override def filter(stack: ItemStack): Boolean = _filter(stack)
      override def createSlotPeer(inventory: Inventory, index: Int, x: Int, y: Int): ValidatedSlot = outer.createSlotPeer(inventory, index, x,  y)
    }

    def linkedTo(inventory: Inventory): LinkedTo = new LinkedTo(inventory)
  }
}
