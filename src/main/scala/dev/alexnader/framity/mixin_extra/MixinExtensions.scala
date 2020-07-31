package dev.alexnader.framity.mixin_extra

import dev.alexnader.framity.mixin
import net.minecraft.item.{Item, ItemStack}

object MixinExtensions {
  implicit class GetItemBeforeEmpty(stack: ItemStack) {
    def itemBeforeEmpty: Item = stack.asInstanceOf[mixin.GetItemBeforeEmpty].getItemBeforeEmpty
  }
}
