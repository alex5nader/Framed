@file:Suppress("FunctionName", "UNUSED_PARAMETER", "unused")

package dev.alexnader.framity.blocks

import dev.alexnader.framity.FRAMERS_HAMMER
import dev.alexnader.framity.SPECIAL_ITEMS
import dev.alexnader.framity.block_entities.FrameEntity
import dev.alexnader.framity.data.hasOverlay
import dev.alexnader.framity.items.HammerData
import dev.alexnader.framity.util.FixedSizeList
import dev.alexnader.framity.util.equalsIgnoring
import dev.alexnader.framity.util.minus
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
import net.minecraft.util.math.Vec3d
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
    stack.item in SPECIAL_ITEMS

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

fun <F> F.frameDefaultState(base: BlockState): BlockState
where
    F: Block,
    F: Frame
=
    base.with(Properties.LIT, false)
        .with(HAS_REDSTONE, false)

fun <F> F.frame_appendProperties(builder: StateManager.Builder<Block, BlockState>, callSuper: () -> Unit)
where
    F: Block,
    F: Frame
{
    callSuper()
    builder.add(Properties.LIT)
    builder.add(HAS_REDSTONE)
}

const val IS_TRANSLUCENT = true
const val HAS_DYNAMIC_BOUNDS = true
const val AMBIENT_OCCLUSION_LIGHT_LEVEL = 1f

fun <F> F.frame_emitsRedstonePower(state: BlockState): Boolean
where
    F: Block,
    F: Frame
=
    state[HAS_REDSTONE]

fun <F> F.frame_getWeakRedstonePower(state: BlockState, world: BlockView, pos: BlockPos, direction: Direction)
where
    F: Block,
    F: Frame
=
    if (state[HAS_REDSTONE]) 15 else 0

fun <F> F.frame_isSideInvisible(
    state: BlockState,
    stateFrom: BlockState,
    direction: Direction,
    callSuper: () -> Boolean
): Boolean
where
    F: Block,
    F: Frame
{
    return stateFrom.isOf(this) && frameStatesEqual(state, stateFrom) || callSuper()
}

fun <F> F.frame_onSyncedBlockEvent(state: BlockState, world: World, pos: BlockPos, type: Int, data: Int, callSuper: () -> Boolean): Boolean
where
    F: Block,
    F: Frame
{
    callSuper()
    return world.getBlockEntity(pos)?.onSyncedBlockEvent(type, data) ?: false
}

fun <F> F.frame_createScreenHandlerFactory(state: BlockState, world: World, pos: BlockPos): NamedScreenHandlerFactory?
where
    F: Block,
    F: Frame
{
    return world.getBlockEntity(pos) as? NamedScreenHandlerFactory
}

fun <F> F.frame_onStateReplaced(
    oldState: BlockState,
    world: World,
    pos: BlockPos,
    newState: BlockState,
    moved: Boolean,
    callSuper: () -> Unit
)
where
    F: Block,
    F: Frame
{
    if (world.isClient() || oldState.block === newState.block) {
        return
    }
    when (val player = posToPlayer.remove(pos)) {
        null -> {
            val blockEntity = world.getBlockEntity(pos)
            if (blockEntity is Inventory) {
                ItemScatterer.spawn(world, pos, blockEntity as Inventory?)
                world.updateComparators(pos, this)
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

fun <F> F.frame_onBlockBreakStart(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, callSuper: () -> Unit)
where
    F: Block,
    F: Frame
{
    callSuper()

    if (world.isClient()) {
        return
    }

    if (player.getStackInHand(player.activeHand).item === FRAMERS_HAMMER.value) {
        onHammerRemove(world, world.getBlockEntity(pos) as FrameEntity?, state, player, true)
    }
}

fun <F> F.frame_onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity, callSuper: () -> Unit)
where
    F: Block,
    F: Frame
{
    callSuper()

    if (world.isClient() || !player.isCreative) {
        return
    }
    
    posToPlayer[pos] = player
}

private fun swapItems(
    frameEntity: FrameEntity,
    slots: FixedSizeList<ItemStack>,
    absoluteSlot: Int,
    relativeSlot: Int,
    player: PlayerEntity,
    playerStack: ItemStack,
    world: World,
    onSuccess: (() -> Unit)? = null
): ActionResult {
    return if (playerStack.item != slots[relativeSlot].item) {
        if (!world.isClient) {
            if (!player.isCreative && !slots[relativeSlot].isEmpty) {
                player.inventory.offerOrDrop(world, slots[relativeSlot])
            }
            frameEntity.copyFrom(absoluteSlot, playerStack, 1, !player.isCreative)
            onSuccess?.invoke()
        }
        ActionResult.SUCCESS
    } else {
        ActionResult.CONSUME
    }
}

fun <F> F.frame_onUse(
    state: BlockState,
    world: World,
    pos: BlockPos,
    player: PlayerEntity,
    hand: Hand,
    hit: BlockHitResult,
    callSuper: () -> ActionResult?
): ActionResult?
where
    F: Block,
    F: Frame
{
    val frameEntity = world.getBlockEntity(pos) as? FrameEntity ?: return ActionResult.CONSUME

    val playerStack = player.mainHandStack

    if (playerStack != null) {
        val posInBlock = hit.pos - Vec3d.of(hit.blockPos)
        val slotOffset = this.getSlotFor(state, posInBlock, hit.side)

        if (validForOverlay(playerStack)) {
            @Suppress("UnnecessaryVariable")
            val overlaySlot = slotOffset
            val absoluteSlot = frameEntity.format.overlay.applyOffset(overlaySlot)
            return swapItems(frameEntity, frameEntity.overlayItems, absoluteSlot, overlaySlot, player, playerStack, world)
        }

        val maybeBaseState = validForBase(
            playerStack,
            { bi -> bi.block.getPlacementState(ItemPlacementContext(ItemUsageContext(player, hand, hit))) },
            world,
            pos
        )

        if (maybeBaseState != null) {
            @Suppress("UnnecessaryVariable")
            val baseSlot = slotOffset
            val absoluteSlot = frameEntity.format.base.applyOffset(baseSlot)
            return swapItems(frameEntity, frameEntity.baseItems, absoluteSlot, baseSlot, player, playerStack, world) {
                frameEntity.baseStates[baseSlot] = maybeBaseState
            }
        }

        if (validForSpecial(playerStack)) {
            val specialItem = SPECIAL_ITEMS[playerStack.item] ?: error("Invalid special item: ${playerStack.item}")
            val slot = frameEntity.format.special.applyOffset(specialItem.offset)
            return if (frameEntity.getStack(slot).isEmpty) {
                if (!world.isClient) {
                    frameEntity.copyFrom(slot, playerStack, 1, !player.isCreative)
                    specialItem.data.onAdd(world, frameEntity)
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

fun <F> F.frame_onPlaced(
    world: World,
    pos: BlockPos,
    state: BlockState,
    placer: LivingEntity?,
    itemStack: ItemStack,
    callSuper: () -> Unit
)
where
    F: Block,
    F: Frame
{
    val player = placer as? PlayerEntity ?: return callSuper()
    val hammer = player.offHandStack.takeIf { it.item == FRAMERS_HAMMER.value } ?: return callSuper()
    val tag = hammer.tag ?: return callSuper()
    val frameEntity = world.getBlockEntity(pos) as? FrameEntity ?: return callSuper()
    val hammerData = HammerData.fromTag(tag)

    hammerData.applySettings(this, state, frameEntity, player, world) { callSuper() }
}
