package dev.alexnader.framity.block.frame

import java.{lang => jl}

import dev.alexnader.framity.block.frame.FrameData.Sections.SlotSectionConversions
import dev.alexnader.framity.block.frame.Valid.ItemStackValidQuery
import dev.alexnader.framity.block_entity.FrameEntity
import dev.alexnader.framity.item.FramersHammer
import dev.alexnader.framity.mixin.DefaultStateAccess
import dev.alexnader.framity.util.MinecraftUtil.Vec3dExt
import net.minecraft.block.{Block, BlockEntityProvider, BlockState}
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.{ItemPlacementContext, ItemStack, ItemUsageContext}
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.sound.{SoundCategory, SoundEvents}
import net.minecraft.state.StateManager
import net.minecraft.state.property.{BooleanProperty, Properties}
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.{BlockPos, Direction, Vec3d}
import net.minecraft.util.{ActionResult, Hand, ItemScatterer}
import net.minecraft.world.{BlockView, World}

import scala.collection.{IndexedSeqView, mutable}
import scala.util.Random

object FrameBlock {
  object Properties {
    val HasRedstone: BooleanProperty = BooleanProperty.of("has_redstone")
  }
}

//noinspection ScalaDeprecation
trait FrameBlock extends Block with BlockEntityProvider with FrameAccess {
  this
    .asInstanceOf[DefaultStateAccess]
    .setDefaultStateWorkaround(this.getDefaultState.`with`[jl.Boolean, jl.Boolean](Properties.LIT, false).`with`[jl.Boolean, jl.Boolean](FrameBlock.Properties.HasRedstone, false))

  private val posToPlayer: mutable.Map[BlockPos, PlayerEntity] = mutable.Map()

  override def appendProperties(builder: StateManager.Builder[Block, BlockState]): Unit = {
    super.appendProperties(builder)
    builder.add(Properties.LIT, FrameBlock.Properties.HasRedstone)
  }

  override def emitsRedstonePower(state: BlockState): Boolean = state.get(FrameBlock.Properties.HasRedstone)

  override def getWeakRedstonePower(state: BlockState, world: BlockView, pos: BlockPos, direction: Direction): Int =
    if (state.get(FrameBlock.Properties.HasRedstone)) 15 else 0

  override def isSideInvisible(state: BlockState, stateFrom: BlockState, direction: Direction): Boolean = {
    super.isSideInvisible(state, stateFrom, direction) || stateFrom.isOf(this)
  }

  override def onSyncedBlockEvent(state: BlockState, world: World, pos: BlockPos, `type`: Int, data: Int): Boolean = {
    super.onSyncedBlockEvent(state, world, pos, `type`, data)
    world.getBlockEntity(pos) match {
      case null => false
      case blockEntity => blockEntity.onSyncedBlockEvent(`type`, data)
    }
  }

  override def createScreenHandlerFactory(state: BlockState, world: World, pos: BlockPos): NamedScreenHandlerFactory =
    world.getBlockEntity(pos).asInstanceOf[NamedScreenHandlerFactory]

  def onHammerRemove(world: World, frameEntity: FrameEntity, state: BlockState, player: PlayerEntity, giveItem: Boolean): Unit = {
    def removeStack(slot: Int): Unit = {
      val stack = frameEntity.removeStack(slot, 1)
      if (!stack.isEmpty && giveItem) {
        player.inventory.offerOrDrop(world, stack)

        world.playSound(null, frameEntity.getPos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f, (Random.nextFloat() - Random.nextFloat()) * 1.4f + 2f)
      }
    }

    world.setBlockState(frameEntity.getPos, state)

    if (player.isSneaking) {
      frameEntity.sections.itemIndices.foreach(removeStack)
    } else {
      val slot = frameEntity.items.lastIndexWhere(_.isDefined)
      if (slot != -1) {
        removeStack(slot)
      }
    }

    frameEntity.markDirty()
  }

  override def onStateReplaced(oldState: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean): Unit = {
    if (world.isClient || oldState.getBlock == newState.getBlock) {
      return
    }

    val blockEntity = world.getBlockEntity(pos)

    posToPlayer.get(pos) match {
      case None =>
        blockEntity match {
          case inventory: Inventory =>
            ItemScatterer.spawn(world, pos, inventory)
        }
        super.onStateReplaced(oldState, world, pos, newState, moved)
      case Some(player) =>
        if (player.getStackInHand(player.getActiveHand).getItem == FramersHammer) {
          onHammerRemove(world, blockEntity.asInstanceOf[FrameEntity], oldState, player, giveItem = false)
        } else {
          super.onStateReplaced(oldState, world, pos, newState, moved)
        }
    }
  }

