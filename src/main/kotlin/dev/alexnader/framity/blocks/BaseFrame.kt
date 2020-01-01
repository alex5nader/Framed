package dev.alexnader.framity.blocks

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
import net.minecraft.world.World

abstract class BaseFrame: Block(FabricBlockSettings.of(Material.WOOD).hardness(0.33f).sounds(BlockSoundGroup.WOOD).nonOpaque().build()), BlockEntityProvider {

    override fun isTranslucent(state: BlockState?, view: BlockView?, pos: BlockPos?) = true
    override fun isSimpleFullBlock(state: BlockState?, view: BlockView?, pos: BlockPos?) = false
    override fun isSideInvisible(state: BlockState, neighbor: BlockState, facing: Direction?): Boolean {
        return neighbor.block == this || super.isSideInvisible(state, neighbor, facing)
    }
    override fun hasDynamicBounds() = true

    override fun onBlockRemoved(
        state: BlockState?, world: World?, pos: BlockPos?, newState: BlockState?, moved: Boolean
    ) {
        if (state?.block != newState?.block) {
            val blockEntity = world?.getBlockEntity(pos)

            if (blockEntity is Inventory) {
                ItemScatterer.spawn(world, pos, blockEntity)
                world.updateHorizontalAdjacent(pos, this)
            }

            super.onBlockRemoved(state, world, pos, newState, moved)
        }
    }

    override fun onUse(
        state: BlockState?, world: World?, pos: BlockPos?, player: PlayerEntity?, hand: Hand?, hit: BlockHitResult?
    ): ActionResult {
        if (world?.isClient == true) return ActionResult.CONSUME

        val frameEntity = world?.getBlockEntity(pos) as FrameEntity<*>

        val playerStack = player?.getStackInHand(hand)!!

        return if (!playerStack.isEmpty) {
            if (playerStack.item !is BlockItem || playerStack.item.id == frameEntity.item.id) {
                return ActionResult.CONSUME
            }

            val playerBlock = (playerStack.item as BlockItem).block

            if (playerBlock is BaseFrame) {
                return ActionResult.CONSUME
            }

            val outlineShape = playerBlock.getOutlineShape(
                playerBlock.defaultState, world, pos, EntityContext.absent()
            )

            if (VoxelShapes.fullCube().boundingBox != outlineShape.boundingBox) {
                return ActionResult.CONSUME
            }

            if (!frameEntity.stack.isEmpty) {
                if (!player.isCreative) {
                    player.inventory.offerOrDrop(world, frameEntity.stack)
                }
            } else {
                frameEntity.disguised = true
            }

            val newStackForFrame = playerStack.copy()
            newStackForFrame.count = 1

            if (!player.isCreative) {
                playerStack.count -= 1
            }

            frameEntity.containedState = playerBlock.getPlacementState(ItemPlacementContext(ItemUsageContext(player, hand, hit)))!!
            frameEntity.stack = newStackForFrame

            ActionResult.SUCCESS
        } else if (player.isSneaking && !frameEntity.stack.isEmpty) {

            val stackFromBlock = frameEntity.takeInvStack(0, 1)
            frameEntity.containedState = null
            frameEntity.disguised = false

            if (!player.isCreative) {
                player.inventory.offerOrDrop(world, stackFromBlock)
            }

            ActionResult.SUCCESS
        } else {
            ActionResult.CONSUME
        }.also {
            if (it == ActionResult.SUCCESS) {
                frameEntity.sync()
            }
        }
    }
}