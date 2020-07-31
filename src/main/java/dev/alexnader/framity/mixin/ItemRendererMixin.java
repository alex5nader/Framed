package dev.alexnader.framity.mixin;

import dev.alexnader.framity.Framity$;
import dev.alexnader.framity.item.FramersHammer$;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Final
    @Shadow
    private ItemModels models;

    @Shadow public abstract BakedModel getHeldItemModel(ItemStack stack, World world, LivingEntity entity);

    @Redirect(method = "getHeldItemModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemModels;getModel(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;"))
    BakedModel getModelProxy(ItemModels itemModels, ItemStack stack) {
        if (stack.getItem() == FramersHammer$.MODULE$) {
            return itemModels.getModelManager().getModel(new ModelIdentifier(Framity$.MODULE$.Mod().id("framers_hammer_none"), "inventory"));
        }
        return itemModels.getModel(stack);
    }

    @Redirect(method = "innerRenderInGui", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;getHeldItemModel(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/client/render/model/BakedModel;"))
    BakedModel getHeldItemModelProxy(ItemRenderer itemRenderer, ItemStack stack, World world, LivingEntity entity) {
        if (stack.getItem() == FramersHammer$.MODULE$) {
            ClientWorld clientWorld = world instanceof ClientWorld ? (ClientWorld) world : null;
            BakedModel model = models.getModelManager().getModel(new ModelIdentifier(Framity$.MODULE$.Mod().id("framers_hammer"), "inventory"));
            BakedModel model2 = model.getOverrides().apply(model, stack, clientWorld, entity);
            return model2 == null ? models.getModelManager().getMissingModel() : model2;
        }
        return this.getHeldItemModel(stack, world, entity);
    }
}
