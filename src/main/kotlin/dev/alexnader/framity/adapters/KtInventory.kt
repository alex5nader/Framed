package dev.alexnader.framity.adapters

import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import kotlin.math.max

interface KtInventory<I: MutableList<ItemStack>>: Inventory {
    val items: I

    operator fun get(slot: Int) = this.items[slot]
    operator fun set(slot: Int, stack: ItemStack) {
        this.items[slot] = stack
    }

    val capacity get() = this.items.size
    val count get() = this.items.filter { !it.isEmpty }.size
    val isEmpty get() = this.items.all { it.isEmpty }

    override fun getInvSize() = this.capacity
    override fun isInvEmpty() = this.isEmpty
    override fun getInvStack(slot: Int) = this[slot]

    override fun takeInvStack(slot: Int, amount: Int): ItemStack {
        val result = Inventories.splitStack(this.items, slot, amount)

        if (!result.isEmpty) this.markDirty()

        return result
    }

    override fun removeInvStack(slot: Int): ItemStack {
        this.markDirty()

        return Inventories.removeStack(this.items, slot)
    }

    override fun setInvStack(slot: Int, stack: ItemStack?) {
        this[slot] = stack!!

        stack.count = max(stack.count, this.invMaxStackAmount)

        this.markDirty()
    }

    override fun clear() {
        this.items.clear()
    }
}