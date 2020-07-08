package dev.alexnader.framity.blocks

import dev.alexnader.framity.block_entities.FrameEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.FenceGateBlock
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World

class FenceGateFrame : FenceGateBlock(FRAME_SETTINGS), BlockEntityProvider, Frame {
    override fun createBlockEntity(view: BlockView) = FrameEntity()

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
        frame_isSideInvisible(state, stateFrom, direction, this) {
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
        frame_onStateReplaced(state, world, pos, newState, moved, this) {
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
