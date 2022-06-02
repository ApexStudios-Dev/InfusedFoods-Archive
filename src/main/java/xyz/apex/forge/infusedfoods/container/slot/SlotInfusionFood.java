package xyz.apex.forge.infusedfoods.container.slot;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.items.SlotItemHandler;

import xyz.apex.forge.infusedfoods.block.entity.InfusionStationInventory;

import javax.annotation.Nonnull;

public final class SlotInfusionFood extends SlotItemHandler
{
	public SlotInfusionFood(InfusionStationInventory itemHandler, int xPosition, int yPosition)
	{
		super(itemHandler, InfusionStationInventory.SLOT_FOOD, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(@Nonnull ItemStack stack)
	{
		if(!stack.isEdible())
			return false;

		var customEffects = PotionUtils.getCustomEffects(stack);
		return customEffects.isEmpty();
	}
}
