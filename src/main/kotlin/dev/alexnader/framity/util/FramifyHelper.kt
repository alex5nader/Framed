package dev.alexnader.framity.util

import dev.alexnader.framity.block_entities.FrameEntity
import net.fabricmc.fabric.api.tag.TagRegistry
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.tag.Tag
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.random.Random

/**
 * Maps [BlockPos] to [PlayerEntity]. Should be empty except for when a frame is broken.
 */
@JvmField
val posToPlayer: MutableMap<BlockPos, PlayerEntity> = mutableMapOf()

/**
 * The `framity:frames` tag.
 */
@JvmField
val FramesTag: Tag<Block> = TagRegistry.block(Identifier("framity", "frames"))

/**
 * Called on server when left clicked by a player holding a framer's hammer.
 * Removes the "rightmost" item from [frameEntity].
 */
fun onHammerRemove(world: World, frameEntity: FrameEntity<*>?, state: BlockState, player: PlayerEntity, giveItem: Boolean) {
    if (frameEntity == null) {
        return
    }

    val slot = frameEntity.highestRemovePrioritySlot

    if (slot == -1) {
        return
    }

    world.setBlockState(frameEntity.pos, state)
    val stackFromBlock = frameEntity.removeStack(slot, 1)

    if (giveItem) {
        player.inventory.offerOrDrop(world, stackFromBlock)

        world.playSound(null, frameEntity.pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS,
            0.2f,
            (Random.nextFloat() - Random.nextFloat()) * 1.4F + 2.0F)
    }

    frameEntity.sync()
}
