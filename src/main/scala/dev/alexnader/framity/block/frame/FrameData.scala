package dev.alexnader.framity.block.frame

import com.mojang.serialization.Dynamic
import dev.alexnader.framity.util.MinecraftUtil.CompoundTagExt
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtOps
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{CompoundTag, IntTag, ListTag}

import scala.jdk.CollectionConverters._
import scala.language.postfixOps

object FrameData {

  object Sections {

    implicit class SlotSectionConversions(val i: Int) {
      def toRelative(r: Range): Int = i - r.start

      def toAbsolute(r: Range): Int = r.start + i
    }

    val BaseIndex: Int = 0
    val OverlayIndex: Int = 1
    val SpecialIndex: Int = 2

    def fromTag(tag: ListTag): Sections = {
      new Sections(makeSections(tag.iterator.asScala map (_.asInstanceOf[IntTag].getInt)))
    }

    private def makeSections(sizes: Iterator[Int]): Array[Range] = {
      var start = 0
      sizes map { size =>
        val r = start until start + size
        start += size
        r
      } toArray
    }
  }

  class Sections(private val sections: Array[Range]) {
    def this(partCount: Int, otherSizes: Int*) = this(Sections.makeSections(Iterator(partCount) ++ Iterator(partCount) ++ Iterator(SpecialItems.map.size) ++ otherSizes.iterator))

    def apply(sectionIndex: Int): Range = sections(sectionIndex)

    def base: Range = this (Sections.BaseIndex)

    def overlay: Range = this (Sections.OverlayIndex)

    def special: Range = this (Sections.SpecialIndex)

    def itemCount: Int = sections.last.end + 1

    def sectionCount: Int = sections.length

    def itemIndices: Range = 0 until sections.last.end

    def containsSlot(slot: Int): Boolean = 0 <= slot && slot < sections.last.end

    def findSectionIndexOf(absoluteIndex: Int): Int = {
      @scala.annotation.tailrec
      def findSectionIndexOf(i: Int, absoluteIndex: Int): Int = {
        if (absoluteIndex < sections(i).end) {
          i
        } else if (i == sections.length) {
          -1
        } else {
          findSectionIndexOf(i + 1, absoluteIndex)
        }
      }

      findSectionIndexOf(0, absoluteIndex)
    }

    def makeItems: Array[Option[ItemStack]] =
      Array.fill[Option[ItemStack]](itemCount)(None)

    def makeBaseStates: Array[Option[BlockState]] =
      Array.fill[Option[BlockState]](base.size)(None)

    def toTag: ListTag = {
      val tag = new ListTag

      sections foreach (s => tag.add(IntTag.of(s.size)))

      tag
    }

    def canEqual(other: Any): Boolean = other.isInstanceOf[Sections]

    override def equals(other: Any): Boolean = other match {
      case that: Sections =>
        (that canEqual this) &&
          (sections sameElements that.sections)
      case _ => false
    }

    override def hashCode(): Int = {
      val state = Seq(sections)
      state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
    }
  }

  private def itemsFromTag(sections: Sections, tag: ListTag): Array[Option[ItemStack]] = {
    val items = sections.makeItems

    tag.asScala.indices foreach { i =>
      val stackTag = tag.getCompound(i)
      val slot = stackTag.getByte("Slot") & 255
      if (sections containsSlot slot) {
        items(slot) = Some(ItemStack.fromTag(stackTag))
      }
    }

    items
  }

  private def statesFromTag(sections: Sections, tag: ListTag): Array[Option[BlockState]] = {
    val states = sections.makeBaseStates

    tag.asScala.indices foreach { tagIndex =>
      val stateTag = tag.getCompound(tagIndex)
      val realIndex = stateTag.getInt("i")
      states(realIndex) = Some(BlockState.CODEC.decode(new Dynamic(NbtOps.INSTANCE, stateTag)).result.get.getFirst)
    }

    states
  }

  def fromTag(tag: CompoundTag): FrameData = {
    val sections = Sections fromTag tag.getList("format", 3)

    FrameData(sections, itemsFromTag(sections, tag.getList("Items", 10)), statesFromTag(sections, tag.getList("states", 10)))
  }
}

case class FrameData(sections: FrameData.Sections, items: Array[Option[ItemStack]], baseStates: Array[Option[BlockState]]) {
  def this(sections: FrameData.Sections) = this(sections, sections.makeItems, sections.makeBaseStates)

  def toTag: CompoundTag = {
    val tag = new CompoundTag

    tag("format") = sections.toTag

    val itemsTag = items.iterator.zipWithIndex.foldLeft(new ListTag) { case (acc, (stack, slot)) =>
      stack match {
        case Some(stack) =>
          val stackTag = new CompoundTag
          stack.toTag(stackTag)
          stackTag.putByte("Slot", slot.byteValue)
          acc.add(stackTag)
        case None =>
      }
      acc
    }
    if (!itemsTag.isEmpty) {
      tag("Items") = itemsTag
    }

    val statesTag = baseStates.iterator.zipWithIndex.foldLeft(new ListTag) { case (acc, (state, i)) =>
      state match {
        case Some(state) =>
          val stateTag = new CompoundTag
          stateTag.putInt("i", i)
          acc.add(BlockState.CODEC.encode(state, NbtOps.INSTANCE, stateTag).get.left.get)
        case None =>
      }
      acc
    }
    if (!statesTag.isEmpty) {
      tag("states") = statesTag
    }

    tag
  }
}
