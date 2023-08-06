package xyz.apex.minecraft.infusedfoods.common;

import net.minecraft.nbt.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xyz.apex.minecraft.apexcore.common.lib.helper.TagHelper;

@ApiStatus.NonExtendable
public interface InfusionHelper
{
    String NBT_HIDE_EFFECTS = "ApexHidePotionEffects";
    TagKey<Item> INFUSION_BLACK_LIST = TagHelper.itemTag(InfusedFoods.ID, "infusion_black_list");

    static int getEffectColor(@Nullable MobEffect effect, int amplifier)
    {
        if(effect == null)
            return 0x385DC6;

        var l = amplifier + 1;

        if(l == 0)
            return 0;

        var color = effect.getColor();
        var r = (float) (l * (color >> 16 & 255)) / 255F;
        var g = (float) (l * (color >> 8 & 255)) / 255F;
        var b = (float) (l * (color >> 0 & 255)) / 255F;

        r = r / (float) l * 255F;
        g = g / (float) l * 255F;
        b = b / (float) l * 255F;

        return (int) r << 16 | (int) g << 8 | (int) b << 0;
    }

    static boolean isInfusedFood(ItemStack stack)
    {
        if(!isValidFood(stack))
            return false;
        return !PotionUtils.getCustomEffects(stack).isEmpty();
    }

    static boolean isValidFood(ItemStack stack)
    {
        if(stack.is(INFUSION_BLACK_LIST))
            return false;
        if(stack.isEmpty() || !stack.isEdible())
            return false;

        var food = stack.getItem().getFoodProperties();

        if(food == null)
            return false;

        return food.getEffects().isEmpty();
    }

    static void setPotionEffectsVisible(ItemStack stack, boolean hide)
    {
        if(hide)
        {
            var tag = stack.getOrCreateTag();
            tag.putBoolean(NBT_HIDE_EFFECTS, true);
        }
        else
            stack.removeTagKey(NBT_HIDE_EFFECTS);
    }

    static boolean arePotionEffectsHidden(ItemStack stack)
    {
        var tag = stack.getTag();
        return tag != null && tag.contains(NBT_HIDE_EFFECTS, Tag.TAG_BYTE) && tag.getBoolean(NBT_HIDE_EFFECTS);
    }
}
