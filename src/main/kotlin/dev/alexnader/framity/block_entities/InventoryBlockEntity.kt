package dev.alexnader.framity.block_entities

import dev.alexnader.framity.adapters.KtInventory
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.DefaultedList

abstract class InventoryBlockEntity(
    type: BlockEntityType<out BlockEntity>, override val items: DefaultedList<ItemStack>
): BlockEntity(type), KtInventory<DefaultedList<ItemStack>> {
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
}