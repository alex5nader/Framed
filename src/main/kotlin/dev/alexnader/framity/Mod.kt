package dev.alexnader.framity

import dev.alexnader.framity.util.WithId
import dev.alexnader.framity.client.model.FramityModelVariantProvider
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.util.function.Supplier

private fun String.withNamespace(namespace: String) = Identifier(namespace, this)

class Mod(private val modId: String, private val modelVariantProvider: FramityModelVariantProvider) {
    private data class ItemGroupInfo(val items: MutableList<String>, val icon: () -> ItemStack)
    private data class ItemInfo(val item: Item)
    private data class DelegatedModelInfo(val delegate: WithId<Block>, val partCount: Int, val sprites: List<SpriteIdentifier>)
    private data class BlockInfo(val block: Block, var renderLayer: RenderLayer? = null, var delegatedModel: DelegatedModelInfo? = null)
    private data class BlockItemInfo(val blockItem: BlockItem)
    private data class BlockEntityInfo<E: BlockEntity>(val blockEntityType: BlockEntityType<E>)

    private val itemGroups: MutableMap<String, ItemGroupInfo> = mutableMapOf()
    private val items: MutableMap<String, ItemInfo> = mutableMapOf()
    private val blocks: MutableMap<String, BlockInfo> = mutableMapOf()
    private val blockItems: MutableMap<String, BlockItemInfo> = mutableMapOf()
    private val blockEntities: MutableMap<String, BlockEntityInfo<*>> = mutableMapOf()

    inner class ItemGroupBuilder(private val id: String, icon: () -> ItemStack) {
        init {
            this@Mod.itemGroups[this.id] = ItemGroupInfo(mutableListOf(), icon)
        }

        fun done() = this@Mod
    }

    inner class ItemBuilder(private val id: String, private val item: Item) {
        init {
            this@Mod.items[this.id] = ItemInfo(this.item)
        }

        fun itemGroup(groupId: String) = this.apply {
            this@Mod.itemGroups[groupId]?.items?.add(this.id)
        }

        fun done() = WithId(this.id, this.item)
    }

    inner class BlockBuilder(private val id: String, private val block: Block) {
        init {
            this@Mod.blocks[this.id] = BlockInfo(this.block)
        }

        fun hasItem(itemSettings: Item.Settings, itemGroup: String? = null) = this.apply {
            this@Mod.blockItems[this.id] = BlockItemInfo(BlockItem(this.block, itemSettings))
            itemGroup?.let {
                this@Mod.itemGroups[it]?.items?.add(this.id)
            }
        }

        fun renderLayer(layer: RenderLayer) = this.apply {
            this@Mod.blocks[this.id]?.let { it.renderLayer = layer }
        }

        fun hasDelegateModel(source: WithId<Block>, sprites: List<SpriteIdentifier>, partCount: Int) = this.apply {
            this@Mod.blocks[this.id]?.let { it.delegatedModel = DelegatedModelInfo(source, partCount, sprites) }
        }

        fun done() = WithId(this.id, this.block)
    }

    fun itemGroup(id: String, icon: () -> ItemStack) = ItemGroupBuilder(id, icon)
    fun item(id: String, item: Item) = ItemBuilder(id, item)
    fun block(id: String, block: Block) = BlockBuilder(id, block)

    fun <E: BlockEntity> blockEntity(id: String, constructor: () -> E, vararg blocks: Block): WithId<BlockEntityType<E>> {
        val blockEntityType: WithId<BlockEntityType<E>> = WithId(id, BlockEntityType.Builder.create(Supplier { constructor() }, *blocks).build(null))
        this.blockEntities[id] = BlockEntityInfo(blockEntityType.value)
        return blockEntityType
    }

    fun id(id: String): Identifier = Identifier(this.modId, id)

    fun register() {
        for ((id, itemGroup) in this.itemGroups) {
            FabricItemGroupBuilder.create(id.withNamespace(this.modId))
                .icon(itemGroup.icon)
                .appendItems { items ->
                    itemGroup.items.forEach { containedId ->
                        this.items[containedId]?.let {
                            items.add(ItemStack(it.item))
                        }
                        this.blockItems[containedId]?.let {
                            items.add(ItemStack(it.blockItem))
                        }
                    }
                }
                .build()
        }

        for ((id, item) in this.items) {
            Registry.register(Registry.ITEM, id.withNamespace(this.modId), item.item)
        }

        for ((id, block) in this.blocks) {
            Registry.register(Registry.BLOCK, id.withNamespace(this.modId), block.block)
        }

        for ((id, blockItem) in this.blockItems) {
            Registry.register(Registry.ITEM, id.withNamespace(this.modId), blockItem.blockItem)
        }

        for ((id, blockEntity) in this.blockEntities) {
            Registry.register(Registry.BLOCK_ENTITY_TYPE, id.withNamespace(this.modId), blockEntity.blockEntityType)
        }
    }

    fun registerClient() {
        ModelLoadingRegistry.INSTANCE.registerVariantProvider { this.modelVariantProvider }

        for ((_, block) in this.blocks) {
            block.delegatedModel?.let { (source, partCount, sprites) -> this@Mod.modelVariantProvider.registerModelsFor(block.block, source.value, partCount, sprites) }
            block.renderLayer?.let { BlockRenderLayerMap.INSTANCE.putBlock(block.block,  it) }
        }
    }
}