package xyz.apex.minecraft.infusedfoods.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import xyz.apex.minecraft.apexcore.common.lib.component.block.entity.*;
import xyz.apex.minecraft.apexcore.common.lib.component.block.entity.types.BlockEntityComponentTypes;
import xyz.apex.minecraft.infusedfoods.common.InfusedFoods;
import xyz.apex.minecraft.infusedfoods.common.InfusionHelper;
import xyz.apex.minecraft.infusedfoods.common.menu.InfusionStationMenu;

import java.util.Collections;
import java.util.Objects;

public final class InfusionStationBlockEntity extends BaseBlockEntityComponentHolder
{
    public static final BlockEntityComponentType<InventoryBlockEntityComponent> COMPONENT_TYPE = BlockEntityComponentType.register(InfusedFoods.ID, "inventory", InventoryBlockEntityComponent::new);

    public static final String NBT_INFUSION_TIME = "InfusionTime";
    public static final String NBT_BLAZE_FUEL = "BlazeFuel";

    public static final String NBT_INFUSION_FLUID = "InfusionFluid";
    public static final String NBT_EFFECT = "Effect";
    public static final String NBT_AMOUNT = "Amount";
    public static final String NBT_DURATION = "Duration";
    public static final String NBT_AMPLIFIER = "Amplifier";

    public static final int SLOT_POTION = 0;
    public static final int SLOT_BOTTLE = 1;
    public static final int SLOT_BLAZE = 2;
    public static final int SLOT_FOOD = 3;
    public static final int SLOT_RESULT = 4;
    public static final int SLOT_COUNT = 5;

    public static final int DATA_SLOT_EFFECT_AMOUNT = 0;
    public static final int DATA_SLOT_EFFECT_AMPLIFIER = 1;
    public static final int DATA_SLOT_EFFECT_DURATION = 2;
    public static final int DATA_SLOT_EFFECT_ID = 3;
    public static final int DATA_SLOT_INFUSE_TIME = 4;
    public static final int DATA_SLOT_BLAZE_FUEL = 5;
    public static final int DATA_SLOT_COUNT = 6;

    public static final int INFUSION_TIME = 400;
    public static final int BLAZE_FUEL = 20;

    public InfusionStationBlockEntity(BlockEntityType<? extends BaseBlockEntityComponentHolder> blockEntityType, BlockPos pos, BlockState blockState)
    {
        super(blockEntityType, pos, blockState);
    }

    @Nullable
    public MobEffect getEffect()
    {
        return getRequiredComponent(COMPONENT_TYPE).effect;
    }

    public int getEffectAmount()
    {
        return getRequiredComponent(COMPONENT_TYPE).effectAmount;
    }

    public int getEffectAmplifier()
    {
        return getRequiredComponent(COMPONENT_TYPE).effectAmplifier;
    }

    @Override
    protected void registerComponents(BlockEntityComponentRegistrar registrar)
    {
        registrar.register(COMPONENT_TYPE);
        registrar.register(BlockEntityComponentTypes.LOOT_TABLE);
        registrar.register(BlockEntityComponentTypes.LOCK_CODE);
        registrar.register(BlockEntityComponentTypes.NAMEABLE);
    }

    private boolean canInfuse()
    {
        var container = getRequiredComponent(COMPONENT_TYPE);

        if(container.blazeFuel <= 0)
            return false;
        if(container.effect == null || container.effectAmount < 0)
            return false;

        var food = container.getItem(SLOT_FOOD);

        if(!InfusionHelper.isValidFood(food))
            return false;
        if(InfusionHelper.isInfusedFood(food))
            return false;

        var result = container.getItem(SLOT_RESULT);

        if(result.isEmpty())
            return true;
        if(!ItemStack.isSameItem(result, food))
            return false;

        var resultEffects = PotionUtils.getCustomEffects(result);

        if(resultEffects.size() == 1)
        {
            var effectInstance = resultEffects.get(0);

            if(!Objects.equals(container.effect, effectInstance.getEffect()))
                return false;
            if(container.effectDuration != effectInstance.getDuration())
                return false;
            if(container.effectAmplifier != effectInstance.getAmplifier())
                return false;
        }

        return result.getCount() + 1 < food.getMaxStackSize();
    }

