package xyz.apex.forge.infusedfoods.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IIntArray;
import net.minecraftforge.common.util.Constants;

import xyz.apex.forge.infusedfoods.block.entity.InfusionStationBlockEntity;
import xyz.apex.forge.infusedfoods.block.entity.InfusionStationInventory;
import xyz.apex.forge.infusedfoods.container.slot.*;

public final class InfusionStationContainer extends Container
{
	public final PlayerEntity player;
	public final InfusionStationInventory itemHandler;

	private final IIntArray dataAccess;

	public InfusionStationContainer(ContainerType<?> menuType, int windowId, PlayerInventory playerInventory, InfusionStationInventory itemHandler, IIntArray dataAccess)
	{
		super(menuType, windowId);

		this.itemHandler = itemHandler;
		this.dataAccess = dataAccess;
		player = playerInventory.player;

		addDataSlots(dataAccess);

		// inventory slots
		addSlot(new SlotInfusionPotion(itemHandler, 27, 8));
		addSlot(new SlotInfusionBlaze(itemHandler, 46, 8));
		addSlot(new SlotInfusionFood(itemHandler, 90, 22));
		addSlot(new SlotInfusionResult(itemHandler, 134, 22));
		addSlot(new SlotInfusionBottle(itemHandler, 8, 51));

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

	public int getInfuseTime()
	{
		return dataAccess.get(InfusionStationBlockEntity.DATA_SLOT_INFUSE_TIME);
	}

	public int getBlazeFuel()
	{
		return dataAccess.get(InfusionStationBlockEntity.DATA_SLOT_BLAZE_FUEL);
	}

	@Override
	public boolean stillValid(PlayerEntity player)
	{
		return player.isAlive() && player.containerMenu == this;
	}

	@Override
	public ItemStack quickMoveStack(PlayerEntity player, int slotIndex)
	{
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = slots.get(slotIndex);

		if(slot != null && slot.hasItem())
		{
			ItemStack stack1 = slot.getItem();
			stack = stack1.copy();
			int maxIndex = itemHandler.getSlots();

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

	public void updateFromNetwork(CompoundNBT updateTag)
	{
		if(updateTag.contains(InfusionStationBlockEntity.NBT_INVENTORY, Constants.NBT.TAG_COMPOUND))
		{
			CompoundNBT inventoryTag = updateTag.getCompound(InfusionStationBlockEntity.NBT_INVENTORY);
			itemHandler.deserializeNBT(inventoryTag);
		}

		int infuseTime;
		int blazeFuel;

		if(updateTag.contains(InfusionStationBlockEntity.NBT_INFUSION_TIME, Constants.NBT.TAG_ANY_NUMERIC))
			infuseTime = updateTag.getInt(InfusionStationBlockEntity.NBT_INFUSION_TIME);
		else
			infuseTime = 0;

		if(updateTag.contains(InfusionStationBlockEntity.NBT_BLAZE_FUEL, Constants.NBT.TAG_ANY_NUMERIC))
			blazeFuel = updateTag.getInt(InfusionStationBlockEntity.NBT_BLAZE_FUEL);
		else
			blazeFuel = 0;

		setData(InfusionStationBlockEntity.DATA_SLOT_INFUSE_TIME, infuseTime);
		setData(InfusionStationBlockEntity.DATA_SLOT_BLAZE_FUEL, blazeFuel);
	}
}
