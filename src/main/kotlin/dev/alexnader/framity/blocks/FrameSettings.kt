package dev.alexnader.framity.blocks

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Material
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.property.Properties

val SHAPE_FRAME_SETTINGS: AbstractBlock.Settings =
    FabricBlockSettings
        .of(Material.WOOD)
        .hardness(0.33f)
        .sounds(BlockSoundGroup.WOOD)
        .nonOpaque()
        .solidBlock { _, _, _ -> false }

@JvmField
val FRAME_SETTINGS: AbstractBlock.Settings =
    SHAPE_FRAME_SETTINGS
        .lightLevel { state -> if (state.contains(Properties.LIT) && state[Properties.LIT]) 15 else 0 }
