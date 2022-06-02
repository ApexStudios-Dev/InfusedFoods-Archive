package xyz.apex.forge.infusedfoods.container.slot;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import xyz.apex.forge.infusedfoods.block.entity.InfusionStationInventory;

import javax.annotation.Nonnull;

public final class SlotInfusionResult extends SlotItemHandler
{
	public SlotInfusionResult(InfusionStationInventory itemHandler, int xPosition, int yPosition)
	{
		super(itemHandler, InfusionStationInventory.SLOT_RESULT, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(@Nonnull ItemStack stack)
	{
		return false;
	}
}
