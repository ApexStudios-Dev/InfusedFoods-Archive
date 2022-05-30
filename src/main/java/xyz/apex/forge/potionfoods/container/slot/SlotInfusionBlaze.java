package xyz.apex.forge.potionfoods.container.slot;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.items.SlotItemHandler;

import xyz.apex.forge.potionfoods.block.entity.InfusionStationInventory;

import javax.annotation.Nonnull;

public final class SlotInfusionBlaze extends SlotItemHandler
{
	public SlotInfusionBlaze(InfusionStationInventory itemHandler, int xPosition, int yPosition)
	{
		super(itemHandler, InfusionStationInventory.SLOT_BLAZE, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(@Nonnull ItemStack stack)
	{
		return stack.getItem() == Items.BLAZE_POWDER;
	}
}
