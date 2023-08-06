package xyz.apex.minecraft.infusedfoods.common.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import xyz.apex.minecraft.infusedfoods.common.InfusedFoods;
import xyz.apex.minecraft.infusedfoods.common.InfusionHelper;

public final class InfusionCleansingRecipe extends CustomRecipe
{
    public InfusionCleansingRecipe(ResourceLocation resourceLocation, CraftingBookCategory craftingBookCategory)
    {
        super(resourceLocation, craftingBookCategory);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level)
    {
        var cleansing = ItemStack.EMPTY;
        var food = ItemStack.EMPTY;

        for(var i = 0; i < container.getContainerSize(); i++)
        {
            var stack = container.getItem(i);

            if(stack.isEmpty())
                continue;

            if(isValidCleansing(stack))
            {
                if(!cleansing.isEmpty())
                    return false;

                cleansing = stack;
            }
            else if(isValidFoodInput(stack))
            {
                if(!food.isEmpty())
                    return false;

                food = stack;
            }
        }

        return isValidFoodInput(food) && isValidCleansing(cleansing);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess)
    {
        var cleansing = ItemStack.EMPTY;
        var food = ItemStack.EMPTY;

        for(var i = 0; i < container.getContainerSize(); i++)
        {
            var stack = container.getItem(i);

            if(stack.isEmpty())
                continue;

            if(isValidCleansing(stack))
            {
                if(!cleansing.isEmpty())
                    return ItemStack.EMPTY;

                cleansing = stack;
            }
            else if(isValidFoodInput(stack))
            {
                if(!food.isEmpty())
                    return ItemStack.EMPTY;

                food = stack;
            }
        }

        if(!isValidCleansing(cleansing) || !isValidFoodInput(food))
            return ItemStack.EMPTY;

        var result = food.copy();
        result.setCount(1);
        InfusionHelper.setPotionEffectsVisible(result, false);
        result.removeTagKey(PotionUtils.TAG_CUSTOM_POTION_EFFECTS);
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
        return InfusedFoods.INFUSION_CLEANSE_RECIPE.value();
    }

    public static boolean isValidFoodInput(ItemStack stack)
    {
        return InfusionHelper.isInfusedFood(stack);
    }

    public static boolean isValidCleansing(ItemStack stack)
    {
        return !stack.isEmpty() && stack.is(InfusedFoods.INFUSION_CLEANSING);
    }
}
