package xyz.apex.forge.infusedfoods.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import xyz.apex.forge.commonality.tags.FluidTags;
import xyz.apex.forge.infusedfoods.InfusedFoods;
import xyz.apex.forge.infusedfoods.init.IFElements;

public final class InfusionCleanseRecipe extends CustomRecipe
{
    public InfusionCleanseRecipe(ResourceLocation recipeId, CraftingBookCategory bookCategory)
    {
        super(recipeId, bookCategory);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level)
    {
        var milk = ItemStack.EMPTY;
        var food = ItemStack.EMPTY;

        for(var i = 0; i < container.getContainerSize(); i++)
        {
            var stack = container.getItem(i);
            if(stack.isEmpty()) continue;

            if(isMilkBucket(stack))
            {
                if(!milk.isEmpty()) return false;
                milk = stack;
            }
            else if(isValidFoodInput(stack))
            {
                if(!food.isEmpty()) return false;
                food = stack;
            }
        }

        return !milk.isEmpty() && !food.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer container)
    {
        var milk = ItemStack.EMPTY;
        var food = ItemStack.EMPTY;

        for(var i = 0; i < container.getContainerSize(); i++)
        {
            var stack = container.getItem(i);
            if(stack.isEmpty()) continue;

            if(isMilkBucket(stack))
            {
                if(!milk.isEmpty()) return ItemStack.EMPTY;
                milk = stack;
            }
            else if(isValidFoodInput(stack))
            {
                if(!food.isEmpty()) return ItemStack.EMPTY;
                food = stack;
            }
        }

        if(milk.isEmpty() || food.isEmpty()) return ItemStack.EMPTY;

        var result = food.copy();
        result.setCount(1);

        // does not modify stack if passed collection is empty
        // which means we can not use this to remove the effects / replace with empty list
        // PotionUtils.setCustomEffects(food, Collections.emptyList());
        // so we manually remove the tag below, if it exists

        var tag = result.getOrCreateTag();
        tag.remove("CustomPotionEffects");
        result.setTag(tag);

        // if for what ever reason, no nbt data changes, fail out the recipe
        if(ItemStack.tagMatches(food, result)) return ItemStack.EMPTY;
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return IFElements.INFUSION_CLEANSE_RECIPE.get();
    }

    public static boolean isValidFoodInput(ItemStack stack)
    {
        if(!InfusedFoods.isValidFood(stack)) return false;
        return !PotionUtils.getCustomEffects(stack).isEmpty();
    }

    public static boolean isMilkBucket(ItemStack stack)
    {
        if(stack.isEmpty()) return false;
        if(stack.is(InfusedFoods.BUCKETS_MILK) || stack.is(InfusedFoods.BOTTLES_MILK) || stack.is(Items.MILK_BUCKET)) return true;
        return FluidUtil.getFluidContained(stack).map(InfusionCleanseRecipe::isMilk).orElse(false);
    }

    public static boolean isMilk(FluidStack stack)
    {
        if(stack.isEmpty()) return false;

        var fluid = stack.getFluid();

        if(fluid.is(FluidTags.Forge.MILK)) return true;
        if(ForgeMod.MILK.isPresent() && fluid.isSame(ForgeMod.MILK.get())) return true;
        if(ForgeMod.FLOWING_MILK.isPresent() && fluid.isSame(ForgeMod.FLOWING_MILK.get())) return true;
        if(ForgeMod.MILK_TYPE.isPresent() && fluid.getFluidType() == ForgeMod.MILK_TYPE.get()) return true;
        return false;
    }
}
