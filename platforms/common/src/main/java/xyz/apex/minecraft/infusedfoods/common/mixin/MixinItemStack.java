package xyz.apex.minecraft.infusedfoods.common.mixin;

import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.apex.minecraft.infusedfoods.common.InfusionHelper;

@Mixin(ItemStack.class)
public abstract class MixinItemStack
{
    @Redirect(
            method = "getTooltipLines",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;hasCustomHoverName()Z",
                    opcode = Opcodes.GETFIELD,
                    ordinal = 0
            )
    )
    private boolean InfusedFoods$getTooltipLines$hasCustomHoverName(ItemStack stack)
    {
        if(stack.hasCustomHoverName())
            return true;

        return InfusionHelper.isInfusedFood(stack) && InfusionHelper.arePotionEffectsHidden(stack);
    }
}
