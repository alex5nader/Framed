package dev.alexnader.framity.util

import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import kotlin.math.max

/**
 * Wrapper interface for [Inventory] which provides default behaviour and adds some kotlin-idiomatic accessors.
 */
interface KtInventory<I: MutableList<ItemStack>>: Inventory {
    /**
     * The items contained in this inventory.
     */
    val items: I

    /**
     * Gets an ItemStack from this inventory by index.
     */
    operator fun get(slot: Int) = this.items[slot]

    /**
     * Sets an ItemStack in this inventory by index.
     */
    operator fun set(slot: Int, stack: ItemStack) {
        this.items[slot] = stack
    }

    /**
     * This inventory's number of slots, including empty.
     */
    val capacity get() = this.items.size
    /**
     * Number of slots in this inventory which are not empty.
     */
    val count get() = this.items.filter { !it.isEmpty }.size

    /**
     * [Inventory] implementation wrapping [capacity]
     */
    override fun size() = this.capacity

    /**
     * [Inventory] implementation wrapping [isEmpty]
     */
    override fun isEmpty() = this.items.all { it.isEmpty }

    /**
     * [Inventory] implementation wrapping [get]
     */
    override fun getStack(slot: Int) = this[slot]

    /**
     * [Inventory] implementation for splitting a stack in this inventory by index.
     */
    override fun removeStack(slot: Int, amount: Int): ItemStack {
        val result = Inventories.splitStack(this.items, slot, amount)

        if (!result.isEmpty) this.markDirty()

        return result
    }

    /**
     * [Inventory] implementation for removing a stack from this inventory by index.
     */
    override fun removeStack(slot: Int): ItemStack {
        this.markDirty()

        return Inventories.removeStack(this.items, slot)
    }

    /**
     * [Inventory] implementation for setting a stack from this inventory by index.
     */
    override fun setStack(slot: Int, stack: ItemStack?) {
        this[slot] = stack!!

        stack.count = max(stack.count, this.maxCountPerStack)

        this.markDirty()
    }

    /**
     * [Inventory] implementation which clears this inventory.
     */
    override fun clear() {
        this.items.clear()
    }
}
