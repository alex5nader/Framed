@file:Suppress("FunctionName", "UNUSED_PARAMETER")

package dev.alexnader.framity.blocks

import dev.alexnader.framity.FRAMERS_HAMMER
import dev.alexnader.framity.SPECIAL_ITEM_DATA
import dev.alexnader.framity.block_entities.FrameEntity
import dev.alexnader.framity.data.hasOverlay
import dev.alexnader.framity.items.HammerData
import dev.alexnader.framity.util.equalsIgnoring
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
import net.minecraft.sound.SoundEvents
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
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
        frameEntity.items.indices.forEach(::removeStack)
    } else {
        removeStack(frameEntity.items.lastNonEmptyIndex)
    }

    frameEntity.markDirty()
}

fun validForSpecial(stack: ItemStack) =
    stack.item in SPECIAL_ITEM_DATA

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

fun frame_createScreenHandlerFactory(state: BlockState, world: World, pos: BlockPos): NamedScreenHandlerFactory? {
    return world.getBlockEntity(pos) as? NamedScreenHandlerFactory
}

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
        if (!frameEntity.overlayItems.isFull() && validForOverlay(playerStack)) {
            if (!world.isClient) {
                val slotOffset = frameEntity.overlayItems.firstEmptyIndex
                val slot = frameEntity.format.overlay.applyOffset(slotOffset)
                frameEntity.copyFrom(slot, playerStack, 1, !player.isCreative)
            }
            return ActionResult.SUCCESS
        }

        val maybeBaseState = validForBase(
            playerStack,
            { bi -> bi.block.getPlacementState(ItemPlacementContext(ItemUsageContext(player, hand, hit))) },
            world,
            pos
        )

        //TODO: extract behaviour with base item as param instead of baseItems[0]
        if (maybeBaseState != null) {
            return if (playerStack.item !== frameEntity.baseItems[0].item) {
                if (!world.isClient) {
                    if (!frameEntity.baseItems[0].isEmpty && !player.isCreative) {
                        player.inventory.offerOrDrop(world, frameEntity.baseItems[0])
                    }
                    frameEntity.copyFrom(frameEntity.format.base.applyOffset(0), playerStack, 1, !player.isCreative)
                    frameEntity.baseStates[0] = maybeBaseState
                }
                ActionResult.SUCCESS
            } else {
                ActionResult.CONSUME
            }
        }

        if (validForSpecial(playerStack)) {
            val specialItem = SPECIAL_ITEM_DATA[playerStack.item] ?: error("Invalid special item: ${playerStack.item}")
            val slot = frameEntity.format.special.applyOffset(specialItem.first)
            return if (frameEntity.getStack(slot).isEmpty) {
                if (!world.isClient) {
                    frameEntity.copyFrom(slot, playerStack, 1, !player.isCreative)
                    specialItem.second.onAdd(world, frameEntity)
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
