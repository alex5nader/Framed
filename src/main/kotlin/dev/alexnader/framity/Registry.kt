package dev.alexnader.framity

import dev.alexnader.framity.adapters.KtBlock
import dev.alexnader.framity.adapters.KtBlockEntity
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import grondag.fermion.client.ClientRegistrar
import java.util.function.Supplier

class Mod(private val id: String) {
    private val blockEntityRendererInitializers = ArrayList<() -> Unit>()
    private val blocks = ArrayList<KtBlock<Block>>()
    private val blockEntities = ArrayList<KtBlockEntity<*>>()

    private lateinit var creativeTabIcon: KtBlock<Block>
    private lateinit var creativeTabItem: Item

    val clientRegistrar = ClientRegistrar(this.id)

    fun id(id: String): Identifier = Identifier(this.id, id)

    fun <B: Block> creativeTab(ktBlock: KtBlock<B>) {
        creativeTabIcon = ktBlock
    }

    fun <B: Block> block(constructor: () -> B, name: String): KtBlock<B> {
        val newBlock = KtBlock(constructor(), name)
        blocks.add(newBlock)
        return newBlock
    }

    fun <E: BlockEntity, B: Block> blockEntity(
        constructor: (KtBlock<B>, KtBlockEntity<E>) -> E, name: String, ktBlock: KtBlock<B>
    ): KtBlockEntity<E> {
        lateinit var newBlockEntity: KtBlockEntity<E>
        newBlockEntity = KtBlockEntity<E>(BlockEntityType.Builder.create(Supplier { constructor(ktBlock, newBlockEntity) }, ktBlock.block).build(null), name)
        blockEntities.add(newBlockEntity)
        return newBlockEntity
    }

    fun <E: BlockEntity, R: BlockEntityRenderer<E>> rendered(
        ktBlockEntity: KtBlockEntity<E>, constructor: (BlockEntityRenderDispatcher) -> R
    ): KtBlockEntity<E> {
        blockEntityRendererInitializers.add {
            BlockEntityRendererRegistry.INSTANCE.register(
                ktBlockEntity.blockEntity
            ) { constructor(it) }
        }
        return ktBlockEntity
    }

    fun registerAll() {
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
            println("Registering ${Identifier(id, ktBlockEntity.id)}")
            Registry.register(Registry.BLOCK_ENTITY, Identifier(id, ktBlockEntity.id), ktBlockEntity.blockEntity)
        }

        FabricItemGroupBuilder
            .create(Identifier(this.id, "framity"))
            .icon { ItemStack(this.creativeTabItem) }
            .appendItems { items -> this.blocks.forEach { items.add(ItemStack(it.blockItem)) } }
            .build()
    }

    fun registerAllClient() {
        for (initializer in this.blockEntityRendererInitializers) {
            initializer()
        }
    }
}
