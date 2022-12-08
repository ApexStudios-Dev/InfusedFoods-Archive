package xyz.apex.forge.infusedfoods.container;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import xyz.apex.forge.apexcore.lib.container.BaseMenu;
import xyz.apex.forge.infusedfoods.block.entity.InfusionStationBlockEntity;
import xyz.apex.forge.infusedfoods.init.IFElements;

import java.util.Objects;

public final class InfusionStationMenu extends BaseMenu
{
	public final Player player;
	public final IItemHandler itemHandler;

	private final DataSlot effectAmount;
	private final DataSlot effectAmplifier;
	private final DataSlot effectDuration;
	private final DataSlot effectId;
	private final DataSlot infuseTime;
	private final DataSlot blazeFuel;

	public InfusionStationMenu(MenuType<? extends InfusionStationMenu> menuType, int windowId, Inventory playerInventory, FriendlyByteBuf buffer)
	{
		super(menuType, windowId, playerInventory, buffer);

		player = playerInventory.player;
		this.itemHandler = Objects.requireNonNull(getItemHandler());

		addSlot(new PotionSlot());
		addSlot(new BlazeSlot());
		addSlot(new FoodSlot());
		addSlot(new ResultSlot());
		addSlot(new BottleSlot());

		bindPlayerInventory(this, 8, 84);

		var blockEntity = IFElements.INFUSION_STATION_BLOCK_ENTITY.get(player.level, pos).orElseThrow();

		effectAmount = addDataSlot(DataSlot.forContainer(blockEntity, InfusionStationBlockEntity.DATA_SLOT_EFFECT_AMOUNT));
		effectAmplifier = addDataSlot(DataSlot.forContainer(blockEntity, InfusionStationBlockEntity.DATA_SLOT_EFFECT_AMPLIFIER));
		effectDuration = addDataSlot(DataSlot.forContainer(blockEntity, InfusionStationBlockEntity.DATA_SLOT_EFFECT_DURATION));
		effectId = addDataSlot(DataSlot.forContainer(blockEntity, InfusionStationBlockEntity.DATA_SLOT_EFFECT_ID));
		infuseTime = addDataSlot(DataSlot.forContainer(blockEntity, InfusionStationBlockEntity.DATA_SLOT_INFUSE_TIME));
		blazeFuel = addDataSlot(DataSlot.forContainer(blockEntity, InfusionStationBlockEntity.DATA_SLOT_BLAZE_FUEL));
	}

	public int getEffectAmount()
	{
		return effectAmount.get();
	}

	public int getEffectAmplifier()
	{
		return effectAmplifier.get();
	}

	public int getEffectDuration()
	{
		return effectDuration.get();
	}

	public int getInfuseTime()
	{
		return infuseTime.get();
	}

	public int getBlazeFuel()
	{
		return blazeFuel.get();
	}

	@Nullable
	public MobEffect getEffect()
	{
		var effectId = this.effectId.get();
		return effectId < 0 ? null : BuiltInRegistries.MOB_EFFECT.getHolder(effectId).map(Holder::value).orElse(null);
	}

	@Nullable
	@Override
	public IItemHandler getItemHandler()
	{
		var blockEntity = Objects.requireNonNull(player.level.getBlockEntity(pos));
		return getItemHandlerFromBlockEntity(blockEntity).resolve().orElse(null);
	}

	@Override
	protected void onInventoryChanges()
	{
		super.onInventoryChanges();
		setBlockEntityChanged();
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
