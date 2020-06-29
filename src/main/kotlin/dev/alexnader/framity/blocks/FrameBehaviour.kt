package dev.alexnader.framity.blocks

import dev.alexnader.framity.FRAMERS_HAMMER
import dev.alexnader.framity.block_entities.FrameEntity
import dev.alexnader.framity.block_entities.FrameEntity.Companion.OtherItem
import dev.alexnader.framity.data.hasOverlay
import net.fabricmc.fabric.api.tag.TagRegistry
import net.minecraft.block.*
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

/**
 * The `framity:frames` tag.
 */
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

fun validForOther(stack: ItemStack) =
    stack.item in FrameEntity.OTHER_ITEM_DATA

fun validForOverlay(stack: ItemStack) =
    hasOverlay(stack)

fun validForBase(stack: ItemStack, toState: (BlockItem) -> BlockState?, world: World, pos: BlockPos): BlockState? {
    val item = stack.item
    if (item == null || item !is BlockItem) {
        return null
    }

    if (FramesTag.contains(item.block)) {
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

@Suppress("UNUSED_PARAMETER")
fun frame_getWeakRedstonePower(state: BlockState, world: BlockView, pos: BlockPos, direction: Direction) =
    if (state[HAS_REDSTONE]) 15 else 0

@Suppress("UNUSED_PARAMETER")
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
            if (player.isSneaking && player.getStackInHand(player.activeHand).item === FRAMERS_HAMMER.value) {
                onHammerRemove(world, world.getBlockEntity(pos) as FrameEntity<*>?, oldState, player, false)
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

    if (player.isSneaking && player.getStackInHand(player.activeHand).item === FRAMERS_HAMMER.value) {
        onHammerRemove(world, world.getBlockEntity(pos) as FrameEntity<*>?, state, player, true)
    }
}

@Suppress("UNUSED_PARAMETER")
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
    val frameEntity = world.getBlockEntity(pos) as FrameEntity<*>? ?: return ActionResult.CONSUME
    
    val playerStack = player.mainHandStack

    if (!world.isClient && playerStack != null) {
        if (frameEntity.overlayStack.isEmpty && validForOverlay(playerStack)) {
            frameEntity.copyFrom(FrameEntity.OVERLAY_SLOT, playerStack, 1, !player.isCreative)
            frameEntity.markDirty()
            return ActionResult.SUCCESS
        }

        val maybeBaseState = validForBase(
            playerStack,
            { bi -> bi.block.getPlacementState(ItemPlacementContext(ItemUsageContext(player, hand, hit))) },
            world,
            pos
        )

        if (playerStack.item !== frameEntity.baseStack.item && maybeBaseState != null) {
            if (!frameEntity.baseStack.isEmpty && !player.isCreative) {
                player.inventory.offerOrDrop(world, frameEntity.baseStack)
            }
            frameEntity.copyFrom(FrameEntity.BASE_SLOT, playerStack, 1, !player.isCreative)
            frameEntity.baseState = maybeBaseState
            return ActionResult.SUCCESS
        }

        if (validForOther(playerStack)) {
            val otherItem: OtherItem? = FrameEntity.OTHER_ITEM_DATA[playerStack.item]
            return if (frameEntity.getStack(otherItem!!.slot).isEmpty) {
                frameEntity.copyFrom(otherItem.slot, playerStack, 1, !player.isCreative)
                println("Running onAdd")
                otherItem.onAdd(world, frameEntity)
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
    
    return callSuper().let {
        if (it == ActionResult.PASS) {
            ActionResult.CONSUME
        } else {
            it
        }
    }
}
