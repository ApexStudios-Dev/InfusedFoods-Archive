package xyz.apex.forge.infusedfoods.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import xyz.apex.forge.infusedfoods.InfusedFoods;
import xyz.apex.forge.infusedfoods.init.IFElements;

public final class InfusionHideRecipe extends CustomRecipe
{
    public InfusionHideRecipe(ResourceLocation recipeId, CraftingBookCategory bookCategory)
    {
        super(recipeId, bookCategory);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level)
    {
        var hider = ItemStack.EMPTY;
        var food = ItemStack.EMPTY;

        for(var i = 0; i < container.getContainerSize(); i++)
        {
            var stack = container.getItem(i);
            if(stack.isEmpty()) continue;

            if(isValidHideInput(stack))
            {
                if(!hider.isEmpty()) return false;
                hider = stack;
            }
            else if(isValidFoodInput(stack))
            {
                if(!food.isEmpty()) return false;
                food = stack;
            }
        }

        return !hider.isEmpty() && !food.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer container)
    {
        var hider = ItemStack.EMPTY;
        var food = ItemStack.EMPTY;

        for(var i = 0; i < container.getContainerSize(); i++)
        {
            var stack = container.getItem(i);
            if(stack.isEmpty()) continue;

            if(isValidHideInput(stack))
            {
                if(!hider.isEmpty()) return ItemStack.EMPTY;
                hider = stack;
            }
            else if(isValidFoodInput(stack))
            {
                if(!food.isEmpty()) return ItemStack.EMPTY;
                food = stack;
            }
        }

        if(hider.isEmpty() || food.isEmpty()) return ItemStack.EMPTY;

        var result = food.copy();
        result.setCount(1);
        InfusedFoods.setPotionEffectsVisible(result, true);

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
        return IFElements.INFUSION_HIDE_RECIPE.get();
    }

    public static boolean isValidFoodInput(ItemStack stack)
    {
        return InfusionCleanseRecipe.isValidFoodInput(stack) && !InfusedFoods.arePotionEffectsHidden(stack);
    }

    public static boolean isValidHideInput(ItemStack stack)
    {
        return !stack.isEmpty() && stack.is(InfusedFoods.INFUSION_HIDER);
    }
}
