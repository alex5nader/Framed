package dev.alexnader.framity.blocks

import dev.alexnader.framity.FRAMERS_HAMMER
import dev.alexnader.framity.block_entities.FrameEntity
import dev.alexnader.framity.adapters.id
import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.entity.EntityContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemUsageContext
import net.minecraft.sound.BlockSoundGroup
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
        private val posToPlayer: MutableMap<BlockPos, PlayerEntity> = mutableMapOf()
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
        if (state?.block != newState?.block && pos !in posToPlayer) {
            val blockEntity = world?.getBlockEntity(pos)

            if (blockEntity is Inventory) {
                ItemScatterer.spawn(world, pos, blockEntity)
                world.updateHorizontalAdjacent(pos, this)
            }

            @Suppress("DEPRECATION")
            super.onBlockRemoved(state, world, pos, newState, moved)
        }
    }

    private fun removeItemFromFrame(world: World, frameEntity: FrameEntity<*>, player: PlayerEntity, giveItem: Boolean) {
        val stackFromBlock = frameEntity.takeInvStack(0, 1)
        frameEntity.containedState = null

        if (giveItem) {
            player.inventory.offerOrDrop(world, stackFromBlock)
        }

        frameEntity.sync()
    }

    override fun onBlockBreakStart(state: BlockState?, world: World?, pos: BlockPos?, player: PlayerEntity?) {
        @Suppress("DEPRECATION")
        super.onBlockBreakStart(state, world, pos, player)

        if (world == null || world.isClient || pos == null || player == null) {
            return
        }

        if (player.isSneaking && player.getStackInHand(player.activeHand).item == FRAMERS_HAMMER.item) {
            removeItemFromFrame(world, world.getBlockEntity(pos) as FrameEntity<*>, player, true)
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

        if (world == null || world.isClient || pos == null || pos !in posToPlayer) {
            return
        }

        val player = posToPlayer[pos]!!

        if (player.isSneaking && player.getStackInHand(player.activeHand).item == FRAMERS_HAMMER.item) {
            world.setBlockState(pos, state, 3)

            removeItemFromFrame(world as World, world.getBlockEntity(pos) as FrameEntity<*>, player, false)

            posToPlayer.remove(pos)
        }
    }

    override fun onUse(
        state: BlockState?, world: World?, pos: BlockPos?, player: PlayerEntity?, hand: Hand?, hit: BlockHitResult?
    ): ActionResult {
        if (world?.isClient == true) return ActionResult.CONSUME

        val frameEntity = world?.getBlockEntity(pos) as FrameEntity<*>

        val playerStack = player?.getStackInHand(hand)!!

        return if (playerStack.item is BlockItem && playerStack.item != frameEntity.item && playerStack.item !is BaseFrame) {
            val playerBlock = (playerStack.item as BlockItem).block

            if (!frameEntity.stack.isEmpty) {
                if (!player.isCreative) {
                    player.inventory.offerOrDrop(world, frameEntity.stack)
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

            val newStackForFrame = playerStack.copy()
            newStackForFrame.count = 1

            if (!player.isCreative) {
                playerStack.count -= 1
            }

            frameEntity.containedState = containedState
            frameEntity.stack = newStackForFrame

            frameEntity.sync()

            ActionResult.SUCCESS
        } else {
            ActionResult.CONSUME
        }
    }
}