package xyz.apex.forge.infusedfoods.container;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import xyz.apex.forge.apexcore.lib.net.packet.SyncContainerPacket;
import xyz.apex.forge.infusedfoods.block.entity.InfusionStationBlockEntity;
import xyz.apex.forge.infusedfoods.init.IFElements;

import java.util.Objects;

public final class InfusionStationMenu extends AbstractContainerMenu
{
	public final Player player;
	public final IItemHandler itemHandler;
	private final BlockPos pos;
	public final InfusionStationBlockEntity blockEntity;

	public InfusionStationMenu(MenuType<?> menuType, int windowId, Inventory playerInventory, FriendlyByteBuf buffer)
	{
		super(menuType, windowId);

		pos = buffer.readBlockPos();
		blockEntity = Objects.requireNonNull(IFElements.INFUSION_STATION_BLOCK_ENTITY.getNullable(playerInventory.player.level, pos));

		this.itemHandler = blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseGet(blockEntity::getItemHandler);
		player = playerInventory.player;

		// inventory slots
		addSlot(new PotionSlot());
		addSlot(new BlazeSlot());
		addSlot(new FoodSlot());
		addSlot(new ResultSlot());
		addSlot(new BottleSlot());

		// player inventory slots
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for(int i = 0; i < 9; ++i)
		{
			addSlot(new Slot(playerInventory, i, 8 + i * 18, 84 + 58));
		}
	}

	@Override
	public boolean stillValid(Player player)
	{
		return player.isAlive() && player.containerMenu == this;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex)
	{
		var stack = ItemStack.EMPTY;
		var slot = slots.get(slotIndex);

		if(slot.hasItem())
		{
			var stack1 = slot.getItem();
			stack = stack1.copy();
			var maxIndex = itemHandler.getSlots();

			if(slotIndex < maxIndex)
			{
				if(!moveItemStackTo(stack1, maxIndex, this.slots.size(), true))
					return ItemStack.EMPTY;
			}
			else if(!moveItemStackTo(stack1, 0, maxIndex, false))
				return ItemStack.EMPTY;

			if(stack1.isEmpty())
				slot.set(ItemStack.EMPTY);
			else
				slot.setChanged();
		}

		return stack;
	}

	@Override
	public void broadcastChanges()
	{
		super.broadcastChanges();
		SyncContainerPacket.sendToClient(blockEntity);
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