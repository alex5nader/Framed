package dev.alexnader.framity.block_entities

import dev.alexnader.framity.adapters.KtInventory
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.DefaultedList
import kotlin.math.min

abstract class InventoryBlockEntity(
    type: BlockEntityType<out BlockEntity>, override val items: DefaultedList<ItemStack>
): BlockEntity(type), BlockEntityClientSerializable, KtInventory<DefaultedList<ItemStack>> {
    override fun fromTag(tag: CompoundTag?) {
        super.fromTag(tag)
        this.items.clear()
        Inventories.fromTag(tag, this.items)
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        Inventories.toTag(tag, this.items, true)
        return super.toTag(tag)
    }

    override fun canPlayerUseInv(player: PlayerEntity?) = true

    override fun fromClientTag(tag: CompoundTag?) = this.fromTag(tag)
    override fun toClientTag(tag: CompoundTag?) = this.toTag(tag)

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