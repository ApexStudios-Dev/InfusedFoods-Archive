package xyz.apex.minecraft.infusedfoods.fabric.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.apex.minecraft.infusedfoods.common.InfusedFoods;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity
{
    @Inject(
            method = "completeUsingItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;stopUsingItem()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void InfusedFoods$completeUsingItem(CallbackInfo ci)
    {
        var entity = (LivingEntity) (Object) this;
        var hand = entity.getUsedItemHand();
        var stack = entity.getItemInHand(hand);

        InfusedFoods.onFinishItemUse(entity, stack);
    }
}
