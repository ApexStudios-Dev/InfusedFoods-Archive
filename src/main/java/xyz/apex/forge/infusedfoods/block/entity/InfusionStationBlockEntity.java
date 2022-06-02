package xyz.apex.forge.infusedfoods.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;

import xyz.apex.forge.apexcore.lib.block.entity.BaseBlockEntity;
import xyz.apex.forge.apexcore.lib.util.NameableMutable;
import xyz.apex.forge.infusedfoods.InfusedFoods;
import xyz.apex.forge.infusedfoods.container.InfusionStationMenu;
import xyz.apex.forge.infusedfoods.init.IFElements;
import xyz.apex.forge.infusedfoods.network.PacketSyncInfusionData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;

// TODO: Make generic InventoryBlockEntity inside of ApexCore
public final class InfusionStationBlockEntity extends BaseBlockEntity implements MenuProvider, NameableMutable, ContainerListener
{
	public static final String NBT_INVENTORY = "Inventory";
	public static final String NBT_CUSTOM_NAME = "CustomName";
	public static final String NBT_INFUSION_TIME = "InfusionTime";
	public static final String NBT_BLAZE_FUEL = "BlazeFuel";

	public static final int DATA_SLOT_INFUSE_TIME = 0;
	public static final int DATA_SLOT_BLAZE_FUEL = 1;
	public static final int DATA_SLOT_COUNT = 2;

	public static final int INFUSION_TIME = 400; // same time as brewing stand

