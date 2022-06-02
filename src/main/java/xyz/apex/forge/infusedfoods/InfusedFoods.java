package xyz.apex.forge.infusedfoods;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import xyz.apex.forge.apexcore.lib.net.NetworkManager;
import xyz.apex.forge.apexcore.lib.util.EventBusHelper;
import xyz.apex.forge.infusedfoods.block.entity.InfusionStationBlockEntity;
import xyz.apex.forge.infusedfoods.block.entity.InfusionStationInventory;
import xyz.apex.forge.infusedfoods.init.IFRegistry;
import xyz.apex.forge.infusedfoods.network.PacketSyncInfusionData;

import java.util.List;
import java.util.Map;

@Mod(InfusedFoods.ID)
public final class InfusedFoods
{
	public static final String ID = "infusedfoods";
	public static final String NETWORK_VERSION = "1";
	public static final NetworkManager NETWORK = new NetworkManager(ID, "network", NETWORK_VERSION);

	public InfusedFoods()
	{
		IFRegistry.bootstrap();

		EventBusHelper.addListener(LivingEntityUseItemEvent.Finish.class, this::onItemUseFinish);

		EventBusHelper.addEnqueuedListener(FMLCommonSetupEvent.class, event -> NETWORK.registerPacket(PacketSyncInfusionData.class));

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> Client::new);
	}

	private void onItemUseFinish(LivingEntityUseItemEvent.Finish event)
	{
		LivingEntity entity = event.getEntityLiving();
		ItemStack stack = event.getResultStack();

		if(entity.level.isClientSide())
			return;

		if(isValidFood(stack))
		{
			List<EffectInstance> effects = PotionUtils.getCustomEffects(stack);

			for(EffectInstance effectInstance : effects)
			{
				Effect effect = effectInstance.getEffect();

				if(effect.isInstantenous())
					effect.applyInstantenousEffect(entity, entity, entity, effectInstance.getAmplifier(), 1D);
				else
				{
					if(entity.hasEffect(effect))
						entity.removeEffect(effect);

					entity.addEffect(effectInstance);
				}
			}
		}
	}

	public static boolean isValidFood(ItemStack stack)
	{
		return !stack.isEmpty() && stack.isEdible();
	}

	public static void appendPotionEffectTooltips(InfusionStationInventory.InfusionFluid fluid, List<ITextComponent> tooltip)
	{
		EffectInstance effectInstance = fluid.toEffectInstance();

		if(effectInstance != null)
		{
			Effect effect = effectInstance.getEffect();
			IFormattableTextComponent potionName = new TranslationTextComponent(effectInstance.getDescriptionId());

			int amplifier = fluid.getAmplifier();
			int duration = fluid.getDuration();

			if(amplifier > 0)
				potionName = new TranslationTextComponent("potion.withAmplifier", potionName, new TranslationTextComponent("potion.potency." + amplifier));

			if(duration > 20)
			{
				if(effectInstance.isNoCounter())
					potionName = new StringTextComponent("**:**");
				else
				{
					String durationFormat = EffectUtils.formatDuration(effectInstance, 1F);
					potionName = new TranslationTextComponent("potion.withDuration", potionName, durationFormat);
				}
			}

			tooltip.add(potionName.withStyle(effect.getCategory().getTooltipFormatting()));

			Map<Attribute, AttributeModifier> attributeModifiers = effect.getAttributeModifiers();

			if(!attributeModifiers.isEmpty())
			{
				tooltip.add(StringTextComponent.EMPTY);
				tooltip.add(new TranslationTextComponent("potion.whenDrank").withStyle(TextFormatting.DARK_PURPLE));

				attributeModifiers.forEach((attribute, attributeModifier) -> {
					AttributeModifier mod = attributeModifier;
					AttributeModifier mod1 = new AttributeModifier(mod.getName(), effect.getAttributeModifierValue(amplifier, mod), mod.getOperation());

					double d0 = mod1.getAmount();
					double d1;

					AttributeModifier.Operation operation = mod1.getOperation();

					if(operation != AttributeModifier.Operation.MULTIPLY_BASE && operation != AttributeModifier.Operation.MULTIPLY_TOTAL)
						d1 = d0;
					else
						d1 = d0 * 100D;

					if(d0 > 0D)
						tooltip.add(new TranslationTextComponent("attribute.modifier.plus." + operation.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslationTextComponent(attribute.getDescriptionId())).withStyle(TextFormatting.BLUE));
					else
						tooltip.add(new TranslationTextComponent("attribute.modifier.take." + operation.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslationTextComponent(attribute.getDescriptionId())).withStyle(TextFormatting.BLUE));
				});
			}
		}
	}

	public static void appendPotionEffectTooltips(ItemStack stack, List<ITextComponent> tooltip)
	{
		CompoundNBT stackTag = stack.getTag();

		if(stackTag != null && stackTag.contains(InfusionStationBlockEntity.NBT_INVENTORY, Constants.NBT.TAG_COMPOUND))
		{
			CompoundNBT inventoryTag = stackTag.getCompound(InfusionStationBlockEntity.NBT_INVENTORY);

			if(inventoryTag.contains(InfusionStationInventory.NBT_INFUSION_FLUID, Constants.NBT.TAG_COMPOUND))
			{
				CompoundNBT fluidTag = inventoryTag.getCompound(InfusionStationInventory.NBT_INFUSION_FLUID);
				InfusionStationInventory.InfusionFluid fluid = new InfusionStationInventory.InfusionFluid(fluidTag);
				appendPotionEffectTooltips(fluid, tooltip);
			}
		}
	}

	private static final class Client
	{
		private Client()
		{
			EventBusHelper.addListener(ItemTooltipEvent.class, this::onItemTooltip);
		}

		private void onItemTooltip(ItemTooltipEvent event)
		{
			ItemStack stack = event.getItemStack();
			List<ITextComponent> tooltip = event.getToolTip();

			if(isValidFood(stack))
			{
				List<EffectInstance> effects = PotionUtils.getCustomEffects(stack);

				if(!effects.isEmpty())
					PotionUtils.addPotionTooltip(stack, tooltip, 1F);
			}
		}
	}
}