package dev.alexnader.framity.mixin;

import dev.alexnader.framity.adapters.IStairs;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StairsBlock.class)
public class StairsBlockMixin {
    @Inject(method = "isStairs", at = @At(value = "HEAD"), cancellable = true)
    private static void isStairsProxy(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof IStairs) {
            cir.setReturnValue(true);
        }
    }
}
