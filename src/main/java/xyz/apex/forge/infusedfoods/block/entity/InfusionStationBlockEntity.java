package xyz.apex.forge.infusedfoods.block.entity;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import xyz.apex.forge.apexcore.revamp.block.entity.InventoryBlockEntity;
import xyz.apex.forge.apexcore.revamp.net.packet.SyncContainerPacket;

import java.util.Collections;
import java.util.Objects;

public final class InfusionStationBlockEntity extends InventoryBlockEntity
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

	@Nullable private MobEffect effect;
	private int effectAmount;
	private int effectDuration;
	private int effectAmplifier;

	public InfusionStationBlockEntity(BlockEntityType<? extends InfusionStationBlockEntity> blockEntityType, BlockPos pos, BlockState blockState)
	{
		super(blockEntityType, pos, blockState, SLOT_COUNT);
	}

	@Nullable
	public MobEffect getEffect()
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
	public CompoundTag serializeData()
	{
		var tagCompound = super.serializeData();

		if(infuseTime > 0)
			tagCompound.putInt(NBT_INFUSION_TIME, infuseTime);
		if(blazeFuel > 0)
			tagCompound.putInt(NBT_BLAZE_FUEL, blazeFuel);

		if(effect != null)
		{
			var fluidTag = new CompoundTag();
			var effectRegistryName = Objects.requireNonNull(effect.getRegistryName()).toString();
			fluidTag.putString(NBT_EFFECT, effectRegistryName);
			fluidTag.putInt(NBT_AMOUNT, effectAmount);
			fluidTag.putInt(NBT_DURATION, effectDuration);
			fluidTag.putInt(NBT_AMPLIFIER, effectAmplifier);
			tagCompound.put(NBT_INFUSION_FLUID, fluidTag);
		}

		return tagCompound;
	}

	@Override
	public void deserializeData(CompoundTag tagCompound)
	{
		if(tagCompound.contains(NBT_INFUSION_TIME, Tag.TAG_ANY_NUMERIC))
			infuseTime = tagCompound.getInt(NBT_INFUSION_TIME);
		if(tagCompound.contains(NBT_BLAZE_FUEL, Tag.TAG_ANY_NUMERIC))
			blazeFuel = tagCompound.getInt(NBT_BLAZE_FUEL);

		if(tagCompound.contains(NBT_INFUSION_FLUID, Tag.TAG_COMPOUND))
		{
			var fluidTag = tagCompound.getCompound(NBT_INFUSION_FLUID);
			var effectRegistryName = new ResourceLocation(fluidTag.getString(NBT_EFFECT));

			effect = ForgeRegistries.MOB_EFFECTS.getValue(effectRegistryName);
			effectAmount = fluidTag.getInt(NBT_AMOUNT);
			effectDuration = fluidTag.getInt(NBT_DURATION);
			effectAmplifier = fluidTag.getInt(NBT_AMPLIFIER);
		}

		super.deserializeData(tagCompound);
	}

	@Override
	public void writeContainerSyncData(FriendlyByteBuf buffer)
	{
		var flag = effect != null;
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
	public void readContainerSyncData(FriendlyByteBuf buffer)
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

		var food = itemHandler.getStackInSlot(SLOT_FOOD);

		if(food.isEmpty())
			return false;
		if(!PotionUtils.getCustomEffects(food).isEmpty())
			return false;

		var result = itemHandler.getStackInSlot(SLOT_RESULT);

		if(result.isEmpty())
			return true;
		if(!ItemStack.isSame(result, food))
			return false;

		var resultEffects = PotionUtils.getCustomEffects(result);

		if(resultEffects.size() == 1)
		{
			var effectInstance = resultEffects.get(0);

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
	public void setChanged()
	{
		super.setChanged();
		SyncContainerPacket.sendToClient(this);
	}

	public static int getColor(@Nullable MobEffect effect, int amplifier)
	{
		if(effect != null)
		{
			var l = amplifier + 1;

			if(l == 0)
				return 0;

			var k = effect.getColor();

			var f = (float) (l * (k >> 16 & 255)) / 255F;
			var f1 = (float) (l * (k >> 8 & 255)) / 255F;
			var f2 = (float) (l * (k >> 0 & 255)) / 255F;

			f = f / (float) l * 255F;
			f1 = f1 / (float) l * 255F;
			f2 = f2 / (float) l * 255F;
			return (int) f << 16 | (int) f1 << 8 | (int) f2;
		}

		return 0x385dc6;
	}

	public static void tick(Level level, BlockPos pos, BlockState blockState, InfusionStationBlockEntity blockEntity)
	{
		var potionStack = blockEntity.itemHandler.getStackInSlot(SLOT_POTION);
		var foodStack = blockEntity.itemHandler.getStackInSlot(SLOT_FOOD);
		var blazeStack = blockEntity.itemHandler.getStackInSlot(SLOT_BLAZE);
		var resultStack = blockEntity.itemHandler.getStackInSlot(SLOT_RESULT);
		var bottleStack = blockEntity.itemHandler.getStackInSlot(SLOT_BOTTLE);

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

		if(blockEntity.effect != null && blockEntity.effectAmount > 0)
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
							if(resultStack.isEmpty())
							{
								var foodToInfuse = foodStack.split(1).copy();
								foodToInfuse.setCount(1);
								PotionUtils.setCustomEffects(foodToInfuse, Collections.singletonList(new MobEffectInstance(blockEntity.effect, blockEntity.effectDuration, blockEntity.effectAmplifier)));
								blockEntity.itemHandler.setStackInSlot(SLOT_RESULT, foodToInfuse);
							}
							else
							{
								foodStack.shrink(1);
								resultStack.grow(1);
							}

							blockEntity.effectAmount--;
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
					blockEntity.effect = effectInstance.getEffect();
					blockEntity.effectAmplifier = effectInstance.getAmplifier();
					blockEntity.effectDuration = effectInstance.getDuration();
					blockEntity.effectAmount = 5;

					blockEntity.itemHandler.setStackInSlot(SLOT_POTION, ItemStack.EMPTY);

					if(bottleStack.isEmpty())
						blockEntity.itemHandler.setStackInSlot(SLOT_BOTTLE, Items.GLASS_BOTTLE.getDefaultInstance());
					else
						bottleStack.grow(1);

					blockEntity.infuseTime = 0;
					changed = true;
				}
			}
		}

		if(changed)
			blockEntity.setChanged();
	}
}