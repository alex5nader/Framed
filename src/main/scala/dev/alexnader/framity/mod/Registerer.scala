package dev.alexnader.framity.mod

import com.google.common.collect.HashBiMap
import dev.alexnader.framity.mod.WithId._
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.block.Block
import net.minecraft.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.item.{BlockItem, Item, ItemStack}
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.jdk.FunctionConverters._

object Registerer {
  def `for`(_namespace: String): Registerer = new Registerer {
    override val namespace: String = _namespace
  }

  object register {
    def block(block: WithId[Block])(implicit registerer: Registerer): Unit = {
      registerer.addBlock(block)
    }

    def blockWithItem(block: WithId[Block])(implicit registerer: Registerer): Unit = {
      registerer.addBlockWithItem(block)
    }

    def blockWithItem(block: WithId[Block], group: ItemGroupAdder)(implicit registerer: Registerer): Unit = {
      registerer.addBlockWithItem(block, group)
    }

    def item(item: WithId[Item])(implicit registerer: Registerer): Unit = {
      registerer.addItem(item)
    }

    def item(item: WithId[Item], group: ItemGroupAdder)(implicit registerer: Registerer): Unit = {
      registerer.addItem(item, group)
    }

    def blockEntityType[E <: BlockEntity](blockEntityType: WithId[BlockEntityType[E]])(implicit registerer: Registerer): Unit = {
      registerer.addBlockEntityType(blockEntityType)
    }
  }

}

trait Registerer extends Mod {
  private val blocks: mutable.Map[Identifier, Block] = mutable.Map[Identifier, Block]()
  private val items: HashBiMap[Identifier, Item] = HashBiMap.create[Identifier, Item]()
  private val blockEntityTypes: mutable.Map[Identifier, BlockEntityType[_]] = mutable.Map[Identifier, BlockEntityType[_]]()
  private val itemGroups: mutable.Map[Identifier, ItemGroupAdder] = mutable.Map[Identifier, ItemGroupAdder]()

  def addBlock(block: WithId[Block]): WithId[Block] = {
    blocks(block.id) = block
    block
  }

  def addBlockWithItem(block: WithId[Block]): WithId[Block] = {
    addItem(new BlockItem(block, new Item.Settings) withId block.id)
    addBlock(block)
  }

  def addBlockWithItem(block: WithId[Block], settings: Item.Settings): WithId[Block] = {
    addItem(new BlockItem(block, settings) withId block.id)
    addBlock(block)
  }

  def addBlockWithItem(block: WithId[Block], group: ItemGroupAdder): WithId[Block] = {
    addItem(new BlockItem(block, new Item.Settings) withId block.id, group)
    addBlock(block)
  }

  def addBlockWithItem(block: WithId[Block], group: ItemGroupAdder, settings: Item.Settings): WithId[Block] = {
    addItem(new BlockItem(block, settings) withId block.id, group)
    addBlock(block)
  }

  def addItem(item: WithId[Item]): WithId[Item] = {
    items.put(item.id, item)
    item
  }

  def addItem(item: WithId[Item], group: ItemGroupAdder): WithId[Item] = {
    group.contents.addOne(item.id)
    items.put(item.id, item)
    item
  }

  def addBlockEntityType[E <: BlockEntity](blockEntityType: WithId[BlockEntityType[E]]): WithId[BlockEntityType[E]] = {
    blockEntityTypes(blockEntityType.id) = blockEntityType
    blockEntityType
  }

  def addItemGroup(adder: WithId[ItemGroupAdder]): Unit = {
    itemGroups(adder.id) = adder
  }

  def register(): Unit = {
    blocks foreach { case (id, block) =>
      Registry.register(Registry.BLOCK, id, block)
    }
    items.forEach((id, item) =>
      Registry.register(Registry.ITEM, id, item)
    )
    blockEntityTypes foreach { case (id, blockEntityType) =>
      Registry.register(Registry.BLOCK_ENTITY_TYPE, id, blockEntityType)
    }
    itemGroups foreach { case (id, group) =>
      FabricItemGroupBuilder.create(id).icon(group.makeIcon.asJava).appendItems(items => items.addAll(group.contents.map(id => new ItemStack(this.items.get(id))).asJava)).build()
    }
  }
}
