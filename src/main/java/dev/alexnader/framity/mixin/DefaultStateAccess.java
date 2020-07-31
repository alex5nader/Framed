package dev.alexnader.framity.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Block.class)
public interface DefaultStateAccess {
    @Invoker("setDefaultState")
    void setDefaultStateWorkaround(BlockState state);
}