    @Override
    protected AbstractContainerMenu createMenu(int syncId, Inventory inventory)
    {
        return new InfusionStationMenu(InfusedFoods.MENU.value(), syncId, inventory, this, this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState blockState, InfusionStationBlockEntity blockEntity)
    {
        var container = blockEntity.getRequiredComponent(COMPONENT_TYPE);
        var potion = container.getItem(SLOT_POTION);
        var food = container.getItem(SLOT_FOOD);
        var blaze = container.getItem(SLOT_BLAZE);
        var result = container.getItem(SLOT_RESULT);
        var bottle = container.getItem(SLOT_BOTTLE);

        var changed = false;

        if(container.blazeFuel <= 0)
        {
            container.blazeFuel = 0;

            if(!blaze.isEmpty())
            {
                container.blazeFuel = BLAZE_FUEL;
                blaze.shrink(1);
                changed = true;
            }
        }

        if(container.infuseTime > 0 && !blockEntity.canInfuse())
        {
            container.infuseTime = 0;
            changed = true;
        }

        if(container.effect != null && container.effectAmount > 0)
        {
            if(blockEntity.canInfuse())
            {
                if(container.infuseTime == 0)
                {
                    container.blazeFuel--;
                    container.infuseTime = INFUSION_TIME;
                    changed = true;
                }
                else
                {
                    container.infuseTime--;

                    if(container.infuseTime == 0)
                    {
                        if(result.isEmpty())
                        {
                            var foodToUse = food.split(1).copy();
                            foodToUse.setCount(1);
                            PotionUtils.setCustomEffects(foodToUse, Collections.singletonList(new MobEffectInstance(container.effect, container.effectDuration, container.effectAmplifier)));
                            container.setItem(SLOT_RESULT, foodToUse);
                        }
                        else
                        {
                            food.shrink(1);
                            result.grow(1);
                        }

                        container.effectAmount--;
                    }

                    changed = true;
                }
            }
        }
        else
        {
            if(!potion.isEmpty())
            {
                var pot = PotionUtils.getPotion(potion);
                var effects = pot.getEffects();

                if(effects.size() == 1)
                {
                    var effectInstance = effects.get(0);

                    container.effect = effectInstance.getEffect();
                    container.effectAmplifier = effectInstance.getAmplifier();
                    container.effectDuration = effectInstance.getDuration();
                    container.effectAmount = 5;

                    container.setItem(SLOT_POTION, ItemStack.EMPTY);

                    if(bottle.isEmpty())
                        container.setItem(SLOT_BOTTLE, Items.GLASS_BOTTLE.getDefaultInstance());
                    else
                        bottle.grow(1);

                    container.infuseTime = 0;
                    changed = true;
                }
            }
        }

        if(changed)
            blockEntity.setChanged();
    }

    public static final class InventoryBlockEntityComponent extends BaseContainerBlockEntityComponent<InventoryBlockEntityComponent>
    {
        private int infuseTime = 0;
        private int blazeFuel = 0;
        @Nullable private MobEffect effect;
        private int effectAmount;
        private int effectDuration;
        private int effectAmplifier;

        private InventoryBlockEntityComponent(BlockEntityComponentHolder componentHolder)
        {
            super(componentHolder);

            super.withSlotCount(SLOT_COUNT);
        }

        @Override
        public InventoryBlockEntityComponent withSlotCount(int slotCount)
        {
            return this;
        }

        @Override
        public void serializeInto(CompoundTag tag, boolean forNetwork)
        {
            super.serializeInto(tag, forNetwork);

            if(infuseTime > 0)
                tag.putInt(NBT_INFUSION_TIME, infuseTime);
            if(blazeFuel > 0)
                tag.putInt(NBT_BLAZE_FUEL, blazeFuel);

            if(effect != null)
            {
                var fluidTag = new CompoundTag();
                var effectRegistryName = Objects.requireNonNull(BuiltInRegistries.MOB_EFFECT.getKey(effect)).toString();
                fluidTag.putString(NBT_EFFECT, effectRegistryName);
                fluidTag.putInt(NBT_AMOUNT, effectAmount);
                fluidTag.putInt(NBT_DURATION, effectDuration);
                fluidTag.putInt(NBT_AMPLIFIER, effectAmplifier);
                tag.put(NBT_INFUSION_FLUID, fluidTag);
            }
        }

        @Override
        public void deserializeFrom(CompoundTag tag, boolean fromNetwork)
        {
            super.deserializeFrom(tag, fromNetwork);

            infuseTime = 0;
            blazeFuel = 0;
            effect = null;
            effectAmount = 0;
            effectAmplifier = 0;
            effectDuration = 0;

            if(tag.contains(NBT_INFUSION_TIME, Tag.TAG_ANY_NUMERIC))
                infuseTime = tag.getInt(NBT_INFUSION_TIME);
            if(tag.contains(NBT_BLAZE_FUEL, Tag.TAG_ANY_NUMERIC))
                blazeFuel = tag.getInt(NBT_BLAZE_FUEL);

            if(tag.contains(NBT_INFUSION_FLUID, Tag.TAG_COMPOUND))
            {
                var fluidTag = tag.getCompound(NBT_INFUSION_FLUID);
                effect = BuiltInRegistries.MOB_EFFECT.get(new ResourceLocation(fluidTag.getString(NBT_EFFECT)));
                effectAmount = fluidTag.getInt(NBT_AMOUNT);
                effectDuration = fluidTag.getInt(NBT_DURATION);
                effectAmplifier = fluidTag.getInt(NBT_AMPLIFIER);
            }
        }

        @Override
        public int[] getSlotsForFace(Direction side)
        {
            return super.getSlotsForFace(side);
        }

        @Override
        public int get(int index)
        {
            return switch(index) {
                case DATA_SLOT_EFFECT_AMOUNT -> effectAmount;
                case DATA_SLOT_EFFECT_AMPLIFIER -> effectAmplifier;
                case DATA_SLOT_EFFECT_DURATION -> effectDuration;
                case DATA_SLOT_EFFECT_ID -> BuiltInRegistries.MOB_EFFECT.getId(effect);
                case DATA_SLOT_INFUSE_TIME -> infuseTime;
                case DATA_SLOT_BLAZE_FUEL -> blazeFuel;
                default -> -1;
            };
        }

        @Override
        public void set(int index, int value)
        {
            switch(index) {
                case DATA_SLOT_EFFECT_AMOUNT -> effectAmount = value;
                case DATA_SLOT_EFFECT_AMPLIFIER -> effectAmplifier = value;
                case DATA_SLOT_EFFECT_DURATION -> effectDuration = value;
                case DATA_SLOT_EFFECT_ID -> effect = value >= 0 ? BuiltInRegistries.MOB_EFFECT.getHolder(value).map(Holder.Reference::value).orElse(null) : null;
                case DATA_SLOT_INFUSE_TIME -> infuseTime = value;
                case DATA_SLOT_BLAZE_FUEL -> blazeFuel = value;
            }
        }

        @Override
        public int getCount()
        {
            return DATA_SLOT_COUNT;
        }
    }
}
