package xyz.apex.forge.infusedfoods.container.slot;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import xyz.apex.forge.infusedfoods.block.entity.InfusionStationInventory;

import javax.annotation.Nonnull;

public final class SlotInfusionBottle extends SlotItemHandler
{
	public SlotInfusionBottle(InfusionStationInventory itemHandler, int xPosition, int yPosition)
	{
		super(itemHandler, InfusionStationInventory.SLOT_BOTTLE, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(@Nonnull ItemStack stack)
	{
		return false;
	}
}