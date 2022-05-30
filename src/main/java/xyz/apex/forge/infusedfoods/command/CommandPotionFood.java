package xyz.apex.forge.infusedfoods.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.PotionArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.Hand;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.HoverEvent;

import xyz.apex.forge.infusedfoods.InfusedFoods;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public final class CommandPotionFood
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(literal(InfusedFoods.ID)
				.then(literal("get")
								.executes(CommandPotionFood::onGetEffects)
				).then(literal("set")
								.then(argument("effect", PotionArgument.effect())
												.then(argument("seconds", IntegerArgumentType.integer(1, 1000000))
																.then(argument("amplifier", IntegerArgumentType.integer(0, 255))
																		.then(argument("hideParticles", BoolArgumentType.bool())
																				.executes(CommandPotionFood::onSetEffects3)
																		)
																		.executes(CommandPotionFood::onSetEffects2)
																)
																.executes(CommandPotionFood::onSetEffects1)
												)
												.executes(CommandPotionFood::onSetEffects0)
								)
				)
		);
	}

	private static int onGetEffects(CommandContext<CommandSource> ctx) throws CommandSyntaxException
	{
		CommandSource source = ctx.getSource();
		ServerPlayerEntity player = source.getPlayerOrException();
		ItemStack stack = player.getMainHandItem();

		if(!InfusedFoods.isValidFood(stack))
			stack = player.getOffhandItem();

		if(!InfusedFoods.isValidFood(stack))
		{
			source.sendFailure(new StringTextComponent("You must be a holding an Edible item to use this command"));
			return SINGLE_SUCCESS;
		}

		List<EffectInstance> effects = PotionUtils.getCustomEffects(stack);

		if(effects.isEmpty())
			source.sendSuccess(new StringTextComponent("This Item has no special Effects"), false);
		else
		{
			IFormattableTextComponent component = new StringTextComponent("This Item has the following Effects: [ ");

			for(int i = 0; i < effects.size(); i++)
			{
				EffectInstance effect = effects.get(i);
				ItemStack potion = Items.POTION.getDefaultInstance();
				PotionUtils.setCustomEffects(potion, Collections.singleton(effect));

				component = component.append(
						new TranslationTextComponent(effect.getDescriptionId())
								.withStyle(style -> style
										.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemHover(potion)))
								)
				);

				if(i + 1 < effects.size())
					component = component.append(", ");
			}

			component = component.append(" ]");
			source.sendSuccess(component, true);
		}

		return SINGLE_SUCCESS;
	}

	private static int getEffectSeconds(Effect effect, @Nullable Integer seconds)
	{
		if(seconds != null)
		{
			if(effect.isInstantenous())
				return seconds;
			else
				return seconds * 20;
		}
		else if(effect.isInstantenous())
			return 1;
		else
			return 600;
	}

	private static int setEffects(CommandSource source, ItemStack stack, Hand hand, Effect effect, int seconds, int amplifier, boolean hideParticles)
	{
		List<EffectInstance> effects = PotionUtils.getCustomEffects(stack);

		ItemStack potion = Items.POTION.getDefaultInstance();
		EffectInstance effectInstance = new EffectInstance(effect, seconds, amplifier, false, !hideParticles);
		PotionUtils.setCustomEffects(potion, Collections.singletonList(effectInstance));

		if(!effects.isEmpty())
			effects.clear();

		effects.add(effectInstance);

		PotionUtils.setCustomEffects(stack, effects);
		source.sendSuccess(new StringTextComponent("Added Effect [ ").append(
				new TranslationTextComponent(effect.getDescriptionId())
						.withStyle(style -> style
								.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemHover(potion)))
						)
		).append(" ] to the Item in your " + hand.name()), true);

		return SINGLE_SUCCESS;
	}

	private static int onSetEffects0(CommandContext<CommandSource> ctx) throws CommandSyntaxException
	{
		CommandSource source = ctx.getSource();
		PlayerEntity player = source.getPlayerOrException();

		for(Hand hand : Hand.values())
		{
			ItemStack stack = player.getItemInHand(hand);

			if(InfusedFoods.isValidFood(stack))
			{
				Effect effect = PotionArgument.getEffect(ctx, "effect");
				int seconds = effect.isInstantenous() ? 1 : 600;

				return setEffects(source, stack, hand, effect, seconds, 0, false);
			}
		}

		source.sendFailure(new StringTextComponent("You must be a holding an Edible item to use this command"));
		return SINGLE_SUCCESS;
	}

	private static int onSetEffects1(CommandContext<CommandSource> ctx) throws CommandSyntaxException
	{
		CommandSource source = ctx.getSource();
		PlayerEntity player = source.getPlayerOrException();

		for(Hand hand : Hand.values())
		{
			ItemStack stack = player.getItemInHand(hand);

			if(InfusedFoods.isValidFood(stack))
			{
				Effect effect = PotionArgument.getEffect(ctx, "effect");
				Integer rawSeconds = IntegerArgumentType.getInteger(ctx, "seconds");
				int seconds = getEffectSeconds(effect, rawSeconds);

				return setEffects(source, stack, hand, effect, seconds, 0, false);
			}
		}

		source.sendFailure(new StringTextComponent("You must be a holding an Edible item to use this command"));
		return SINGLE_SUCCESS;
	}

	private static int onSetEffects2(CommandContext<CommandSource> ctx) throws CommandSyntaxException
	{
		CommandSource source = ctx.getSource();
		PlayerEntity player = source.getPlayerOrException();

		for(Hand hand : Hand.values())
		{
			ItemStack stack = player.getItemInHand(hand);

			if(InfusedFoods.isValidFood(stack))
			{
				Effect effect = PotionArgument.getEffect(ctx, "effect");
				Integer rawSeconds = IntegerArgumentType.getInteger(ctx, "seconds");
				int seconds = getEffectSeconds(effect, rawSeconds);
				int amplifier = IntegerArgumentType.getInteger(ctx, "amplifier");

				return setEffects(source, stack, hand, effect, seconds, amplifier, false);
			}
		}

		source.sendFailure(new StringTextComponent("You must be a holding an Edible item to use this command"));
		return SINGLE_SUCCESS;
	}

	private static int onSetEffects3(CommandContext<CommandSource> ctx) throws CommandSyntaxException
	{
		CommandSource source = ctx.getSource();
		PlayerEntity player = source.getPlayerOrException();

		for(Hand hand : Hand.values())
		{
			ItemStack stack = player.getItemInHand(hand);

			if(InfusedFoods.isValidFood(stack))
			{
				Effect effect = PotionArgument.getEffect(ctx, "effect");
				Integer rawSeconds = IntegerArgumentType.getInteger(ctx, "seconds");
				int seconds = getEffectSeconds(effect, rawSeconds);
				int amplifier = IntegerArgumentType.getInteger(ctx, "amplifier");
				boolean hideParticles = BoolArgumentType.getBool(ctx, "hideParticles");

				return setEffects(source, stack, hand, effect, seconds, amplifier, hideParticles);
			}
		}

		source.sendFailure(new StringTextComponent("You must be a holding an Edible item to use this command"));
		return SINGLE_SUCCESS;
	}
}