	@Nullable private InfusionStationInventory inventory = null;
	private final LazyOptional<InfusionStationInventory> itemHandlerCapability = LazyOptional.of(this::createItemHandler);
	@Nullable private Component customName = null;
	private final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int index)
		{
			if(index == DATA_SLOT_INFUSE_TIME)
				return infuseTime;
			if(index == DATA_SLOT_BLAZE_FUEL)
				return blazeFuel;
			return 0;
		}

		@Override
		public void set(int index, int value)
		{
			if(index == DATA_SLOT_BLAZE_FUEL)
				blazeFuel = value;
			else if(index == DATA_SLOT_INFUSE_TIME)
				infuseTime = value;
		}

		@Override
		public int getCount()
		{
			return DATA_SLOT_COUNT;
		}
	};

	private int infuseTime = 0;
	private int blazeFuel = 0;

	public InfusionStationBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState)
	{
		super(blockEntityType, pos, blockState);

		itemHandlerCapability.addListener(opt -> inventory = null);
	}

	private InfusionStationInventory createItemHandler()
	{
		if(inventory == null)
			inventory = new InfusionStationInventory(this::setChanged);
		return inventory;
	}

	public InfusionStationInventory getItemHandler()
	{
		return itemHandlerCapability.orElseGet(this::createItemHandler);
	}

	public void loadFromItemStack(CompoundTag tagCompound)
	{
		if(tagCompound.contains(NBT_BLAZE_FUEL, Tag.TAG_ANY_NUMERIC))
		{
			blazeFuel = tagCompound.getInt(NBT_BLAZE_FUEL);
			setChanged();
		}

		if(tagCompound.contains(NBT_CUSTOM_NAME, Tag.TAG_STRING))
		{
			var customNameJson = tagCompound.getString(NBT_CUSTOM_NAME);
			customName = TextComponent.Serializer.fromJson(customNameJson);
			setChanged();
		}

		if(tagCompound.contains(NBT_INVENTORY, Tag.TAG_COMPOUND))
		{
			inventory = getItemHandler();
			var inventoryTag = tagCompound.getCompound(NBT_INVENTORY);

			if(inventoryTag.contains(InfusionStationInventory.NBT_INFUSION_FLUID, Tag.TAG_COMPOUND))
			{
				var fluidTag = inventoryTag.getCompound(InfusionStationInventory.NBT_INFUSION_FLUID);
				inventory.infusionFluid = new InfusionStationInventory.InfusionFluid(fluidTag);
				inventory.onFluidChanged.run();
			}
		}
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player)
	{
		InfusionStationMenu drawerContainer = new InfusionStationMenu(IFElements.INFUSION_STATION_CONTAINER.asMenuType(), windowId, playerInventory, getItemHandler(), dataAccess);
		drawerContainer.addSlotListener(this);
		return drawerContainer;
	}

	@Override
	public void setCustomName(@Nullable Component customName)
	{
		// TODO: Change base to be component
		this.customName = customName;
	}

	@Override
	public void load(CompoundTag tagCompound)
	{
		inventory = null;
		customName = null;
		infuseTime = 0;
		blazeFuel = 0;

		if(tagCompound.contains(NBT_INVENTORY, Tag.TAG_COMPOUND))
		{
			itemHandlerCapability.invalidate();
			inventory = new InfusionStationInventory(this::setChanged);
			var inventoryTag = tagCompound.getCompound(NBT_INVENTORY);
			inventory.deserializeNBT(inventoryTag);
		}

		if(tagCompound.contains(NBT_CUSTOM_NAME, Tag.TAG_STRING))
		{
			var customNameJson = tagCompound.getString(NBT_CUSTOM_NAME);
			customName = TextComponent.Serializer.fromJson(customNameJson);
		}

		if(tagCompound.contains(NBT_INFUSION_TIME, Tag.TAG_ANY_NUMERIC))
			infuseTime = tagCompound.getInt(NBT_INFUSION_TIME);
		if(tagCompound.contains(NBT_BLAZE_FUEL, Tag.TAG_ANY_NUMERIC))
			blazeFuel = tagCompound.getInt(NBT_BLAZE_FUEL);

		super.load(tagCompound);
	}

	@Override
	public CompoundTag save(CompoundTag tagCompound)
	{
		if(inventory != null)
		{
			var inventoryTag = inventory.serializeNBT();
			tagCompound.put(NBT_INVENTORY, inventoryTag);
		}

		if(customName != null)
		{
			var customNameJson = TextComponent.Serializer.toJson(customName);
			tagCompound.putString(NBT_CUSTOM_NAME, customNameJson);
		}

		if(infuseTime > 0)
			tagCompound.putInt(NBT_INFUSION_TIME, infuseTime);
		if(blazeFuel > 0)
			tagCompound.putInt(NBT_BLAZE_FUEL, blazeFuel);

		return super.save(tagCompound);
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side)
	{
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return itemHandlerCapability.cast();

		return super.getCapability(capability, side);
	}

	@Nullable
	@Override
	public Component getCustomName()
	{
		return customName;
	}

	@Override
	public Component getDisplayName()
	{
		return customName == null ? getName() : customName;
	}

	@Override
	public Component getName()
	{
		return new TranslatableComponent(getBlockState().getBlock().getDescriptionId());
	}

	@Override
	public void slotChanged(AbstractContainerMenu container, int slotIndex, ItemStack stack)
	{
		setChanged();
	}

	@Override
	public void dataChanged(AbstractContainerMenu container, int varToUpdate, int newValue)
	{
	}

	@Override
	protected CompoundTag writeUpdateTag(CompoundTag tagCompound)
	{
		if(inventory != null)
		{
			var inventoryTag = inventory.serializeNBT();
			tagCompound.put(NBT_INVENTORY, inventoryTag);
		}

		if(customName != null)
		{
			var customNameJson = TextComponent.Serializer.toJson(customName);
			tagCompound.putString(NBT_CUSTOM_NAME, customNameJson);
		}

		if(infuseTime > 0)
			tagCompound.putInt(NBT_INFUSION_TIME, infuseTime);
		if(blazeFuel > 0)
			tagCompound.putInt(NBT_BLAZE_FUEL, blazeFuel);

		return super.writeUpdateTag(tagCompound);
	}

	@Override
	protected void readeUpdateTag(CompoundTag tagCompound)
	{
		inventory = null;
		customName = null;
		infuseTime = 0;
		blazeFuel = 0;

		if(tagCompound.contains(NBT_INVENTORY, Tag.TAG_COMPOUND))
		{
			itemHandlerCapability.invalidate();
			inventory = new InfusionStationInventory(this::setChanged);
			var inventoryTag = tagCompound.getCompound(NBT_INVENTORY);
			inventory.deserializeNBT(inventoryTag);
		}

		if(tagCompound.contains(NBT_CUSTOM_NAME, Tag.TAG_STRING))
		{
			var customNameJson = tagCompound.getString(NBT_CUSTOM_NAME);
			customName = TextComponent.Serializer.fromJson(customNameJson);
		}

		if(tagCompound.contains(NBT_INFUSION_TIME, Tag.TAG_ANY_NUMERIC))
			infuseTime = tagCompound.getInt(NBT_INFUSION_TIME);
		if(tagCompound.contains(NBT_BLAZE_FUEL, Tag.TAG_ANY_NUMERIC))
			blazeFuel = tagCompound.getInt(NBT_BLAZE_FUEL);

		super.readeUpdateTag(tagCompound);
	}

	@Override
	public void setChanged()
	{
		super.setChanged();
		markBlockUpdate();
	}

	private void markBlockUpdate()
	{
		if(level != null && !level.isClientSide())
		{
			var blockState = level.getBlockState(worldPosition);
			level.sendBlockUpdated(worldPosition, blockState, blockState, 3);

			var updateTag = getUpdateTag();
			InfusedFoods.NETWORK.sendTo(new PacketSyncInfusionData(worldPosition, updateTag), PacketDistributor.ALL.noArg());
		}
	}

	private boolean canInfuse()
	{
		var inventory = getItemHandler();

		if(!inventory.hasInfusionFluid())
			return false;

		var food = inventory.getFood();

		if(food.isEmpty())
			return false;
		if(!PotionUtils.getCustomEffects(food).isEmpty())
			return false;

		var result = inventory.getResult();

		if(result.isEmpty())
			return true;
		if(!ItemStack.isSame(result, food))
			return false;

		return result.getCount() + 1 < food.getMaxStackSize();
	}

	public static void serverTick(Level level, BlockPos pos, BlockState blockState, InfusionStationBlockEntity blockEntity)
	{
		var inventory = blockEntity.getItemHandler();

		var potionStack = inventory.getPotion();
		var foodStack = inventory.getFood();
		var blazeStack = inventory.getBlaze();
		var resultStack = inventory.getResult();
		var bottleStack = inventory.getBottle();

		var changed = false;

		if(blockEntity.blazeFuel <= 0 && !blazeStack.isEmpty())
		{
			blockEntity.blazeFuel = 20;
			blazeStack.shrink(1);
			changed = true;
		}

		if(blockEntity.infuseTime > 0 && !blockEntity.canInfuse())
		{
			blockEntity.infuseTime = 0;
			changed = true;
		}

		if(inventory.hasInfusionFluid())
		{
			if(blockEntity.canInfuse())
			{
				if(blockEntity.blazeFuel > 0)
				{
					if(blockEntity.infuseTime == 0)
					{
						blockEntity.blazeFuel--;
						blockEntity.infuseTime = INFUSION_TIME;
						changed = true;
					}
					else
					{
						blockEntity.infuseTime--;

						if(blockEntity.infuseTime == 0)
						{
							var effectInstance = inventory.getInfusionFluid().toEffectInstance();

							if(effectInstance != null)
							{
								if(resultStack.isEmpty())
								{
									var foodToInfuse = foodStack.split(1).copy();
									foodToInfuse.setCount(1);
									PotionUtils.setCustomEffects(foodToInfuse, Collections.singletonList(effectInstance));

									inventory.setResult(foodToInfuse);
								}
								else
								{
									foodStack.shrink(1);
									resultStack.grow(1);
								}

								inventory.decrementInfusionFluid(1);
							}
						}

						changed = true;
					}
				}
			}
		}
		else
		{
			if(!potionStack.isEmpty())
			{
				var potion = PotionUtils.getPotion(potionStack);
				var effects = potion.getEffects();

				if(effects.size() == 1)
				{
					var effectInstance = effects.get(0);
					inventory.incrementInfusionFluid(effectInstance);

					if(inventory.hasInfusionFluid())
					{
						inventory.setPotion(ItemStack.EMPTY);

						if(bottleStack.isEmpty())
							inventory.setBottle(Items.GLASS_BOTTLE.getDefaultInstance());
						else
							bottleStack.grow(1);

						blockEntity.infuseTime = 0;
						changed = true;
					}
				}
			}
		}

		if(changed)
			blockEntity.setChanged();
	}
}
