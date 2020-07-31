package dev.alexnader.framity.block_entity

import dev.alexnader.framity.block.frame.FrameData.Sections
import dev.alexnader.framity.block.frame.FrameData.Sections.SlotSectionConversions
import dev.alexnader.framity.block.frame.Valid.ItemStackValidQuery
import dev.alexnader.framity.block.frame.{FrameData, SpecialItems}
import dev.alexnader.framity.data.overlay.ItemStackOverlayQuery
import dev.alexnader.framity.gui.FrameGuiDescription
import dev.alexnader.framity.mixin_extra.MixinExtensions.GetItemBeforeEmpty
import dev.alexnader.framity.util.ScalaExtensions.IsInRange
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.fabricmc.fabric.api.server.PlayerStream
import net.minecraft.block.BlockState
import net.minecraft.block.entity.{BlockEntityType, LockableContainerBlockEntity}
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.{PlayerEntity, PlayerInventory}
import net.minecraft.item.{BlockItem, ItemStack}
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.{ScreenHandler, ScreenHandlerContext}
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.{Text, TranslatableText}
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction

import scala.collection.{IndexedSeqView, View}

object FrameEntity {
  val Sections = new FrameData.Sections(1)
}

class FrameEntity(`type`: BlockEntityType[FrameEntity], _sections: FrameData.Sections)
  extends LockableContainerBlockEntity(`type`)
    with ExtendedScreenHandlerFactory
    with RenderAttachmentBlockEntity
    with BlockEntityClientSerializable {

  def this(`type`: BlockEntityType[FrameEntity]) = this(`type`, FrameEntity.Sections)

  var data = new FrameData(_sections)

  def sections: Sections = data.sections

  def items: Array[Option[ItemStack]] = data.items

  def baseItems: IndexedSeqView[Option[ItemStack]] = items.view.slice(data.sections.base.start, data.sections.base.end)

  def overlayItems: IndexedSeqView[Option[ItemStack]] = items.view.slice(data.sections.overlay.start, data.sections.overlay.end)

  def specialItems: IndexedSeqView[Option[ItemStack]] = items.view.slice(data.sections.special.start, data.sections.special.end)

  def baseStates: Array[Option[BlockState]] = data.baseStates

  def copyFrom(slot: Int, stack: ItemStack, count: Int, take: Boolean): Unit = {
    val newStack = stack.copy
    val realCount = count min stack.getCount

    newStack.setCount(realCount)

    if (take) {
      stack.setCount(stack.getCount - realCount)
    }

    this.setStack(slot, newStack)
  }

  override def getMaxCountPerStack: Int = 1

  override def isValid(slot: Int, stack: ItemStack): Boolean = sections.findSectionIndexOf(slot) match {
    case FrameData.Sections.BaseIndex => stack.isValidForBase(s => Some(s.getBlock.getDefaultState), world, pos).isDefined
    case FrameData.Sections.OverlayIndex => stack.isValidForOverlay
    case FrameData.Sections.SpecialIndex => stack.isValidForSpecial
    case _ => false
  }

  private def beforeRemove(slot: Int): Unit = {
    sections.findSectionIndexOf(slot) match {
      case FrameData.Sections.BaseIndex => this.baseStates(slot.toRelative(sections.base)) = None
      case FrameData.Sections.OverlayIndex =>
      case FrameData.Sections.SpecialIndex => SpecialItems.map(getStack(slot).itemBeforeEmpty).onRemove(world, this)
      case _ => throw new IllegalArgumentException(s"Invalid slot: $slot")
    }
  }

  override def removeStack(slot: Int, amount: Int): ItemStack = {
    beforeRemove(slot)

    Some(slot)
      .filter { _ => slot isIn sections.itemIndices }
      .filter { _ => amount > 0 }
      .flatMap { items(_) }
      .map { orig => (orig, orig split amount) }
      .map { case (orig, res) =>
        this.markDirty()
        if (orig.isEmpty) {
          items(slot) = None
        }
        res
      }
      .getOrElse(ItemStack.EMPTY)
  }

  override def removeStack(slot: Int): ItemStack = {
    beforeRemove(slot)

    this.markDirty()

    val result = items(slot)

    items(slot) = None

    result.getOrElse(ItemStack.EMPTY)
  }

  override def getStack(slot: Int): ItemStack = items(slot).getOrElse(ItemStack.EMPTY)

  override def size(): Int = sections.itemCount

  override def isEmpty: Boolean = items.iterator.flatten.isEmpty

  override def canPlayerUse(player: PlayerEntity): Boolean = true

  override def clear(): Unit = {
    items.indices.foreach { slot => items(slot) = None }
  }

  override def setStack(slot: Int, stack: ItemStack): Unit = {
    def setStack(): Unit = {
      items(slot) = Some(stack)
      stack.setCount(stack.getCount min this.getMaxCountPerStack)
      this.markDirty()
    }

    val sectionIndex = sections.findSectionIndexOf(slot)

    sectionIndex match {
      case FrameData.Sections.BaseIndex =>
        setStack()
        val baseSlot = slot.toRelative(sections.base)
        this.baseStates(baseSlot) = this.baseItems(baseSlot)
          .map(_.getItem)
          .filter(_.isInstanceOf[BlockItem])
          .map(_.asInstanceOf[BlockItem].getBlock.getDefaultState)
      case FrameData.Sections.SpecialIndex =>
        SpecialItems.map.get(getStack(slot).itemBeforeEmpty).tapEach(_.onRemove(world, this))
        setStack()
        SpecialItems.map.get(getStack(slot).getItem).tapEach(_.onAdd(world, this))
      case _ => setStack()
    }
  }

  override def markDirty(): Unit = {
    super.markDirty()

    Option(world) match {
      case Some(world) =>
        val state = world.getBlockState(pos)
        val block = state.getBlock

        if (world.isClient) {
          MinecraftClient.getInstance.worldRenderer.updateBlock(world, pos, getCachedState, state, 1)
        } else {
          sync()

          PlayerStream.watching(this).forEach(_.asInstanceOf[ServerPlayerEntity].networkHandler.sendPacket(this.toUpdatePacket))

          world.updateNeighborsAlways(pos.offset(Direction.UP), block)
        }
      case None =>
    }
  }

  override def getRenderAttachmentData: View[(Option[BlockState], Option[Identifier])] =
    baseStates.view zip overlayItems.map(_.flatMap(_.getOverlayId))

  override def toTag(tag: CompoundTag): CompoundTag = {
    toClientTag(tag)
    super.toTag(tag)
  }

  override def fromTag(state: BlockState, tag: CompoundTag): Unit = {
    fromClientTag(tag)
    super.fromTag(state, tag)
  }

  override def toClientTag(tag: CompoundTag): CompoundTag = {
    tag.put("frameData", data.toTag)
    tag
  }

  override def fromClientTag(tag: CompoundTag): Unit = {
    data = FrameData.fromTag(tag.getCompound("frameData"))
    this.markDirty()
  }

  override def createScreenHandler(syncId: Int, playerInventory: PlayerInventory): ScreenHandler = {
    new FrameGuiDescription(syncId, playerInventory, ScreenHandlerContext.create(world, pos))
  }

  override def writeScreenOpeningData(serverPlayerEntity: ServerPlayerEntity, packetByteBuf: PacketByteBuf): Unit = {
    packetByteBuf.writeBlockPos(pos)
  }

  override def getContainerName: Text = new TranslatableText(getCachedState.getBlock.getTranslationKey)
}
