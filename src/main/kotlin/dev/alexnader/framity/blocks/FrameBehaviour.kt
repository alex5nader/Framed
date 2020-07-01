@file:Suppress("FunctionName", "UNUSED_PARAMETER")

package dev.alexnader.framity.blocks

import dev.alexnader.framity.FRAMERS_HAMMER
import dev.alexnader.framity.block_entities.FrameEntity
import dev.alexnader.framity.block_entities.FrameEntity.Companion.OtherItem
import dev.alexnader.framity.data.hasOverlay
import dev.alexnader.framity.items.HammerData
import dev.alexnader.framity.util.equalsIgnoring
import net.fabricmc.fabric.api.tag.TagRegistry
import net.minecraft.block.*
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
import net.minecraft.tag.Tag
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import kotlin.random.Random

val HAS_REDSTONE: BooleanProperty = BooleanProperty.of("has_redstone")

/**
 * Maps [BlockPos] to [PlayerEntity]. Should be empty except for when a frame is broken.
 */
val posToPlayer: MutableMap<BlockPos, PlayerEntity> = mutableMapOf()

fun frameStatesEqual(a: BlockState, b: BlockState) = equalsIgnoring<Block, BlockState>(setOf(Properties.LIT, HAS_REDSTONE))(a, b)

/**
 * Called on server when left clicked by a player holding a framer's hammer.
 * Removes the "rightmost" item from [frameEntity].
 */
fun onHammerRemove(world: World, frameEntity: FrameEntity?, state: BlockState, player: PlayerEntity, giveItem: Boolean) {
    if (frameEntity == null) {
        return
    }

    fun removeStack(slot: Int) =
        frameEntity.removeStack(slot, 1).let { stack ->
            if (!stack.isEmpty && giveItem) {
                player.inventory.offerOrDrop(world, stack)

                world.playSound(null, frameEntity.pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS,
                    0.2f,
                    (Random.nextFloat() - Random.nextFloat()) * 1.4f + 2f
                )
            }
        }

    world.setBlockState(frameEntity.pos, state)

    if (player.isSneaking) {
        (0 until FrameEntity.SLOT_COUNT).forEach(::removeStack)
    } else {
        removeStack(frameEntity.highestRemovePrioritySlot)
    }

    frameEntity.sync()
}

fun validForOther(stack: ItemStack) =
    stack.item in FrameEntity.OTHER_ITEM_DATA

fun validForOverlay(stack: ItemStack) =
    hasOverlay(stack)

fun validForBase(stack: ItemStack, toState: (BlockItem) -> BlockState?, world: World, pos: BlockPos): BlockState? {
    val item = stack.item
    if (item == null || item !is BlockItem) {
        return null
    }

    if (item.block is Frame) {
        return null
    }

    return toState(item)?.let { state ->
        if (validStateForBase(state, world, pos)) {
            state
        } else {
            null
        }
    }
}

fun validStateForBase(state: BlockState, world: World, pos: BlockPos): Boolean {
    @Suppress("deprecation")
    if (state.block is BlockWithEntity && state.block.getRenderType(state) != BlockRenderType.MODEL) {
        return false
    }

    @Suppress("deprecation")
    val outlineShape = state.block.getOutlineShape(state, world, pos, ShapeContext.absent())

    if (VoxelShapes.fullCube().boundingBoxes != outlineShape.boundingBoxes) {
        return false
    }

    return true
}

fun frameDefaultState(base: BlockState): BlockState =
    base.with(Properties.LIT, false)
        .with(HAS_REDSTONE, false)

fun frame_appendProperties(builder: StateManager.Builder<Block, BlockState>, callSuper: () -> Unit) {
    callSuper()
    builder.add(Properties.LIT)
    builder.add(HAS_REDSTONE)
}

const val IS_TRANSLUCENT = true
const val HAS_DYNAMIC_BOUNDS = true
const val AMBIENT_OCCLUSION_LIGHT_LEVEL = 1f

fun frame_emitsRedstonePower(state: BlockState): Boolean =
    state[HAS_REDSTONE]

fun frame_getWeakRedstonePower(state: BlockState, world: BlockView, pos: BlockPos, direction: Direction) =
    if (state[HAS_REDSTONE]) 15 else 0

fun frame_isSideInvisible(
    state: BlockState,
    stateFrom: BlockState,
    direction: Direction,
    `this`: Block,
    callSuper: () -> Boolean
): Boolean {
    return stateFrom.isOf(`this`) && frameStatesEqual(state, stateFrom) || callSuper()
}

fun frame_onSyncedBlockEvent(state: BlockState, world: World, pos: BlockPos, type: Int, data: Int, callSuper: () -> Boolean): Boolean {
    callSuper()
    return world.getBlockEntity(pos)?.onSyncedBlockEvent(type, data) ?: false
}

