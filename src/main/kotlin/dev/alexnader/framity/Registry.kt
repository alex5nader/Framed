package dev.alexnader.framity

import dev.alexnader.framity.adapters.KtBlock
import dev.alexnader.framity.adapters.KtBlockEntity
import dev.alexnader.framity.adapters.KtItem
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.util.function.Supplier

/**
 * Auto-registration manager for a mod.
 */
class Mod(private val id: String) {
    private val items = ArrayList<KtItem<Item>>()
    private val blocks = ArrayList<KtBlock<Block>>()
    private val blockEntities = ArrayList<KtBlockEntity<*>>()

    private lateinit var creativeTabIcon: KtBlock<Block>
    private lateinit var creativeTabItem: Item

    /**
     * Marks the given block as used for the mod's creative tab.
     */
    fun <B: Block> creativeTab(ktBlock: KtBlock<B>) {
        creativeTabIcon = ktBlock
    }

    /**
     * Creates a new item from [constructor].
     */
    fun <I: Item> item(constructor: () -> I, name: String): KtItem<I> {
        val newItem = KtItem(constructor(), name)
        items.add(newItem)
        return newItem
    }

    /**
     * Creates a new block from [constructor].
     */
    fun <B: Block> block(constructor: () -> B, name: String): KtBlock<B> {
        val newBlock = KtBlock(constructor(), name)
        blocks.add(newBlock)
        return newBlock
    }

    /**
     * Creates a new block entity from [constructor] and [ktBlock].
     */
    fun <E: BlockEntity, B: Block> blockEntity(
        constructor: (KtBlock<B>, KtBlockEntity<E>) -> E, name: String, ktBlock: KtBlock<B>
    ): KtBlockEntity<E> {
        lateinit var newBlockEntity: KtBlockEntity<E>
        newBlockEntity = KtBlockEntity<E>(BlockEntityType.Builder.create(Supplier { constructor(ktBlock, newBlockEntity) }, ktBlock.block).build(null), name)
        blockEntities.add(newBlockEntity)
        return newBlockEntity
    }

    /**
     * Registers all non-client-side exclusives.
     */
    fun registerAll() {
        for (ktItem in this.items) {
            val id = Identifier(this.id, ktItem.id)
            println("Registering $id")
            Registry.register(Registry.ITEM, id, ktItem.item)
        }

        for (ktBlock in this.blocks) {
            val id = Identifier(this.id, ktBlock.id)
            println("Registering $id")
            Registry.register(Registry.BLOCK, id, ktBlock.block)
            val blockItem = BlockItem(ktBlock.block, Item.Settings())
            ktBlock.blockItem = blockItem
            if (this.creativeTabIcon.block == ktBlock.block) {
                this.creativeTabItem = blockItem
            }
            Registry.register(Registry.ITEM, id, blockItem)
        }

        for (ktBlockEntity in this.blockEntities) {
            val id = Identifier(this.id, ktBlockEntity.id)
            println("Registering $id")
            Registry.register(Registry.BLOCK_ENTITY_TYPE, id, ktBlockEntity.blockEntity)
        }

        FabricItemGroupBuilder
            .create(Identifier(this.id, "framity"))
            .icon { ItemStack(this.creativeTabItem) }
            .appendItems { items ->
                this.items.forEach { items.add(ItemStack(it.item)) }
                this.blocks.forEach { items.add(ItemStack(it.blockItem)) }
            }
            .build()
    }
}
