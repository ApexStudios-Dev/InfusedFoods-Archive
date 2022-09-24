package xyz.apex.forge.infusedfoods;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import xyz.apex.forge.apexcore.lib.util.EventBusHelper;
import xyz.apex.forge.commonality.Mods;
import xyz.apex.forge.infusedfoods.client.renderer.model.InfusionStationModel;
import xyz.apex.forge.infusedfoods.init.IFRegistry;

import java.util.List;
import java.util.Map;

import static xyz.apex.forge.apexcore.lib.block.entity.BaseBlockEntity.NBT_APEX;
import static xyz.apex.forge.infusedfoods.block.entity.InfusionStationBlockEntity.*;

@Mod(Mods.INFUSED_FOODS)
public final class InfusedFoods
{
	public InfusedFoods()
	{
		IFRegistry.bootstrap();
		EventBusHelper.addListener(LivingEntityUseItemEvent.Finish.class, this::onItemUseFinish);
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> Client::new);
	}

	private void onItemUseFinish(LivingEntityUseItemEvent.Finish event)
	{
		var entity = event.getEntity();
		var stack = event.getItem();

		if(entity.level.isClientSide())
			return;

		if(isValidFood(stack))
		{
			var effects = PotionUtils.getCustomEffects(stack);

			for(var effectInstance : effects)
			{
				var effect = effectInstance.getEffect();

				if(effect.isInstantenous())
					effect.applyInstantenousEffect(null, null, entity, effectInstance.getAmplifier(), 1D);
				else
				{
					if(entity.hasEffect(effect))
						entity.removeEffect(effect);

					entity.addEffect(new MobEffectInstance(effectInstance));
				}
			}
		}
	}

	public static boolean isValidFood(ItemStack stack)
	{
		return !stack.isEmpty() && stack.isEdible();
	}

	public static void appendPotionEffectTooltips(@Nullable MobEffect effect, int amplifier, int duration, List<Component> tooltip)
	{
		if(effect != null)
		{
			MutableComponent potionName = Component.translatable(effect.getDescriptionId());

			if(amplifier > 0)
				potionName = Component.translatable("potion.withAmplifier", potionName, Component.translatable("potion.potency." + amplifier));

			if(duration > 20)
			{
				int i = Mth.floor((float) duration);
				String durationFormat = StringUtil.formatTickDuration(i);
				potionName = Component.translatable("potion.withDuration", potionName, durationFormat);
			}

			tooltip.add(potionName.withStyle(effect.getCategory().getTooltipFormatting()));

			Map<Attribute, AttributeModifier> attributeModifiers = effect.getAttributeModifiers();

			if(!attributeModifiers.isEmpty())
			{
				tooltip.add(Component.empty());
				tooltip.add(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

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
						tooltip.add(Component.translatable("attribute.modifier.plus." + operation.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(attribute.getDescriptionId())).withStyle(ChatFormatting.BLUE));
					else if(d0 < 0D)
					{
						d1 = d1 * -1D;
						tooltip.add(Component.translatable("attribute.modifier.take." + operation.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(attribute.getDescriptionId())).withStyle(ChatFormatting.BLUE));
					}
				});
			}
		}
	}

	public static void appendPotionEffectTooltips(ItemStack stack, List<Component> tooltip)
	{
		var stackTag = stack.getTag();

		if(stackTag != null && stackTag.contains(NBT_APEX, Tag.TAG_COMPOUND))
		{
			var apexTag = stackTag.getCompound(NBT_APEX);

			if(apexTag.contains(NBT_INFUSION_FLUID, Tag.TAG_COMPOUND))
			{
				var fluidTag = apexTag.getCompound(NBT_INFUSION_FLUID);

				var effectRegistryName = new ResourceLocation(fluidTag.getString(NBT_EFFECT));

				var effect = ForgeRegistries.MOB_EFFECTS.getValue(effectRegistryName);
				// var effectAmount = fluidTag.getInt(NBT_AMOUNT);
				var effectDuration = fluidTag.getInt(NBT_DURATION);
				var effectAmplifier = fluidTag.getInt(NBT_AMPLIFIER);

				appendPotionEffectTooltips(effect, effectAmplifier, effectDuration, tooltip);
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