package dev.alexnader.framity.blocks

import dev.alexnader.framity.FRAMERS_HAMMER
import dev.alexnader.framity.block_entities.FrameEntity
import dev.alexnader.framity.data.OverlayKind
import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.fabricmc.fabric.api.tag.TagRegistry
import net.minecraft.block.*
import net.minecraft.entity.EntityContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.*
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.EnumProperty
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

/**
 * Base class for frame blocks.
 */
abstract class BaseFrame: BlockWithEntity(FabricBlockSettings.of(Material.WOOD).hardness(0.33f).sounds(BlockSoundGroup.WOOD).nonOpaque().build()) {
    companion object {
        /**
         * Property representing whether or not a frame should give off light.
         */
        val HasGlowstone: BooleanProperty = BooleanProperty.of("has_glowstone")

        /**
         * Property representing what overlay a frame has.
         */
        val OverlayKindProp: EnumProperty<OverlayKind> = EnumProperty.of("overlay", OverlayKind::class.java)

        /**
         * Maps [BlockPos] to [PlayerEntity]. Should be empty except for when a frame is broken.
         */
        private val posToPlayer: MutableMap<BlockPos, PlayerEntity> = mutableMapOf()

        /**
         * The `framity:overlay_items` tag.
         */
        private val OverlayItemsTag: Tag<Item> = TagRegistry.item(Identifier("framity", "overlay_items"))
    }

    init {
        this.defaultState = this.defaultState.with(HasGlowstone, false).with(OverlayKindProp, OverlayKind.None)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        super.appendProperties(builder)
        builder?.add(HasGlowstone)
        builder?.add(OverlayKindProp)
    }

    /**
     * Frames always render via [BlockRenderType.MODEL].
     */
    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

    /**
     * Frames are always translucent.
     */
    override fun isTranslucent(state: BlockState?, view: BlockView?, pos: BlockPos?) = true

    /**
     * Frames are not a simple full block.
     */
    override fun isSimpleFullBlock(state: BlockState?, view: BlockView?, pos: BlockPos?) = false

    /**
     * Frames always have dynamic bounds.
     */
    override fun hasDynamicBounds() = true

    /**
     * Frames only render a side as invisible if the other block is the same kind of block as this one.
     */
    @Suppress("DEPRECATION")
    override fun isSideInvisible(state: BlockState, neighbor: BlockState, facing: Direction?) =
        neighbor.block == this || super.isSideInvisible(state, neighbor, facing)

    /**
     * Drops inventory onto ground.
     */
    override fun onBlockRemoved(
        state: BlockState?, world: World?, pos: BlockPos?, newState: BlockState?, moved: Boolean
    ) {
        if (world == null || world.isClient || pos == null || state == null || state.block == newState?.block) {
            return
        }

        if (pos in posToPlayer) {
            val player = posToPlayer[pos]!!

            if (player.isCreative && player.isSneaking && player.getStackInHand(player.activeHand).item == FRAMERS_HAMMER.item) {
                onHammerRemove(world, world.getBlockEntity(pos) as FrameEntity<*>, state, player, false)
            }

            posToPlayer.remove(pos)
        } else {
            val blockEntity = world.getBlockEntity(pos)

            if (blockEntity is Inventory) {
                ItemScatterer.spawn(world, pos, blockEntity)
                world.updateHorizontalAdjacent(pos, this)
            }

            @Suppress("DEPRECATION")
            super.onBlockRemoved(state, world, pos, newState, moved)
        }
    }

    /**
     * Returns a light level of 15 if the frame has glowstone dust.
     * Returns a light level of 0 otherwise.
     */
    override fun getLuminance(state: BlockState?) = if (state?.get(HasGlowstone) == true) 15 else 0

    /**
     * Called on server when left clicked by a player holding a framer's hammer.
     * Removes the "rightmost" item from [frameEntity].
     * Returns false if the frame should be removed, true otherwise.
     */
    private fun onHammerRemove(world: World, frameEntity: FrameEntity<*>, frameState: BlockState, player: PlayerEntity, giveItem: Boolean): Boolean {
        val slot = frameEntity.highestRemovePrioritySlot
        val stackFromBlock = frameEntity.takeInvStack(slot, 1)

        if (slot == -1) {
            return false
        }

        if (slot == FrameEntity.ContainedSlot) {
            frameEntity.containedState = null
        }

        val changedState = when (slot) {
            FrameEntity.ContainedSlot -> frameState
            FrameEntity.GlowstoneSlot -> frameState.with(HasGlowstone, false)
            FrameEntity.OverlaySlot -> frameState.with(OverlayKindProp, OverlayKind.None)
            else -> throw RuntimeException("unreachable")
        }

        world.playSound(null, frameEntity.pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS,
            0.2f,
            (Random.nextFloat() - Random.nextFloat()) * 1.4F + 2.0F)

        world.setBlockState(frameEntity.pos, changedState)

        if (giveItem) {
            player.inventory.offerOrDrop(world, stackFromBlock)
        }

        frameEntity.sync()

        return true
    }

    /**
     * Calls [onHammerRemove] when on server.
     */
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

    /**
     * Marks [player] as having broken the frame at [pos] so [onHammerRemove] can be called after the break is cancelled.
     */
    override fun onBreak(world: World?, pos: BlockPos?, state: BlockState?, player: PlayerEntity?) {
        super.onBreak(world, pos, state, player)

        if (world?.isClient == true || pos == null || player == null || !player.isCreative) {
            return
        }

        posToPlayer[pos] = player
    }

    /**
     * Adds an item to the frame.
     */
    override fun onUse(
        state: BlockState?, world: World?, pos: BlockPos?, player: PlayerEntity?, hand: Hand?, hit: BlockHitResult?
    ): ActionResult {
        if (world?.isClient == true) return ActionResult.CONSUME

        val frameEntity = world?.getBlockEntity(pos) as FrameEntity<*>

        val playerStack = player?.getStackInHand(hand)!!

        return if (playerStack.item.isIn(OverlayItemsTag) && frameEntity.overlayStack.isEmpty) {
            frameEntity.copyFrom(FrameEntity.OverlaySlot, playerStack, 1, !player.isCreative)

            world.setBlockState(pos, state?.with(OverlayKindProp, OverlayKind.from(frameEntity.overlayStack.item)!!))

            ActionResult.SUCCESS
        } else if (playerStack.item is BlockItem && playerStack.item != frameEntity.item && playerStack.item !is BaseFrame) {
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

            frameEntity.copyFrom(FrameEntity.ContainedSlot, playerStack, 1, !player.isCreative)
            frameEntity.containedState = containedState

            ActionResult.SUCCESS
        } else if (playerStack.item == Items.GLOWSTONE_DUST && frameEntity.glowstoneStack.isEmpty) {
            frameEntity.copyFrom(FrameEntity.GlowstoneSlot, playerStack, 1, !player.isCreative)

            world.setBlockState(pos, state?.with(HasGlowstone, true))

            ActionResult.SUCCESS
        } else {
            ActionResult.CONSUME
        }
    }
}
