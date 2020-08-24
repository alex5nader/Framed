package dev.alexnader.framity.item

import dev.alexnader.framity.block.frame.{FrameAccess, FrameData}
import dev.alexnader.framity.block_entity.FrameEntity
import dev.alexnader.framity.util.MinecraftUtil.IterableInventory
import net.minecraft.block.BlockState
import net.minecraft.client.item.ModelPredicateProvider
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.{Item, ItemStack, ItemUsageContext}
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.TranslatableText
import net.minecraft.util.{ActionResult, Hand, TypedActionResult}
import net.minecraft.world.World

import scala.language.implicitConversions

object FramersHammer extends Item(new Item.Settings().maxCount(1)) {

  object Mode extends Enumeration {

    protected case class Val(translationKey: String) extends super.Val {
      def next: Val = Mode((id + 1) % maxId).asInstanceOf[Val]
    }

    implicit def valueToVal(value: Value): Val = value.asInstanceOf[Val]

    val CopyNone: Value = Val("no_copy")
    val CopyPartial: Value = Val("partial_copy")
    val CopyAll: Value = Val("whole_copy")

    def default: Value = CopyNone

    def fromString(string: String): Option[Value] = values.find(_.toString == string)
    def fromStringOrDefault(string: String): Value = fromString(string).getOrElse(default)
  }

  object Data {
    def fromTag(tag: CompoundTag): Data = {
      Data(
        if (tag contains "storedData") Some(FrameData.fromTag(tag.getCompound("storedData"))) else None,
        (if (tag contains "mode") Mode.fromString(tag.getString("mode")) else None).getOrElse(Mode.default)
      )
    }
  }

  case class Data(storedData: Option[FrameData], mode: Mode.Value) {
    def applySettings[A](frame: FrameAccess, state: BlockState, frameEntity: FrameEntity, player: PlayerEntity, world: World): Boolean = {
      storedData match {
        case Some(storedData) =>
          if (storedData.sections != frameEntity.sections) {
            player.sendMessage(new TranslatableText("gui.framity.framers_hammer.different_format"), true)
            return false
          }

          if (!player.isCreative) {
            val requireAllItems = mode match {
              case Mode.CopyNone => return false
              case Mode.CopyPartial => false
              case Mode.CopyAll => true
            }

            val itemSlotToFrameSlot = storedData.sections.itemIndices
              .map(i => (storedData.items(i), i))
              .flatMap {
                case (Some(stack), slot) => Some(stack.getItem, slot)
                case _ => None
              }
              .toMap

            val playerSlotToFrameSlot = player.inventory
              .zipWithIndex
              .flatMap {
                case (Some(stack), slot) =>
                  if (itemSlotToFrameSlot contains stack.getItem)
                    Some(slot, itemSlotToFrameSlot(stack.getItem))
                  else
                    None
                case _ => None
              }
              .toMap

            if (requireAllItems && playerSlotToFrameSlot.size != storedData.items.count(_.isDefined)) {
              return false
            }

            if (!world.isClient) {
              playerSlotToFrameSlot foreach { case (playerSlot, frameSlot) =>
                if (frameEntity.getStack(frameSlot).getItem != player.inventory.getStack(playerSlot).getItem && frame.absoluteSlotIsValid(frameEntity, state, frameSlot)) {
                  if (!frameEntity.getStack(frameSlot).isEmpty) {
                    player.inventory.offerOrDrop(world, frameEntity.removeStack(frameSlot))
                  }
                  frameEntity.setStack(frameSlot, player.inventory.removeStack(playerSlot, 1))
                }
              }
            }
          } else {
            if (mode == Mode.CopyNone) {
              return false
            }

            if (!world.isClient) {
              storedData.sections.itemIndices
                .flatMap(slot => Some(storedData.items(slot), slot))
                .flatMap {
                  case (Some(stack), slot) => Some((stack, slot))
                  case _ => None
                }
                .foreach { case (stack, slot) =>
                  frameEntity.setStack(slot, stack.copy())
                }
            }
          }

          player.sendMessage(new TranslatableText("gui.framity.framers_hammer.apply_settings"), true)

          true
        case None => false
      }
    }
  }

  object ModelPredicate extends ModelPredicateProvider {
    override def call(stack: ItemStack, world: ClientWorld, entity: LivingEntity): Float =
      Option(stack.getTag)
        .map(_.getString("mode"))
        .flatMap(Mode.fromString)
        .getOrElse(Mode.default)
        .id
  }

  def getTagOrAssignDefault(stack: ItemStack): CompoundTag = {
    if (stack.getTag == null) {
      val tag = new CompoundTag
      tag.putString("mode", Mode.default.toString)
      stack.setTag(tag)
    }
    stack.getTag
  }

  override def useOnBlock(context: ItemUsageContext): ActionResult = {
    val tag = getTagOrAssignDefault(context.getStack)
    val data = Data.fromTag(context.getStack.getTag)
    val state = context.getWorld.getBlockState(context.getBlockPos)
    val frame = state.getBlock match {
      case frame: FrameAccess => frame
      case _ => return super.useOnBlock(context)
    }
    val frameEntity = context.getWorld.getBlockEntity(context.getBlockPos) match {
      case frameEntity: FrameEntity => frameEntity
      case _ => return super.useOnBlock(context)
    }
    val player = context.getPlayer match {
      case player: PlayerEntity => player
      case _ => return super.useOnBlock(context)
    }

    if (player.isSneaking) {
      player.sendMessage(new TranslatableText("gui.framity.framers_hammer.copy_settings"), true)
      tag.put("storedData", frameEntity.data.toTag)

      ActionResult.SUCCESS
    } else {
      if (data.applySettings(frame, state, frameEntity, player, context.getWorld)) {
        ActionResult.SUCCESS
      } else {
        super.useOnBlock(context)
      }
    }
  }

  override def use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult[ItemStack] = {
    if (!user.isSneaking) {
      return super.use(world, user, hand)
    }

    val stack = user.getStackInHand(hand)
    val tag = getTagOrAssignDefault(stack)
    val mode = Mode.fromStringOrDefault(tag.getString("mode"))

    val newMode = mode.next
    tag.putString("mode", newMode.toString)
    user.sendMessage(new TranslatableText(s"gui.framity.framers_hammer.${newMode.translationKey}"), true)

    TypedActionResult.success(stack)
  }
}


























