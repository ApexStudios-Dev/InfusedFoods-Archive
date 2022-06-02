package xyz.apex.forge.infusedfoods;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import xyz.apex.forge.apexcore.lib.net.NetworkManager;
import xyz.apex.forge.apexcore.lib.util.EventBusHelper;
import xyz.apex.forge.infusedfoods.block.entity.InfusionStationBlockEntity;
import xyz.apex.forge.infusedfoods.block.entity.InfusionStationInventory;
import xyz.apex.forge.infusedfoods.client.renderer.model.InfusionStationModel;
import xyz.apex.forge.infusedfoods.init.IFRegistry;
import xyz.apex.forge.infusedfoods.network.PacketSyncInfusionData;

import java.util.List;

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
		var entity = event.getEntityLiving();
		var stack = event.getResultStack();

		if(entity.level.isClientSide())
			return;

		if(isValidFood(stack))
		{
			var effects = PotionUtils.getCustomEffects(stack);

			for(var effectInstance : effects)
			{
				var effect = effectInstance.getEffect();

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

	public static void appendPotionEffectTooltips(InfusionStationInventory.InfusionFluid fluid, List<Component> tooltip)
	{
		var effectInstance = fluid.toEffectInstance();

		if(effectInstance != null)
		{
			var effect = effectInstance.getEffect();
			var potionName = new TranslatableComponent(effectInstance.getDescriptionId());

			var amplifier = fluid.getAmplifier();
			var duration = fluid.getDuration();

			if(amplifier > 0)
				potionName = new TranslatableComponent("potion.withAmplifier", potionName, new TranslatableComponent("potion.potency." + amplifier));
			if(duration > 20)
			{
				var durationFormat = MobEffectUtil.formatDuration(effectInstance, 1F);
				potionName = new TranslatableComponent("potion.withDuration", potionName, durationFormat);
			}

			tooltip.add(potionName.withStyle(effect.getCategory().getTooltipFormatting()));

			var attributeModifiers = effect.getAttributeModifiers();

			if(!attributeModifiers.isEmpty())
			{
				tooltip.add(TextComponent.EMPTY);
				tooltip.add(new TranslatableComponent("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

				attributeModifiers.forEach((attribute, attributeModifier) -> {
					var mod = attributeModifier;
					var mod1 = new AttributeModifier(mod.getName(), effect.getAttributeModifierValue(amplifier, mod), mod.getOperation());

					var d0 = mod1.getAmount();
					double d1;

					var operation = mod1.getOperation();

					if(operation != AttributeModifier.Operation.MULTIPLY_BASE && operation != AttributeModifier.Operation.MULTIPLY_TOTAL)
						d1 = d0;
					else
						d1 = d0 * 100D;

					if(d0 > 0D)
						tooltip.add(new TranslatableComponent("attribute.modifier.plus." + operation.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(attribute.getDescriptionId())).withStyle(ChatFormatting.BLUE));
					else
						tooltip.add(new TranslatableComponent("attribute.modifier.take." + operation.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(attribute.getDescriptionId())).withStyle(ChatFormatting.BLUE));
				});
			}
		}
	}

	public static void appendPotionEffectTooltips(ItemStack stack, List<Component> tooltip)
	{
		var stackTag = stack.getTag();

		if(stackTag != null && stackTag.contains(InfusionStationBlockEntity.NBT_INVENTORY, Tag.TAG_COMPOUND))
		{
			var inventoryTag = stackTag.getCompound(InfusionStationBlockEntity.NBT_INVENTORY);

			if(inventoryTag.contains(InfusionStationInventory.NBT_INFUSION_FLUID, Tag.TAG_COMPOUND))
			{
				var fluidTag = inventoryTag.getCompound(InfusionStationInventory.NBT_INFUSION_FLUID);
				var fluid = new InfusionStationInventory.InfusionFluid(fluidTag);
				appendPotionEffectTooltips(fluid, tooltip);
			}
		}
	}

	private static final class Client
	{
		private Client()
		{
			EventBusHelper.addListener(ItemTooltipEvent.class, this::onItemTooltip);
			EventBusHelper.addListener(EntityRenderersEvent.RegisterLayerDefinitions.class, event -> event.registerLayerDefinition(InfusionStationModel.LAYER_LOCATION, InfusionStationModel::createDefinition));
		}

		private void onItemTooltip(ItemTooltipEvent event)
		{
			var stack = event.getItemStack();
			var tooltip = event.getToolTip();

			if(isValidFood(stack))
			{
				var effects = PotionUtils.getCustomEffects(stack);

				if(!effects.isEmpty())
					PotionUtils.addPotionTooltip(stack, tooltip, 1F);
			}
		}
	}
}