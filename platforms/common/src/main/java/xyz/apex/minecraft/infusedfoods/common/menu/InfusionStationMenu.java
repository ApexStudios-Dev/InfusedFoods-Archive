package xyz.apex.minecraft.infusedfoods.common.menu;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import org.jetbrains.annotations.Nullable;
import xyz.apex.minecraft.apexcore.common.lib.component.block.entity.types.BlockEntityComponentTypes;
import xyz.apex.minecraft.apexcore.common.lib.menu.EnhancedSlot;
import xyz.apex.minecraft.apexcore.common.lib.menu.SimpleContainerMenu;
import xyz.apex.minecraft.infusedfoods.common.InfusedFoods;
import xyz.apex.minecraft.infusedfoods.common.InfusionHelper;
import xyz.apex.minecraft.infusedfoods.common.block.entity.InfusionStationBlockEntity;

public final class InfusionStationMenu extends SimpleContainerMenu
{
    private static final ResourceLocation SPRITE_SLOT_BLAZE = new ResourceLocation(InfusedFoods.ID, "item/empty_slot_blaze");
    private static final ResourceLocation SPRITE_SLOT_FOOD = new ResourceLocation(InfusedFoods.ID, "item/empty_slot_food");
    private static final ResourceLocation SPRITE_SLOT_POTION = new ResourceLocation(InfusedFoods.ID, "item/empty_slot_potion");

    private final DataSlot effectAmount;
    private final DataSlot effectAmplifier;
    private final DataSlot effectDuration;
    private final DataSlot effectId;
    private final DataSlot infuseTime;
    private final DataSlot blazeFuel;

    public InfusionStationMenu(MenuType<? extends InfusionStationMenu> menuType, int syncId, Inventory playerInventory, Container container, ContainerData containerData)
    {
        super(menuType, syncId, playerInventory, container);

        effectAmount = addDataSlot(DataSlot.forContainer(containerData, InfusionStationBlockEntity.DATA_SLOT_EFFECT_AMOUNT));
        effectAmplifier = addDataSlot(DataSlot.forContainer(containerData, InfusionStationBlockEntity.DATA_SLOT_EFFECT_AMPLIFIER));
        effectDuration = addDataSlot(DataSlot.forContainer(containerData, InfusionStationBlockEntity.DATA_SLOT_EFFECT_DURATION));
        effectId = addDataSlot(DataSlot.forContainer(containerData, InfusionStationBlockEntity.DATA_SLOT_EFFECT_ID));
        infuseTime = addDataSlot(DataSlot.forContainer(containerData, InfusionStationBlockEntity.DATA_SLOT_INFUSE_TIME));
        blazeFuel = addDataSlot(DataSlot.forContainer(containerData, InfusionStationBlockEntity.DATA_SLOT_BLAZE_FUEL));
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
    public Slot getContainerSlot(int slotIndex)
    {
        var slot = findSlot(container, slotIndex);
        return slot.isPresent() ? getSlot(slot.getAsInt()) : null;
    }

    @Nullable
    public MobEffect getEffect()
    {
        var effectId = this.effectId.get();
        return effectId < 0 ? null : BuiltInRegistries.MOB_EFFECT.getHolder(effectId).map(Holder.Reference::value).orElse(null);
    }

    @Override
    protected void bindSlots(Inventory playerInventory)
    {
        addSlot(new PotionSlot());
        addSlot(new BlazeSlot());
        addSlot(new FoodSlot());
        addSlot(new ResultSlot());
        addSlot(new BottleSlot());

        bindPlayerInventory(playerInventory, 8, 84, this::addSlot);
    }

    public static InfusionStationMenu forNetwork(MenuType<? extends InfusionStationMenu> menuType, int syncId, Inventory inventory, FriendlyByteBuf buffer)
    {
        var pos = buffer.readBlockPos();
        var blockEntity = InfusedFoods.BLOCK_ENTITY.getBlockEntityOptional(inventory.player.level(), pos).orElseThrow();
        var container = blockEntity.getRequiredComponent(BlockEntityComponentTypes.INVENTORY);

        return new InfusionStationMenu(menuType, syncId, inventory, container, blockEntity);
    }

    private final class PotionSlot extends EnhancedSlot
    {
        private PotionSlot()
        {
            super(InfusionStationMenu.this.container, InfusionStationBlockEntity.SLOT_POTION, 27, 8);
        }

        @Override
        public boolean mayPlace(ItemStack stack)
        {
            return PotionUtils.getPotion(stack).getEffects().size() == 1;
        }

        @Override
        public Pair<ResourceLocation, ResourceLocation> getNoItemIcon()
        {
            return Pair.of(InventoryMenu.BLOCK_ATLAS, SPRITE_SLOT_POTION);
        }
    }

    private final class BlazeSlot extends EnhancedSlot
    {
        private BlazeSlot()
        {
            super(InfusionStationMenu.this.container, InfusionStationBlockEntity.SLOT_BLAZE, 46, 8);
        }

        @Override
        public boolean mayPlace(ItemStack stack)
        {
            return stack.is(Items.BLAZE_POWDER);
        }

        @Override
        public Pair<ResourceLocation, ResourceLocation> getNoItemIcon()
        {
            return Pair.of(InventoryMenu.BLOCK_ATLAS, SPRITE_SLOT_BLAZE);
        }
    }

    private final class FoodSlot extends EnhancedSlot
    {
        private FoodSlot()
        {
            super(InfusionStationMenu.this.container, InfusionStationBlockEntity.SLOT_FOOD, 90, 22);
        }

        @Override
        public boolean mayPlace(ItemStack stack)
        {
            if(!InfusionHelper.isValidFood(stack))
                return false;

            return !InfusionHelper.isInfusedFood(stack);
        }

        @Override
        public Pair<ResourceLocation, ResourceLocation> getNoItemIcon()
        {
            return Pair.of(InventoryMenu.BLOCK_ATLAS, SPRITE_SLOT_FOOD);
        }
    }

    private final class ResultSlot extends EnhancedSlot
    {
        private ResultSlot()
        {
            super(InfusionStationMenu.this.container, InfusionStationBlockEntity.SLOT_RESULT, 134, 22);
        }

        @Override
        public boolean mayPlace(ItemStack stack)
        {
            return false;
        }
    }

    private final class BottleSlot extends EnhancedSlot
    {
        private BottleSlot()
        {
            super(InfusionStationMenu.this.container, InfusionStationBlockEntity.SLOT_BOTTLE, 8, 51);
        }

        @Override
        public boolean mayPlace(ItemStack stack)
        {
            return false;
        }
    }
}
