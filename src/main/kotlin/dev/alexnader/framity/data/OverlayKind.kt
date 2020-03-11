package dev.alexnader.framity.data

import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.util.StringIdentifiable

enum class OverlayKind : StringIdentifiable {
    None {
        override fun asString() = "none"
    },
    Grass {
        override fun asString() = "grass"
    };

    companion object {
        private val Map = mapOf(
            Items.WHEAT_SEEDS to Grass
        )

        fun from(item: Item) = Map[item]
    }
}
