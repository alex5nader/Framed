package dev.alexnader.framity.blocks

import dev.alexnader.framity.util.HasGlowstone
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Material
import net.minecraft.sound.BlockSoundGroup

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
        .lightLevel { state -> if (state.contains(HasGlowstone) && state[HasGlowstone]) 15 else 0 }