@Suppress("UNUSED_PARAMETER")
fun frame_createScreenHandlerFactory(state: BlockState, world: World, pos: BlockPos): NamedScreenHandlerFactory? {
    return world.getBlockEntity(pos) as? NamedScreenHandlerFactory
}

@Suppress("UNUSED_PARAMETER")
fun frame_onStateReplaced(
    oldState: BlockState,
    world: World,
    pos: BlockPos,
    newState: BlockState,
    moved: Boolean,
    `this`: Block,
    callSuper: () -> Unit
) {
    if (world.isClient() || oldState.block === newState.block) {
        return
    }
    when (val player = posToPlayer.remove(pos)) {
        null -> {
            val blockEntity = world.getBlockEntity(pos)
            if (blockEntity is Inventory) {
                ItemScatterer.spawn(world, pos, blockEntity as Inventory?)
                world.updateComparators(pos, `this`)
            }
            callSuper()
        }
        else -> {
            if (player.getStackInHand(player.activeHand).item === FRAMERS_HAMMER.value) {
                onHammerRemove(world, world.getBlockEntity(pos) as FrameEntity?, oldState, player, false)
            } else {
                callSuper()
            }
        }
    }
}

fun frame_onBlockBreakStart(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, callSuper: () -> Unit) {
    callSuper()

    if (world.isClient()) {
        return
    }

    if (player.getStackInHand(player.activeHand).item === FRAMERS_HAMMER.value) {
        onHammerRemove(world, world.getBlockEntity(pos) as FrameEntity?, state, player, true)
    }
}

fun frame_onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity, callSuper: () -> Unit) {
    callSuper()

    if (world.isClient() || !player.isCreative) {
        return
    }
    
    posToPlayer[pos] = player
}

fun frame_onUse(
    state: BlockState,
    world: World,
    pos: BlockPos,
    player: PlayerEntity,
    hand: Hand,
    hit: BlockHitResult,
    callSuper: () -> ActionResult?
): ActionResult? {
    val frameEntity = world.getBlockEntity(pos) as FrameEntity? ?: return ActionResult.CONSUME
    
    val playerStack = player.mainHandStack

    if (playerStack != null) {
        if (frameEntity.overlayStack.isEmpty && validForOverlay(playerStack)) {
            if (!world.isClient) {
                frameEntity.copyFrom(FrameEntity.OVERLAY_SLOT, playerStack, 1, !player.isCreative)
                frameEntity.markDirty()
            }
            return ActionResult.SUCCESS
        }

        val maybeBaseState = validForBase(
            playerStack,
            { bi -> bi.block.getPlacementState(ItemPlacementContext(ItemUsageContext(player, hand, hit))) },
            world,
            pos
        )

        if (playerStack.item !== frameEntity.baseStack.item && maybeBaseState != null) {
            if (!world.isClient) {
                if (!frameEntity.baseStack.isEmpty && !player.isCreative) {
                    player.inventory.offerOrDrop(world, frameEntity.baseStack)
                }
                frameEntity.copyFrom(FrameEntity.BASE_SLOT, playerStack, 1, !player.isCreative)
                frameEntity.baseState = maybeBaseState
            }
            return ActionResult.SUCCESS
        }

        if (validForOther(playerStack)) {
            val otherItem: OtherItem? = FrameEntity.OTHER_ITEM_DATA[playerStack.item]
            return if (frameEntity.getStack(otherItem!!.slot).isEmpty) {
                if (!world.isClient) {
                    frameEntity.copyFrom(otherItem.slot, playerStack, 1, !player.isCreative)
                    otherItem.onAdd(world, frameEntity)
                }
                ActionResult.SUCCESS
            } else {
                ActionResult.CONSUME
            }
        }
    }
    
    if (playerStack.isEmpty && player.isSneaking) {
        player.openHandledScreen(state.createScreenHandlerFactory(world, pos))
        return ActionResult.SUCCESS
    }

    return callSuper()
}

fun frame_onPlaced(
    world: World,
    pos: BlockPos,
    state: BlockState,
    placer: LivingEntity?,
    itemStack: ItemStack,
    callSuper: () -> Unit
) {
    val player = placer as? PlayerEntity ?: return callSuper()
    val hammer = player.offHandStack.takeIf { it.item == FRAMERS_HAMMER.value } ?: return callSuper()
    val tag = hammer.tag ?: return callSuper()
    val frameEntity = world.getBlockEntity(pos) as? FrameEntity ?: return callSuper()
    val hammerData = HammerData.fromTag(tag)

    hammerData.applySettings(frameEntity, player, world) { callSuper() }
}
