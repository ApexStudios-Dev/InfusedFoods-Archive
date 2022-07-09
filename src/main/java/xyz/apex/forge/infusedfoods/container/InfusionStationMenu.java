package xyz.apex.forge.infusedfoods.container;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import xyz.apex.forge.apexcore.revamp.container.BaseMenu;
import xyz.apex.forge.infusedfoods.block.entity.InfusionStationBlockEntity;

public final class InfusionStationMenu extends BaseMenu
{
	public final Player player;
	public final IItemHandler itemHandler;

	public InfusionStationMenu(MenuType<? extends InfusionStationMenu> menuType, int windowId, Inventory playerInventory, FriendlyByteBuf buffer)
	{
		super(menuType, windowId, playerInventory, buffer);

		Validate.notNull(blockEntity);
		this.itemHandler = blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).resolve().orElseThrow();
		player = playerInventory.player;

		// inventory slots
		addSlot(new PotionSlot());
		addSlot(new BlazeSlot());
		addSlot(new FoodSlot());
		addSlot(new ResultSlot());
		addSlot(new BottleSlot());

		bindPlayerInventory(this, 8, 84);
	}

	private final class PotionSlot extends SlotItemHandler
	{
		private PotionSlot()
		{
			super(itemHandler, InfusionStationBlockEntity.SLOT_POTION, 27, 8);
		}

		@Override
		public boolean mayPlace(@NotNull ItemStack stack)
		{
			if(stack.getItem() != Items.POTION)
				return false;

			var potion = PotionUtils.getPotion(stack);
			return potion.getEffects().size() == 1;
		}
	}

	private final class BlazeSlot extends SlotItemHandler
	{
		private BlazeSlot()
		{
			super(itemHandler, InfusionStationBlockEntity.SLOT_BLAZE, 46, 8);
		}

		@Override
		public boolean mayPlace(@NotNull ItemStack stack)
		{
			return stack.getItem() == Items.BLAZE_POWDER;
		}
	}

	private final class FoodSlot extends SlotItemHandler
	{
		private FoodSlot()
		{
			super(itemHandler, InfusionStationBlockEntity.SLOT_FOOD, 90, 22);
		}

		@Override
		public boolean mayPlace(@NotNull ItemStack stack)
		{
			if(!stack.isEdible())
				return false;

			var customEffects = PotionUtils.getCustomEffects(stack);
			return customEffects.isEmpty();
		}
	}

	private final class ResultSlot extends SlotItemHandler
	{
		private ResultSlot()
		{
			super(itemHandler, InfusionStationBlockEntity.SLOT_RESULT, 134, 22);
		}

		@Override
		public boolean mayPlace(@NotNull ItemStack stack)
		{
			return false;
		}
	}

	private final class BottleSlot extends SlotItemHandler
	{
		private BottleSlot()
		{
			super(itemHandler, InfusionStationBlockEntity.SLOT_BOTTLE, 8, 51);
		}

		@Override
		public boolean mayPlace(@NotNull ItemStack stack)
		{
			return false;
		}
	}
}