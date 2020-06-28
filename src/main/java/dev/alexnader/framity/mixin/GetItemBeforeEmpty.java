package dev.alexnader.framity.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStack.class)
public interface GetItemBeforeEmpty {
    @Accessor("item")
    Item getItemBeforeEmpty();
}
