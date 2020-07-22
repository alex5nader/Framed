package dev.alexnader.framity.blocks

import dev.alexnader.framity.block_entities.FrameEntity
import dev.alexnader.framity.util.FrameDataFormat
import net.minecraft.block.*
import net.minecraft.block.enums.SlabType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.BlockView
import net.minecraft.world.World

class SlabFrame : SlabBlock(FRAME_SETTINGS), BlockEntityProvider, Frame {
    companion object {
        val FORMAT = FrameDataFormat(2)

        private const val LOWER_SLOT = 0
        private const val UPPER_SLOT = 1
    }

    override fun createBlockEntity(view: BlockView) = FrameEntity(FORMAT)

    override val format = FORMAT
    override fun getSlotFor(
        state: BlockState,
        posInBlock: Vec3d,
        side: Direction
    ) =
        when (side) {
            Direction.UP -> if (posInBlock.y == 0.5) LOWER_SLOT else UPPER_SLOT
            Direction.DOWN -> if (posInBlock.y == 0.5) UPPER_SLOT else LOWER_SLOT
            else -> if (posInBlock.y < 0.5) LOWER_SLOT else UPPER_SLOT
        }
    override fun slotIsValid(state: BlockState, slot: Int): Boolean {
        val wantedSlot = when (state.get(Properties.SLAB_TYPE)) {
            SlabType.DOUBLE -> return true
            SlabType.TOP -> UPPER_SLOT
            SlabType.BOTTOM -> LOWER_SLOT
            null -> error("Slab type is null.")
        }
        return when (format.getSectionIndex(slot)) {
            FrameDataFormat.BASE_INDEX -> format.base.findOffset(slot) == wantedSlot
            FrameDataFormat.OVERLAY_INDEX -> format.overlay.findOffset(slot) == wantedSlot
            FrameDataFormat.SPECIAL_INDEX -> true // special items are not dependent on slab half
            else -> error("Frame slab should not have other data.")
        }
    }

    init {
        this.defaultState = frameDefaultState(this.defaultState)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) =
        frame_appendProperties(builder) { super.appendProperties(builder) }

    override fun isTranslucent(state: BlockState, world: BlockView, pos: BlockPos) =
        IS_TRANSLUCENT

    override fun hasDynamicBounds() =
        HAS_DYNAMIC_BOUNDS

    override fun getAmbientOcclusionLightLevel(state: BlockState, world: BlockView, pos: BlockPos) =
        AMBIENT_OCCLUSION_LIGHT_LEVEL

    override fun emitsRedstonePower(state: BlockState) =
        frame_emitsRedstonePower(state)

    override fun getWeakRedstonePower(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        direction: Direction
    ) =
        frame_getWeakRedstonePower(state, world, pos, direction)

    override fun isSideInvisible(state: BlockState, stateFrom: BlockState, direction: Direction) =
        frame_isSideInvisible(state, stateFrom, direction) {
            @Suppress("DEPRECATION")
            super.isSideInvisible(state, stateFrom, direction)
        }

    override fun onSyncedBlockEvent(state: BlockState, world: World, pos: BlockPos, type: Int, data: Int) =
        frame_onSyncedBlockEvent(state, world, pos, type, data) {
            @Suppress("DEPRECATION")
            super.onSyncedBlockEvent(state, world, pos, type, data)
        }

    override fun createScreenHandlerFactory(
        state: BlockState,
        world: World,
        pos: BlockPos
    ) =
        frame_createScreenHandlerFactory(state, world, pos)

    override fun onStateReplaced(
        state: BlockState,
        world: World,
        pos: BlockPos,
        newState: BlockState,
        moved: Boolean
    ) =
        frame_onStateReplaced(state, world, pos, newState, moved) {
            @Suppress("DEPRECATION")
            super.onStateReplaced(state, world, pos, newState, moved)
        }

    override fun onBlockBreakStart(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity) =
        frame_onBlockBreakStart(state, world, pos, player) {
            @Suppress("DEPRECATION")
            super.onBlockBreakStart(state, world, pos, player)
        }

    override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity) =
        frame_onBreak(world, pos, state, player) { super.onBreak(world, pos, state, player) }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult
    ) =
        frame_onUse(state, world, pos, player, hand, hit) {
            @Suppress("DEPRECATION")
            super.onUse(state, world, pos, player, hand, hit)
        }

    override fun onPlaced(
        world: World,
        pos: BlockPos,
        state: BlockState,
        placer: LivingEntity?,
        itemStack: ItemStack
    ) =
        frame_onPlaced(world, pos, state, placer, itemStack) { super.onPlaced(world, pos, state, placer, itemStack) }
}
