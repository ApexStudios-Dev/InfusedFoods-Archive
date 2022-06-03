package xyz.apex.forge.infusedfoods.block.entity;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

import xyz.apex.forge.apexcore.revamp.block.entity.InventoryBlockEntity;
import xyz.apex.forge.apexcore.revamp.net.packet.SyncContainerPacket;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class InfusionStationBlockEntity extends InventoryBlockEntity implements ITickableTileEntity
{
	public static final String NBT_INFUSION_TIME = "InfusionTime";
	public static final String NBT_BLAZE_FUEL = "BlazeFuel";

	public static final String NBT_INFUSION_FLUID = "InfusionFluid";
	public static final String NBT_EFFECT = "Effect";
	public static final String NBT_AMOUNT = "Amount";
	public static final String NBT_DURATION = "Duration";
	public static final String NBT_AMPLIFIER = "Amplifier";

	public static final int SLOT_COUNT = 5;

	public static final int SLOT_POTION = 0;
	public static final int SLOT_BLAZE = 1;
	public static final int SLOT_FOOD = 2;
	public static final int SLOT_RESULT = 3;
	public static final int SLOT_BOTTLE = 4;

	public static final int INFUSION_TIME = 400; // same time as brewing stand

	private int infuseTime = 0;
	private int blazeFuel = 0;

	@Nullable private Effect effect;
	private int effectAmount;
	private int effectDuration;
	private int effectAmplifier;

	public InfusionStationBlockEntity(TileEntityType<? extends InfusionStationBlockEntity> blockEntityType)
	{
		super(blockEntityType, SLOT_COUNT);
	}

	@Nullable
	public Effect getEffect()
	{
		return effect;
	}

	public int getEffectAmount()
	{
		return effectAmount;
	}

	public int getEffectDuration()
	{
		return effectDuration;
	}

	public int getEffectAmplifier()
	{
		return effectAmplifier;
	}

	public int getInfuseTime()
	{
		return infuseTime;
	}

	public int getBlazeFuel()
	{
		return blazeFuel;
	}

	@Override
	public CompoundNBT serializeData()
	{
		CompoundNBT tagCompound = super.serializeData();

		if(infuseTime > 0)
			tagCompound.putInt(NBT_INFUSION_TIME, infuseTime);
		if(blazeFuel > 0)
			tagCompound.putInt(NBT_BLAZE_FUEL, blazeFuel);

		if(effect != null)
		{
			CompoundNBT fluidTag = new CompoundNBT();
			String effectRegistryName = Objects.requireNonNull(effect.getRegistryName()).toString();
			fluidTag.putString(NBT_EFFECT, effectRegistryName);
			fluidTag.putInt(NBT_AMOUNT, effectAmount);
			fluidTag.putInt(NBT_DURATION, effectDuration);
			fluidTag.putInt(NBT_AMPLIFIER, effectAmplifier);
			tagCompound.put(NBT_INFUSION_FLUID, fluidTag);
		}

		return tagCompound;
	}

	@Override
	public void deserializeData(CompoundNBT tagCompound)
	{
		if(tagCompound.contains(NBT_INFUSION_TIME, Constants.NBT.TAG_ANY_NUMERIC))
			infuseTime = tagCompound.getInt(NBT_INFUSION_TIME);
		if(tagCompound.contains(NBT_BLAZE_FUEL, Constants.NBT.TAG_ANY_NUMERIC))
			blazeFuel = tagCompound.getInt(NBT_BLAZE_FUEL);

		if(tagCompound.contains(NBT_INFUSION_FLUID, Constants.NBT.TAG_COMPOUND))
		{
			CompoundNBT fluidTag = tagCompound.getCompound(NBT_INFUSION_FLUID);
			ResourceLocation effectRegistryName = new ResourceLocation(fluidTag.getString(NBT_EFFECT));

			effect = ForgeRegistries.POTIONS.getValue(effectRegistryName);
			effectAmount = fluidTag.getInt(NBT_AMOUNT);
			effectDuration = fluidTag.getInt(NBT_DURATION);
			effectAmplifier = fluidTag.getInt(NBT_AMPLIFIER);
		}

		super.deserializeData(tagCompound);
	}

	@Override
	public void writeContainerSyncData(PacketBuffer buffer)
	{
		boolean flag = effect != null;
		buffer.writeBoolean(flag);

		if(flag)
			buffer.writeRegistryId(effect);

		buffer.writeInt(effectAmount);
		buffer.writeInt(effectAmplifier);
		buffer.writeInt(effectDuration);

		buffer.writeInt(infuseTime);
		buffer.writeInt(blazeFuel);
	}

	@Override
	public void readContainerSyncData(PacketBuffer buffer)
	{
		if(buffer.readBoolean())
			effect = buffer.readRegistryId();

		effectAmount = buffer.readInt();
		effectAmplifier = buffer.readInt();
		effectDuration = buffer.readInt();

		infuseTime = buffer.readInt();
		blazeFuel = buffer.readInt();
	}

	private boolean canInfuse()
	{
		if(effect == null || effectAmount < 0)
			return false;

		ItemStack food = itemHandler.getStackInSlot(SLOT_FOOD);

		if(food.isEmpty())
			return false;
		if(!PotionUtils.getCustomEffects(food).isEmpty())
			return false;

		ItemStack result = itemHandler.getStackInSlot(SLOT_RESULT);

		if(result.isEmpty())
			return true;
		if(!ItemStack.isSame(result, food))
			return false;

		List<EffectInstance> resultEffects = PotionUtils.getCustomEffects(result);

		if(resultEffects.size() == 1)
		{
			EffectInstance effectInstance = resultEffects.get(0);

			if(!Objects.equals(effect, effectInstance.getEffect()))
				return false;
			if(effectDuration != effectInstance.getDuration())
				return false;
			if(effectAmplifier != effectInstance.getAmplifier())
				return false;
		}

		return result.getCount() + 1 < food.getMaxStackSize();
	}

	@Override
	public void tick()
	{
		if(level == null)
			return;

		ItemStack potionStack = itemHandler.getStackInSlot(SLOT_POTION);
		ItemStack foodStack = itemHandler.getStackInSlot(SLOT_FOOD);
		ItemStack blazeStack = itemHandler.getStackInSlot(SLOT_BLAZE);
		ItemStack resultStack = itemHandler.getStackInSlot(SLOT_RESULT);
		ItemStack bottleStack = itemHandler.getStackInSlot(SLOT_BOTTLE);

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

		if(effect != null && effectAmount > 0)
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
							if(resultStack.isEmpty())
							{
								ItemStack foodToInfuse = foodStack.split(1).copy();
								foodToInfuse.setCount(1);
								PotionUtils.setCustomEffects(foodToInfuse, Collections.singletonList(new EffectInstance(effect, effectDuration, effectAmplifier)));
								itemHandler.setStackInSlot(SLOT_RESULT, foodToInfuse);
							}
							else
							{
								foodStack.shrink(1);
								resultStack.grow(1);
							}

							effectAmount--;
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

				if(effects.size() == 1)
				{
					EffectInstance effectInstance = effects.get(0);
					effect = effectInstance.getEffect();
					effectAmplifier = effectInstance.getAmplifier();
					effectDuration = effectInstance.getDuration();
					effectAmount = 5;

					itemHandler.setStackInSlot(SLOT_POTION, ItemStack.EMPTY);

					if(bottleStack.isEmpty())
						itemHandler.setStackInSlot(SLOT_BOTTLE, Items.GLASS_BOTTLE.getDefaultInstance());
					else
						bottleStack.grow(1);

					infuseTime = 0;
					changed = true;
				}
			}
		}

		if(changed)
			setChanged();
	}

	@Override
	public void setChanged()
	{
		super.setChanged();
		SyncContainerPacket.sendToClient(this);
	}

	public static int getColor(@Nullable Effect effect, int amplifier)
	{
		if(effect != null)
		{
			int l = amplifier + 1;

			if(l == 0)
				return 0;

			int k = effect.getColor();

			float f = (float) (l * (k >> 16 & 255)) / 255F;
			float f1 = (float) (l * (k >> 8 & 255)) / 255F;
			float f2 = (float) (l * (k >> 0 & 255)) / 255F;

			f = f / (float) l * 255F;
			f1 = f1 / (float) l * 255F;
			f2 = f2 / (float) l * 255F;
			return (int) f << 16 | (int) f1 << 8 | (int) f2;
		}

		return 0x385dc6;
	}
}
