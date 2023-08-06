package xyz.apex.minecraft.infusedfoods.common.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import xyz.apex.minecraft.infusedfoods.common.InfusedFoods;
import xyz.apex.minecraft.infusedfoods.common.InfusionHelper;

public final class InfusionHideRecipe extends CustomRecipe
{
    public InfusionHideRecipe(ResourceLocation resourceLocation, CraftingBookCategory craftingBookCategory)
    {
        super(resourceLocation, craftingBookCategory);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level)
    {
        var hider = ItemStack.EMPTY;
        var food = ItemStack.EMPTY;

        for(var i = 0; i < container.getContainerSize(); i++)
        {
            var stack = container.getItem(i);

            if(stack.isEmpty())
                continue;

            if(isValidHider(stack))
            {
                if(!hider.isEmpty())
                    return false;

                hider = stack;
            }
            else if(isValidFoodInput(stack))
            {
                if(!food.isEmpty())
                    return false;

                food = stack;
            }
        }

        return isValidFoodInput(food) && isValidHider(hider);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess)
    {
        var hider = ItemStack.EMPTY;
        var food = ItemStack.EMPTY;

        for(var i = 0; i < container.getContainerSize(); i++)
        {
            var stack = container.getItem(i);

            if(stack.isEmpty())
                continue;

            if(isValidHider(stack))
            {
                if(!hider.isEmpty())
                    return ItemStack.EMPTY;

                hider = stack;
            }
            else if(isValidFoodInput(stack))
            {
                if(!food.isEmpty())
                    return ItemStack.EMPTY;

                food = stack;
            }
        }

        if(!isValidHider(hider) || !isValidFoodInput(food))
            return ItemStack.EMPTY;

        var result = food.copy();
        result.setCount(1);
        InfusionHelper.setPotionEffectsVisible(result, true);
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
        return InfusedFoods.INFUSION_HIDE_RECIPE.value();
    }

    public static boolean isValidFoodInput(ItemStack stack)
    {
        if(!InfusionHelper.isInfusedFood(stack))
            return false;
        return !InfusionHelper.arePotionEffectsHidden(stack);
    }

    public static boolean isValidHider(ItemStack stack)
    {
        return !stack.isEmpty() && stack.is(InfusedFoods.INFUSION_HIDER);
    }
}
