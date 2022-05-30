package xyz.apex.forge.infusedfoods;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import xyz.apex.forge.apexcore.lib.net.NetworkManager;
import xyz.apex.forge.apexcore.lib.util.EventBusHelper;
import xyz.apex.forge.infusedfoods.command.CommandPotionFood;
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

		EventBusHelper.addListener(RegisterCommandsEvent.class, event -> CommandPotionFood.register(event.getDispatcher()));
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