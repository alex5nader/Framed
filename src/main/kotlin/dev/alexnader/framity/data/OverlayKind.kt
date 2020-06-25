package dev.alexnader.framity.data

import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.util.Identifier
import net.minecraft.util.StringIdentifiable

enum class OverlayKind : StringIdentifiable {
    None {
        override fun asString() = "none"

        override val sprites: Nothing? = null
        override val stateForColor: Nothing? = null
    },
    Grass {
        override fun asString() = "grass"

        override val sprites = Sprites(Identifier("minecraft", "block/grass_block_top"), Identifier("minecraft", "block/grass_block_side_overlay"))
        override val stateForColor: BlockState = Blocks.GRASS_BLOCK.defaultState
    };

    data class Sprites(val top: Identifier, val sides: Identifier)

    companion object {
        private val Map = mapOf(
            Items.WHEAT_SEEDS to Grass
        )

        fun from(item: Item) = Map[item]
    }

    abstract val sprites: Sprites?
    abstract val stateForColor: BlockState?
}
