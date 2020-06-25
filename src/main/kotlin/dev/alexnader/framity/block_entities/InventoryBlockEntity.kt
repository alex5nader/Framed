package dev.alexnader.framity.block_entities

import dev.alexnader.framity.adapters.KtInventory
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.collection.DefaultedList
import kotlin.math.min

/**
 * Base class for block entities with an inventory. Handles some common
 * implementations needed to make inventory work.
 */
abstract class InventoryBlockEntity(
    type: BlockEntityType<out BlockEntity>,
    override val items: DefaultedList<ItemStack>,
    private val defaultState: BlockState
): BlockEntity(type), BlockEntityClientSerializable, KtInventory<DefaultedList<ItemStack>> {
    /**
     * Reads this inventory's contents from [tag].
     */
    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        super.fromTag(state, tag)
        this.fromClientTag(tag)
    }

    /**
     * Writes this inventory's contents to [tag].
     */
    override fun toTag(tag: CompoundTag?): CompoundTag {
        this.toClientTag(tag)
        return super.toTag(tag)
    }

    /**
     * Inventory is always usable. Override to change behaviour.
     */
    override fun canPlayerUse(player: PlayerEntity?) = true

    /**
     * Reads this inventory's contents from [tag]. Deferred to [fromTag].
     */
    override fun fromClientTag(tag: CompoundTag?) {
        this.items.clear()
        Inventories.fromTag(tag, this.items)
    }

    /**
     * Writes this inventory's contents to [tag]. Deferred to [toTag].
     */
    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        return Inventories.toTag(tag, this.items, true)
    }

    /**
     * Copies an [ItemStack] from this inventory, up to [count]. Will
     * remove from the inventory if [take] is true.
     */
    fun copyFrom(slot: Int, stack: ItemStack, count: Int, take: Boolean) {
        val newStack = stack.copy()
        val realCount = min(count, stack.count)

        newStack.count = realCount

        if (take) {
            stack.count -= realCount
        }

        this[slot] = newStack

        this.sync()
    }
}
