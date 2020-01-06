package dev.alexnader.framity.blocks

import dev.alexnader.framity.FRAMERS_HAMMER
import dev.alexnader.framity.block_entities.FrameEntity
import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.entity.EntityContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.Items
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.IWorld
import net.minecraft.world.World

abstract class BaseFrame: BlockWithEntity(FabricBlockSettings.of(Material.WOOD).hardness(0.33f).sounds(BlockSoundGroup.WOOD).nonOpaque().build()) {
    companion object {
        val HasGlowstone: BooleanProperty = BooleanProperty.of("has_glowstone")

        private val posToPlayer: MutableMap<BlockPos, PlayerEntity> = mutableMapOf()
    }

    init {
        this.defaultState = this.defaultState.with(HasGlowstone, false)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        super.appendProperties(builder)
        builder?.add(HasGlowstone)
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL
    override fun isTranslucent(state: BlockState?, view: BlockView?, pos: BlockPos?) = true
    override fun isSimpleFullBlock(state: BlockState?, view: BlockView?, pos: BlockPos?) = false
    override fun hasDynamicBounds() = true
    @Suppress("DEPRECATION")
    override fun isSideInvisible(state: BlockState, neighbor: BlockState, facing: Direction?) =
        neighbor.block == this || super.isSideInvisible(state, neighbor, facing)

    override fun onBlockRemoved(
        state: BlockState?, world: World?, pos: BlockPos?, newState: BlockState?, moved: Boolean
    ) {
        if (state?.block != newState?.block) {
            val blockEntity = world?.getBlockEntity(pos)

            if (blockEntity is Inventory) {
                ItemScatterer.spawn(world, pos, blockEntity)
                world.updateHorizontalAdjacent(pos, this)
            }

            @Suppress("DEPRECATION")
            super.onBlockRemoved(state, world, pos, newState, moved)
        }
    }

    override fun getLuminance(state: BlockState?) = if (state?.get(HasGlowstone) == true) 15 else 1

    private fun onHammerRemove(world: World, frameEntity: FrameEntity<*>, frameState: BlockState, player: PlayerEntity, giveItem: Boolean) {
        val slot = frameEntity.highestRemovePrioritySlot
        val stackFromBlock = frameEntity.takeInvStack(slot, 1)

        when (slot) {
            FrameEntity.ContainedSlot -> frameEntity.containedState = null
            FrameEntity.GlowstoneSlot -> world.setBlockState(frameEntity.pos, frameState.with(HasGlowstone, false))
        }

        if (giveItem) {
            player.inventory.offerOrDrop(world, stackFromBlock)
        }

        frameEntity.sync()
    }

    override fun onBlockBreakStart(state: BlockState?, world: World?, pos: BlockPos?, player: PlayerEntity?) {
        @Suppress("DEPRECATION")
        super.onBlockBreakStart(state, world, pos, player)

        if (world == null || world.isClient || pos == null || player == null || state == null) {
            return
        }

        if (player.isSneaking && player.getStackInHand(player.activeHand).item == FRAMERS_HAMMER.item) {
            onHammerRemove(world, world.getBlockEntity(pos) as FrameEntity<*>, state, player, true)
        }
    }

    override fun onBreak(world: World?, pos: BlockPos?, state: BlockState?, player: PlayerEntity?) {
        super.onBreak(world, pos, state, player)

        if (world?.isClient == true || pos == null || player == null || !player.isCreative) {
            return
        }

        posToPlayer[pos] = player
    }

    override fun onBroken(world: IWorld?, pos: BlockPos?, state: BlockState?) {
        super.onBroken(world, pos, state)

        if (world == null || world.isClient || pos == null || pos !in posToPlayer || state == null) {
            return
        }

        val player = posToPlayer[pos]!!

        if (player.isSneaking && player.getStackInHand(player.activeHand).item == FRAMERS_HAMMER.item) {
            world.setBlockState(pos, state, 3)

            onHammerRemove(world as World, world.getBlockEntity(pos) as FrameEntity<*>, state, player, false)

            posToPlayer.remove(pos)
        }
    }

    override fun onUse(
        state: BlockState?, world: World?, pos: BlockPos?, player: PlayerEntity?, hand: Hand?, hit: BlockHitResult?
    ): ActionResult {
        if (world?.isClient == true) return ActionResult.CONSUME

        val frameEntity = world?.getBlockEntity(pos) as FrameEntity<*>

        println("size: ${frameEntity.capacity}")

        val playerStack = player?.getStackInHand(hand)!!

        return if (playerStack.item is BlockItem && playerStack.item != frameEntity.item && playerStack.item !is BaseFrame) {
            val playerBlock = (playerStack.item as BlockItem).block

            if (!frameEntity.containedStack.isEmpty) {
                if (!player.isCreative) {
                    player.inventory.offerOrDrop(world, frameEntity.containedStack)
                }
            }

            val containedState = playerBlock.getPlacementState(ItemPlacementContext(ItemUsageContext(player, hand, hit)))!!

            @Suppress("DEPRECATION")
            if (playerBlock is BlockWithEntity && playerBlock.getRenderType(containedState) != BlockRenderType.MODEL) {
                return ActionResult.CONSUME
            }

            @Suppress("DEPRECATION")
            val outlineShape = playerBlock.getOutlineShape(containedState, world, pos, EntityContext.absent())

            if (VoxelShapes.fullCube().boundingBoxes != outlineShape.boundingBoxes) {
                return ActionResult.CONSUME
            }

            frameEntity.copyFrom(FrameEntity.ContainedSlot, playerStack, 1, take = !player.isCreative)
            frameEntity.containedState = containedState

            ActionResult.SUCCESS
        } else if (playerStack.item == Items.GLOWSTONE_DUST && frameEntity.glowstoneStack.isEmpty) {
            frameEntity.copyFrom(FrameEntity.GlowstoneSlot, playerStack, 1, take = !player.isCreative)

            world.setBlockState(pos, state?.with(HasGlowstone, true))

            ActionResult.SUCCESS
        } else {
            ActionResult.CONSUME
        }
    }
}