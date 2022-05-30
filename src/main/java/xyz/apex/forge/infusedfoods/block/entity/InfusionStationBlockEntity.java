package xyz.apex.forge.infusedfoods.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;

import xyz.apex.forge.apexcore.lib.block.entity.BaseBlockEntity;
import xyz.apex.forge.apexcore.lib.util.INameableMutable;
import xyz.apex.forge.infusedfoods.InfusedFoods;
import xyz.apex.forge.infusedfoods.container.InfusionStationContainer;
import xyz.apex.forge.infusedfoods.init.IFElements;
import xyz.apex.forge.infusedfoods.network.PacketSyncInfusionData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

// TODO: Make generic InventoryBlockEntity inside of ApexCore
public final class InfusionStationBlockEntity extends BaseBlockEntity implements INamedContainerProvider, INameableMutable, IContainerListener, ITickableTileEntity
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
	@Nullable private ITextComponent customName = null;
	private final IIntArray dataAccess = new IIntArray() {
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

	public InfusionStationBlockEntity(TileEntityType<?> blockEntityType)
	{
		super(blockEntityType);

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

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player)
	{
		InfusionStationContainer drawerContainer = new InfusionStationContainer(IFElements.INFUSION_STATION_CONTAINER.asContainerType(), windowId, playerInventory, getItemHandler(), dataAccess);
		drawerContainer.addSlotListener(this);
		return drawerContainer;
	}

	@Override
	public void setCustomName(@Nullable ITextComponent customName)
	{
		this.customName = customName;
	}

	@Override
	public void load(BlockState blockState, CompoundNBT tagCompound)
	{
		inventory = null;
		customName = null;
		infuseTime = 0;
		blazeFuel = 0;

		if(tagCompound.contains(NBT_INVENTORY, Constants.NBT.TAG_COMPOUND))
		{
			itemHandlerCapability.invalidate();
			inventory = new InfusionStationInventory(this::setChanged);
			CompoundNBT inventoryTag = tagCompound.getCompound(NBT_INVENTORY);
			inventory.deserializeNBT(inventoryTag);
		}

		if(tagCompound.contains(NBT_CUSTOM_NAME, Constants.NBT.TAG_STRING))
		{
			String customNameJson = tagCompound.getString(NBT_CUSTOM_NAME);
			customName = ITextComponent.Serializer.fromJson(customNameJson);
		}

		if(tagCompound.contains(NBT_INFUSION_TIME, Constants.NBT.TAG_ANY_NUMERIC))
			infuseTime = tagCompound.getInt(NBT_INFUSION_TIME);
		if(tagCompound.contains(NBT_BLAZE_FUEL, Constants.NBT.TAG_ANY_NUMERIC))
			blazeFuel = tagCompound.getInt(NBT_BLAZE_FUEL);

		super.load(blockState, tagCompound);
	}

	@Override
	public CompoundNBT save(CompoundNBT tagCompound)
	{
		if(inventory != null)
		{
			CompoundNBT inventoryTag = inventory.serializeNBT();
			tagCompound.put(NBT_INVENTORY, inventoryTag);
		}

		if(customName != null)
		{
			String customNameJson = ITextComponent.Serializer.toJson(customName);
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
	public ITextComponent getCustomName()
	{
		return customName;
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return customName == null ? getName() : customName;
	}

	@Override
	public ITextComponent getName()
	{
		return new TranslationTextComponent(getBlockState().getBlock().getDescriptionId());
	}

	@Override
	public void refreshContainer(Container container, NonNullList<ItemStack> stacks)
	{
	}

	@Override
	public void slotChanged(Container container, int slotIndex, ItemStack stack)
	{
		setChanged();
	}

	@Override
	public void setContainerData(Container container, int varToUpdate, int newValue)
	{
	}

	@Override
	protected CompoundNBT writeUpdateTag(CompoundNBT tagCompound)
	{
		if(inventory != null)
		{
			CompoundNBT inventoryTag = inventory.serializeNBT();
			tagCompound.put(NBT_INVENTORY, inventoryTag);
		}

		if(customName != null)
		{
			String customNameJson = ITextComponent.Serializer.toJson(customName);
			tagCompound.putString(NBT_CUSTOM_NAME, customNameJson);
		}

		if(infuseTime > 0)
			tagCompound.putInt(NBT_INFUSION_TIME, infuseTime);
		if(blazeFuel > 0)
			tagCompound.putInt(NBT_BLAZE_FUEL, blazeFuel);

		return super.writeUpdateTag(tagCompound);
	}

	@Override
	protected void readeUpdateTag(CompoundNBT tagCompound)
	{
		inventory = null;
		customName = null;
		infuseTime = 0;
		blazeFuel = 0;

		if(tagCompound.contains(NBT_INVENTORY, Constants.NBT.TAG_COMPOUND))
		{
			itemHandlerCapability.invalidate();
			inventory = new InfusionStationInventory(this::setChanged);
			CompoundNBT inventoryTag = tagCompound.getCompound(NBT_INVENTORY);
			inventory.deserializeNBT(inventoryTag);
		}

		if(tagCompound.contains(NBT_CUSTOM_NAME, Constants.NBT.TAG_STRING))
		{
			String customNameJson = tagCompound.getString(NBT_CUSTOM_NAME);
			customName = ITextComponent.Serializer.fromJson(customNameJson);
		}

		if(tagCompound.contains(NBT_INFUSION_TIME, Constants.NBT.TAG_ANY_NUMERIC))
			infuseTime = tagCompound.getInt(NBT_INFUSION_TIME);
		if(tagCompound.contains(NBT_BLAZE_FUEL, Constants.NBT.TAG_ANY_NUMERIC))
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
			BlockState blockState = level.getBlockState(worldPosition);
			level.sendBlockUpdated(worldPosition, blockState, blockState, 3);

			CompoundNBT updateTag = getUpdateTag();
			InfusedFoods.NETWORK.sendTo(new PacketSyncInfusionData(worldPosition, updateTag), PacketDistributor.ALL.noArg());
		}
	}

	private boolean canInfuse()
	{
		InfusionStationInventory inventory = getItemHandler();

		if(!inventory.hasInfusionFluid())
			return false;

		ItemStack food = inventory.getFood();

		if(food.isEmpty())
			return false;
		if(!PotionUtils.getCustomEffects(food).isEmpty())
			return false;

		ItemStack result = inventory.getResult();

		if(result.isEmpty())
			return true;
		if(!ItemStack.isSame(result, food))
			return false;

		return result.getCount() + 1 < food.getMaxStackSize();
	}

	@Override
	public void tick()
	{
		if(level == null)
			return;

		InfusionStationInventory inventory = getItemHandler();

		ItemStack potionStack = inventory.getPotion();
		ItemStack foodStack = inventory.getFood();
		ItemStack blazeStack = inventory.getBlaze();
		ItemStack resultStack = inventory.getResult();
		ItemStack bottleStack = inventory.getBottle();

		boolean changed = false;

		if(blazeFuel <= 0 && !blazeStack.isEmpty())
		{
			blazeFuel = 20;
			blazeStack.shrink(1);
			changed = true;
		}

		if(infuseTime > 0 && !canInfuse())
		{
			infuseTime = 0;
			changed = true;
		}

		if(inventory.hasInfusionFluid())
		{
			if(canInfuse())
			{
				if(blazeFuel > 0)
				{
					if(infuseTime == 0)
					{
						blazeFuel--;
						infuseTime = INFUSION_TIME;
						changed = true;
					}
					else
					{
						infuseTime--;

						if(infuseTime == 0)
						{
							EffectInstance effectInstance = inventory.getInfusionFluid().toEffectInstance();

							if(effectInstance != null)
							{
								if(resultStack.isEmpty())
								{
									ItemStack foodToInfuse = foodStack.split(1).copy();
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
				Potion potion = PotionUtils.getPotion(potionStack);
				List<EffectInstance> effects = potion.getEffects();

				if(!effects.isEmpty())
				{
					EffectInstance effectInstance = effects.get(0);
					inventory.incrementInfusionFluid(effectInstance);

					if(inventory.hasInfusionFluid())
					{
						if(effects.size() - 1 <= 0)
						{
							inventory.setPotion(ItemStack.EMPTY);

							if(bottleStack.isEmpty())
								inventory.setBottle(Items.GLASS_BOTTLE.getDefaultInstance());
							else
								bottleStack.grow(1);
						}
						else
						{
							effects = effects.subList(1, effects.size());
							potion = new Potion(effects.toArray(new EffectInstance[0]));
							potionStack = PotionUtils.setPotion(potionStack, potion);
							inventory.setPotion(potionStack);
						}

						infuseTime = 0;
						changed = true;
					}
				}
			}
		}

		if(changed)
			setChanged();
	}
}
