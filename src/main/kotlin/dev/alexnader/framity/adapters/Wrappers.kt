package dev.alexnader.framity.adapters

import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item

/**
 * Wrapper type for objects that stores an id.
 */
data class WithId<T>(val id: String, val value: T)

/**
 * Wrapper type for [Item] subclasses which stores the item's id.
 */
data class KtItem<out I: Item>(val item: I, val id: String)

/**
 * Wrapper type for [Block] subclasses which stores the block's id.
 */
data class KtBlock<out B: Block>(val block: B, val id: String) {
    /**
     * Returns this block's [BlockItem] (if applicable).
     */
    var blockItem: BlockItem? = null
}

/**
 * Wrapper type for [BlockEntity] subclasses which stores the block entity's id.
 */
data class KtBlockEntity<E: BlockEntity>(val blockEntity: BlockEntityType<E>, val id: String)
