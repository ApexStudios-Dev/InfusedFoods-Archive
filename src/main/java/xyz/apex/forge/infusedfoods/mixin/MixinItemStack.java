package xyz.apex.forge.infusedfoods.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.item.ItemStack;

import xyz.apex.forge.infusedfoods.InfusedFoods;

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
    private boolean InfusedFoods$shouldItalicizeName(ItemStack stack)
    {
        return stack.hasCustomHoverName() || InfusedFoods.arePotionEffectsHidden(stack);
    }
}
