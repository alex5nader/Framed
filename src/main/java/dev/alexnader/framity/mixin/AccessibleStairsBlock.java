package dev.alexnader.framity.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StairsBlock.class)
public interface AccessibleStairsBlock {
    @Invoker("<init>")
    static StairsBlock create(@SuppressWarnings("unused") BlockState baseBlockState, @SuppressWarnings("unused") AbstractBlock.Settings settings) {
        return null;
    }
}
