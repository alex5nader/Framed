package dev.alexnader.framity

import dev.alexnader.framity.block_entities.FrameEntity
import dev.alexnader.framity.blocks.HAS_REDSTONE
import net.minecraft.item.Items
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
import net.minecraft.world.World

sealed class SpecialItemData {
    class BindsProperty(private val property: BooleanProperty) : SpecialItemData() {
        override fun onAdd(world: World, frameEntity: FrameEntity) {
            world.setBlockState(frameEntity.pos, frameEntity.cachedState.with(property, true))
        }

        override fun onRemove(world: World, frameEntity: FrameEntity) {
            world.setBlockState(frameEntity.pos, frameEntity.cachedState.with(property, false))
        }
    }

    abstract fun onAdd(world: World, frameEntity: FrameEntity)
    abstract fun onRemove(world: World, frameEntity: FrameEntity)
}

data class SpecialItem(val offset: Int, val data: SpecialItemData)

@JvmField
val SPECIAL_ITEMS = mapOf(
    Items.GLOWSTONE_DUST to SpecialItemData.BindsProperty(Properties.LIT),
    Items.REDSTONE to SpecialItemData.BindsProperty(HAS_REDSTONE)
)
    .entries
    .mapIndexed { i, (item, data) -> Pair(item, SpecialItem(i, data)) }
    .toMap()