  override def onBlockBreakStart(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity): Unit = {
    super.onBlockBreakStart(state, world, pos, player)

    if (world.isClient) {
      return
    }

    if (player.getStackInHand(player.getActiveHand).getItem eq FramersHammer) {
      world.getBlockEntity(pos) match {
        case frameEntity: FrameEntity =>
          onHammerRemove(world, frameEntity, state, player, giveItem = true)
        case _ =>
      }
    }
  }

  override def onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity): Unit = {
    super.onBreak(world, pos, state, player)

    if (world.isClient || !player.isCreative) {
      return
    }

    posToPlayer(pos) = player
  }

  override def onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult = {
    val frameEntity = world.getBlockEntity(pos) match {
      case frameEntity: FrameEntity => frameEntity
      case _ => return ActionResult.CONSUME
    }

    val playerStack = player.getMainHandStack

    if (playerStack != null) {
      val posInBlock = hit.getPos - Vec3d.of(hit.getBlockPos)
      val relativeSlot = this.getRelativeSlotAt(state, posInBlock, hit.getSide)

      def swapItems(slots: IndexedSeqView[Option[ItemStack]], absoluteSlot: Int, onSuccess: => Unit = ()): ActionResult = {
        if (playerStack.getItem != slots(relativeSlot).map(_.getItem).getOrElse(ItemStack.EMPTY)) {
          if (!world.isClient) {
            if (!player.isCreative && slots(relativeSlot).isDefined) {
              player.inventory.offerOrDrop(world, slots(relativeSlot).get)
            }
            frameEntity.copyFrom(absoluteSlot, playerStack, 1, !player.isCreative)
            onSuccess
          }
          ActionResult.SUCCESS
        } else {
          ActionResult.CONSUME
        }
      }

      if (playerStack.isValidForOverlay) {
        val absoluteSlot = relativeSlot.toAbsolute(frameEntity.sections.overlay)
        return swapItems(frameEntity.overlayItems, absoluteSlot)
      }

      playerStack.isValidForBase(bi => Some(bi.getBlock.getPlacementState(new ItemPlacementContext(new ItemUsageContext(player, hand, hit)))), world, pos) match {
        case Some(baseState) =>
          val absoluteSlot = relativeSlot.toAbsolute(frameEntity.sections.base)
          return swapItems(frameEntity.baseItems, absoluteSlot, { frameEntity.baseStates(relativeSlot) = Some(baseState) })
        case _ =>
      }

      if (playerStack.isValidForSpecial) {
        val specialItem = SpecialItems.map(playerStack.getItem)
        val slot = specialItem.offset.toAbsolute(frameEntity.sections.special)
        return if (frameEntity.getStack(slot).isEmpty) {
          if (!world.isClient) {
            frameEntity.copyFrom(slot, playerStack, 1, !player.isCreative)
            specialItem.onAdd(world, frameEntity)
          }
          ActionResult.SUCCESS
        } else {
          ActionResult.CONSUME
        }
      }

      if (playerStack.isEmpty && player.isSneaking) {
        player.openHandledScreen(state.createScreenHandlerFactory(world, pos))
        return ActionResult.SUCCESS
      }
    }

    super.onUse(state, world, pos, player, hand, hit)
  }

  override def onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity, itemStack: ItemStack): Unit = {
    def tryCopy(): Boolean = {
      placer match {
        case player: PlayerEntity =>
          if (!(player.getOffHandStack.getItem eq FramersHammer) || player.getOffHandStack.getTag == null) {
            return false
          }
          world.getBlockEntity(pos) match {
            case frameEntity: FrameEntity =>
              FramersHammer.Data.fromTag(player.getOffHandStack.getTag).applySettings(this, state, frameEntity, player, world)
            case _ => false
          }
        case _ => false
      }
    }

    if (!tryCopy()) {
      super.onPlaced(world, pos, state, placer, itemStack)
    }
  }
}
