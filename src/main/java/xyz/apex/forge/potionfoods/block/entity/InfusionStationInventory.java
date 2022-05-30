package xyz.apex.forge.potionfoods.block.entity;

import com.google.common.util.concurrent.Runnables;
import org.apache.commons.lang3.Validate;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public final class InfusionStationInventory extends ItemStackHandler
{
	public static final int SLOT_COUNT = 5;

	public static final int SLOT_POTION = 0;
	public static final int SLOT_BLAZE = 1;
	public static final int SLOT_FOOD = 2;
	public static final int SLOT_RESULT = 3;
	public static final int SLOT_BOTTLE = 4;

	private static final String NBT_INFUSION_FLUID = "InfusionFluid";
	private static final String NBT_EFFECT = "Effect";
	private static final String NBT_AMOUNT = "Amount";
	private static final String NBT_DURATION = "Duration";
	private static final String NBT_AMPLIFIER = "Amplifier";

	private InfusionFluid infusionFluid = InfusionFluid.EMPTY;
	private final Runnable onFluidChanged;

	public InfusionStationInventory(Runnable onFluidChanged)
	{
		super(SLOT_COUNT);

		this.onFluidChanged = onFluidChanged;
	}

	public InfusionStationInventory()
	{
		this(Runnables.doNothing());
	}

	public ItemStack getPotion()
	{
		return getStackInSlot(SLOT_POTION);
	}

	public ItemStack getBlaze()
	{
		return getStackInSlot(SLOT_BLAZE);
	}

	public ItemStack getFood()
	{
		return getStackInSlot(SLOT_FOOD);
	}

	public ItemStack getResult()
	{
		return getStackInSlot(SLOT_RESULT);
	}

	public ItemStack getBottle()
	{
		return getStackInSlot(SLOT_BOTTLE);
	}

	public void setPotion(ItemStack potion)
	{
		setStackInSlot(SLOT_POTION, potion);
	}

	public void setBlaze(ItemStack potion)
	{
		setStackInSlot(SLOT_BLAZE, potion);
	}

	public void setFood(ItemStack potion)
	{
		setStackInSlot(SLOT_FOOD, potion);
	}

	public void setResult(ItemStack potion)
	{
		setStackInSlot(SLOT_RESULT, potion);
	}

	public void setBottle(ItemStack potion)
	{
		setStackInSlot(SLOT_BOTTLE, potion);
	}

	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack)
	{
		if(slot == SLOT_POTION)
		{
			if(stack.getItem() == Items.POTION && !PotionUtils.getPotion(stack).getEffects().isEmpty())
				return true;
		}

		if(slot == SLOT_BLAZE)
		{
			if(stack.getItem() == Items.BLAZE_POWDER)
				return true;
		}

		if(slot == SLOT_FOOD)
		{
			if(stack.isEdible() && PotionUtils.getCustomEffects(stack).isEmpty())
				return true;
		}

		return false;
	}

	@Override
	public void deserializeNBT(CompoundNBT tagCompound)
	{
		infusionFluid = InfusionFluid.EMPTY;

		super.deserializeNBT(tagCompound);

		if(tagCompound.contains(NBT_INFUSION_FLUID, Constants.NBT.TAG_COMPOUND))
		{
			CompoundNBT fluidTag = tagCompound.getCompound(NBT_INFUSION_FLUID);
			infusionFluid = new InfusionFluid(fluidTag);
		}
	}

	@Override
	public CompoundNBT serializeNBT()
	{
		CompoundNBT tagCompound = super.serializeNBT();

		if(!infusionFluid.isEmpty())
		{
			CompoundNBT fluidTag = infusionFluid.serialize();
			tagCompound.put(NBT_INFUSION_FLUID, fluidTag);
		}

		return tagCompound;
	}

	public InfusionFluid getInfusionFluid()
	{
		return infusionFluid;
	}

	public boolean hasInfusionFluid()
	{
		return !infusionFluid.isEmpty();
	}

	public void incrementInfusionFluid(EffectInstance effectInstance)
	{
		if(infusionFluid.isEmpty())
			infusionFluid = new InfusionFluid(effectInstance);
		else if(infusionFluid.is(effectInstance))
			infusionFluid = infusionFluid.increment(5);
	}

	public void decrementInfusionFluid(int amount)
	{
		if(!infusionFluid.isEmpty())
		{
			infusionFluid = infusionFluid.decrement(amount);
			onFluidChanged.run();
		}
	}

	public static final class InfusionFluid
	{
		public static final InfusionFluid EMPTY = new InfusionFluid(null, 0, 0, 0);

		@Nullable private final Effect effect;
		private final int amount;
		private final int duration;
		private final int amplifier;

		private InfusionFluid(@Nullable Effect effect, int amount, int duration, int amplifier)
		{
			this.effect = effect;
			this.amount = amount;
			this.duration = duration;
			this.amplifier = amplifier;
		}

		public InfusionFluid(CompoundNBT tag)
		{
			Validate.isTrue(tag.contains(NBT_EFFECT, Constants.NBT.TAG_STRING));
			Validate.isTrue(tag.contains(NBT_AMOUNT, Constants.NBT.TAG_ANY_NUMERIC));
			Validate.isTrue(tag.contains(NBT_DURATION, Constants.NBT.TAG_ANY_NUMERIC));
			Validate.isTrue(tag.contains(NBT_AMPLIFIER, Constants.NBT.TAG_ANY_NUMERIC));

			ResourceLocation registryName = new ResourceLocation(tag.getString(NBT_EFFECT));

			// effect = Registry.MOB_EFFECT.get(registryName);
			effect = ForgeRegistries.POTIONS.getValue(registryName);
			amount = tag.getInt(NBT_AMOUNT);
			duration = tag.getInt(NBT_DURATION);
			amplifier = tag.getInt(NBT_AMPLIFIER);
		}

		public InfusionFluid(InfusionFluid fluid)
		{
			this(fluid.effect, fluid.amount, fluid.duration, fluid.amplifier);
		}

		public InfusionFluid(EffectInstance effectInstance)
		{
			this(effectInstance.getEffect(), 5, effectInstance.getDuration(), effectInstance.getAmplifier());
		}

		public InfusionFluid copy()
		{
			return new InfusionFluid(this);
		}

		@Nullable
		public Effect getEffect()
		{
			return effect;
		}

		public int getAmount()
		{
			return amount;
		}

		public boolean isEmpty()
		{
			return amount <= 0 || effect == null;
		}

		public int getDuration()
		{
			return duration;
		}

		public int getAmplifier()
		{
			return amplifier;
		}

		public int getColor()
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

		private InfusionFluid decrement(int count)
		{
			if(amount - count <= 0)
				return EMPTY;

			return new InfusionFluid(effect, amount - count, duration, amplifier);
		}

		private InfusionFluid increment(int count)
		{
			int newAmount = Math.min(amount + count, 5);
			return new InfusionFluid(effect, newAmount, duration, amplifier);
		}

		@Nullable
		public EffectInstance toEffectInstance()
		{
			if(effect == null)
				return null;

			return new EffectInstance(effect, duration, amplifier, false, true, true, null);
		}

		public boolean is(Effect effect)
		{
			return !isEmpty() && this.effect == effect;
		}

		public boolean is(EffectInstance effectInstance)
		{
			return !isEmpty() && is(effectInstance.getEffect());
		}

		public CompoundNBT serialize()
		{
			CompoundNBT tag = new CompoundNBT();

			ResourceLocation registryName = Objects.requireNonNull(effect.getRegistryName());
			tag.putString(NBT_EFFECT, registryName.toString());
			tag.putInt(NBT_AMOUNT, amount);
			tag.putInt(NBT_DURATION, duration);
			tag.putInt(NBT_AMPLIFIER, amplifier);

			return tag;
		}
	}
}
