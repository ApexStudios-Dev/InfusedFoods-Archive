package xyz.apex.forge.potionfoods.container.slot;

import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.items.SlotItemHandler;

import xyz.apex.forge.potionfoods.block.entity.InfusionStationInventory;

import javax.annotation.Nonnull;
import java.util.List;

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

		List<EffectInstance> customEffects = PotionUtils.getCustomEffects(stack);
		return customEffects.isEmpty();
	}
}
