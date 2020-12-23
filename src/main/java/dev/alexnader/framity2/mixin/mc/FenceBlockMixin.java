package dev.alexnader.framity2.mixin.mc;

import dev.alexnader.framity2.Framity2;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FenceBlock.class)
public class FenceBlockMixin {
    @Inject(method = "canConnect", at = @At("HEAD"), cancellable = true)
    void connectToFenceFrame(final BlockState state, final boolean neighborIsFullSquare, final Direction dir, final CallbackInfoReturnable<Boolean> cir) {
        if (state.isOf(Framity2.BLOCKS.FENCE_FRAME)) {
            cir.setReturnValue(true);
        }

        //noinspection ConstantConditions
        if ((Object) this == Framity2.BLOCKS.FENCE_FRAME && state.getBlock() instanceof FenceBlock) {
            cir.setReturnValue(true);
        }
    }
}
