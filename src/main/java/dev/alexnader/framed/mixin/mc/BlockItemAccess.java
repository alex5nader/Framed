package dev.alexnader.framed.mixin.mc;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;

@Mixin(BlockItem.class)
public interface BlockItemAccess {
    @Invoker("getPlacementState")
    @Nullable BlockState getPlacementStateProxy(ItemPlacementContext context);
}
