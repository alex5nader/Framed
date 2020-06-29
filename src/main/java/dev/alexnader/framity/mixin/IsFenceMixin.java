package dev.alexnader.framity.mixin;

import dev.alexnader.framity.blocks.FenceFrame;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.FenceBlock;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FenceBlock.class)
public class IsFenceMixin extends Block {
    private IsFenceMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("HEAD"), method = "isFence", cancellable = true)
    private void isFence(Block block, CallbackInfoReturnable<Boolean> cir) {
        if (block instanceof FenceFrame) {
            cir.setReturnValue(true);
        }
    }
}
