package dev.alexnader.framity.util

import com.mojang.serialization.Dynamic
import net.minecraft.block.BlockState
import net.minecraft.datafixer.NbtOps
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.IntTag
import net.minecraft.nbt.ListTag
import kotlin.experimental.and

class FrameDataFormat private constructor(private val sections: MutableList<Section>) : List<Section> by sections {
    companion object {
        const val BASE_INDEX = 0
        const val OVERLAY_INDEX = 1
        const val SPECIAL_INDEX = 2

        private fun sizesToSections(sizes: Sequence<Int>): MutableList<Section> =
            ArrayList<Section>().apply {
                var start = 0
                for (size in sizes) {
                    val section = Section(start, start + size)
                    add(section)
                    start = section.end
                }
            }

        fun fromTag(tag: CompoundTag) =
            FrameDataFormat(sizesToSections(tag.getList("format", 3).asSequence().map { (it as IntTag).int }))
    }

    constructor(baseSize: Int, overlaySize: Int, specialSize: Int, vararg otherSizes: Int) : this(sizesToSections(sequence {
        yield(baseSize)
        yield(overlaySize)
        yield(specialSize)
        yieldAll(otherSizes.asSequence())
    }))

    val base get() = sections[BASE_INDEX]
    val overlay get() = sections[OVERLAY_INDEX]
    val special get() = sections[SPECIAL_INDEX]

    val totalSize get() = sections.sumBy { it.size }

    fun fromTag(tag: CompoundTag) {
        sections.clear()
        sections.addAll(sizesToSections(tag.getList("format", 3).asSequence().map { (it as IntTag).int }))
    }

    fun toTag() = ListTag().apply {
        sections.forEach { section -> add(IntTag.of(section.size)) }
    }

    override fun toString() = if (sections.isEmpty()) {
        "FrameDataFormat()"
    } else {"""
        FrameDataFormat(
            base = $base
            overlay = $overlay
            special = $special
            other = ${sections.subList(SPECIAL_INDEX + 1, sections.size)}
        )
    """.trimIndent()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FrameDataFormat

        if (sections != other.sections) return false

        return true
    }

    override fun hashCode(): Int {
        return sections.hashCode()
    }
}

data class FrameData(val format: FrameDataFormat, val items: SectionedList<ItemStack>, val baseStates: FixedSizeList<BlockState?>) {
    companion object {
        private fun itemsFromTag(target: SectionedList<ItemStack>, tag: CompoundTag) = target.also {
            tag.getList("Items", 10).let { listTag ->
                listTag.indices.map {
                    val stackTag = listTag.getCompound(it)
                    val slot = (stackTag.getByte("Slot") and 255.toByte()).toInt()
                    if (0 <= slot && slot < target.size) {
                        target[slot] = ItemStack.fromTag(stackTag)
                    }
                }
            }
        }

        private fun statesFromTag(target: FixedSizeList<BlockState?>, tag: CompoundTag) = target.also {
            tag.getList("states", 10).let { listTag ->
                listTag.indices.map {
                    val stateTag = listTag.getCompound(it)
                    target[it] =
                        BlockState.CODEC.decode(Dynamic(NbtOps.INSTANCE, stateTag)).result().get().first
                }
            }
        }

        fun fromTag(tag: CompoundTag) =
            FrameDataFormat.fromTag(tag).let { format ->
                FrameData(
                    format,
                    itemsFromTag(SectionedList(ItemStack.EMPTY, format, ItemStackEquality), tag),
                    statesFromTag(FixedSizeList(null, format.base.size), tag)
                )
            }
    }

    fun fromTag(tag: CompoundTag) {
        this.items.clear()

        this.format.fromTag(tag)
        itemsFromTag(this.items, tag)
        statesFromTag(this.baseStates, tag)
    }

    fun toTag() =
        CompoundTag().apply {
            put("format", format.toTag())

            items.indices.asSequence()
                .map { Pair(it, items[it]) }
                .filter { !it.second.isEmpty }
                .fold(ListTag()) { list, (slot, stack) ->
                    list.add(CompoundTag().apply {
                        putByte("Slot", slot.toByte())
                        stack.toTag(this)
                    })
                    list
                }
                .takeUnless { it.isEmpty() }
                ?.let { put("Items", it) }

            baseStates.nonEmpty
                .fold(ListTag()) { list, baseState ->
                    list.add(BlockState.CODEC.encode(baseState, NbtOps.INSTANCE, CompoundTag()).get().left().get())
                    list
                }
                .takeUnless { it.isEmpty() }
                ?.let { put("states", it) }
        }

    override fun toString() = "FrameData($items, $baseStates)"
}