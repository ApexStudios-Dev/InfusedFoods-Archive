package xyz.apex.forge.infusedfoods.container.slot;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.items.SlotItemHandler;

import xyz.apex.forge.infusedfoods.block.entity.InfusionStationInventory;

import javax.annotation.Nonnull;

public final class SlotInfusionPotion extends SlotItemHandler
{
	public SlotInfusionPotion(InfusionStationInventory itemHandler, int xPosition, int yPosition)
	{
		super(itemHandler, InfusionStationInventory.SLOT_POTION, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(@Nonnull ItemStack stack)
	{
		if(stack.getItem() != Items.POTION)
			return false;

		Potion potion = PotionUtils.getPotion(stack);
		return !potion.getEffects().isEmpty();
	}
}
