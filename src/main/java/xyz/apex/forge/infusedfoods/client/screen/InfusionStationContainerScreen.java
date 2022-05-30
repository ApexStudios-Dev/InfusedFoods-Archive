package xyz.apex.forge.infusedfoods.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.*;

import xyz.apex.forge.infusedfoods.block.entity.InfusionStationInventory;
import xyz.apex.forge.infusedfoods.container.InfusionStationContainer;
import xyz.apex.forge.infusedfoods.init.IFElements;

import java.util.List;
import java.util.Map;

public final class InfusionStationContainerScreen extends ContainerScreen<InfusionStationContainer>
{
	public InfusionStationContainerScreen(InfusionStationContainer menu, PlayerInventory playerInventory, ITextComponent title)
	{
		super(menu, playerInventory, title);
	}

	@Override
	protected void init()
	{
		imageWidth = 176;
		imageHeight = 166;
		super.init();
		titleLabelX = imageWidth - font.width(title) - 8;
	}

	@Override
	public void render(MatrixStack pose, int mouseX, int mouseY, float partialTick)
	{
		renderBackground(pose);
		super.render(pose, mouseX, mouseY, partialTick);

		renderSlotBackground(pose, InfusionStationInventory.SLOT_BLAZE, 240, 0);
		renderSlotBackground(pose, InfusionStationInventory.SLOT_POTION, 240, 16);
		renderSlotBackground(pose, InfusionStationInventory.SLOT_FOOD, 240, 33);

		renderInfusionFluid(pose);
		renderInfusionProgress(pose);
		renderInfusionFluidTankOverlay(pose, mouseX, mouseY);

		renderTooltip(pose, mouseX, mouseY);
	}

	@Override
	protected void renderBg(MatrixStack pose, float partialTick, int mouseX, int mouseY)
	{
		RenderSystem.color4f(1F, 1F, 1F, 1F);
		getMinecraft().getTextureManager().bind(IFElements.INFUSION_STATION_CONTAINER_SCREEN_TEXTURE);
		int i = (width - imageWidth) / 2;
		int j = (height - imageHeight) / 2;
		blit(pose, i, j, 0, 0, imageWidth, imageHeight);
	}

	private void renderSlotBackground(MatrixStack pose, int slotIndex, int backgroundX, int backgroundY)
	{
		Slot slot = menu.getSlot(slotIndex);

		if(!slot.hasItem())
		{
			getMinecraft().getTextureManager().bind(IFElements.INFUSION_STATION_CONTAINER_SCREEN_TEXTURE);
			blit(pose, leftPos + slot.x, topPos + slot.y, backgroundX, backgroundY, 16, 16, 256, 256);
		}
	}

	private void renderInfusionFluidTankOverlay(MatrixStack pose, int mouseX, int mouseY)
	{
		int tankWidth = 16;
		int tankHeight = 40;
		int tankX = leftPos + 8;
		int tankY = topPos + 8;

		getMinecraft().getTextureManager().bind(IFElements.INFUSION_STATION_CONTAINER_SCREEN_TEXTURE);
		blit(pose, tankX, tankY, 176, 41, tankWidth, tankHeight, 256, 256);

		if(mouseX >= tankX && mouseY >= tankY && mouseX < tankX + tankWidth && mouseY < tankY + tankHeight)
		{
			fillGradient(pose, tankX, tankY, tankX + tankWidth, tankY + tankHeight, 0x80ffffff, 0x80ffffff);

			if(menu.itemHandler.hasInfusionFluid())
			{
				InfusionStationInventory.InfusionFluid infusionFluid = menu.itemHandler.getInfusionFluid();
				Effect effect = infusionFluid.getEffect();

				if(effect != null)
				{
					List<ITextComponent> tooltip = Lists.newArrayList();
					IFormattableTextComponent potionName = new TranslationTextComponent(effect.getDescriptionId());

					int amplifier = infusionFluid.getAmplifier();
					int duration = infusionFluid.getDuration();

					if(amplifier > 0)
						potionName = new TranslationTextComponent("potion.withAmplifier", potionName, new TranslationTextComponent("potion.potency." + amplifier));
					if(duration > 20)
					{
						int i = MathHelper.floor(duration * 1F);
						String durationFormat = StringUtils.formatTickDuration(i);
						potionName = new TranslationTextComponent("potion.withDuration", potionName, durationFormat);
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

					renderWrappedToolTip(pose, tooltip, mouseX, mouseY, font);
				}
			}
		}
	}

	private void renderInfusionFluid(MatrixStack pose)
	{
		if(menu.itemHandler.hasInfusionFluid())
		{
			int tankWidth = 16;
			int tankHeight = 40;
			int tankX = leftPos + 8;
			int tankY = topPos + tankHeight;

			int fluidWidth = 16;
			int fluidHeight = 8;

			InfusionStationInventory.InfusionFluid infusionFluid = menu.itemHandler.getInfusionFluid();
			int fluidAmount = infusionFluid.getAmount();
			int color = infusionFluid.getColor();

			getMinecraft().getTextureManager().bind(IFElements.INFUSION_STATION_CONTAINER_SCREEN_TEXTURE);

			// float a = (float) (color >> 24 & 255) / 255F;
			float r = (float) (color >> 16 & 255) / 255F;
			float g = (float) (color >> 8 & 255) / 255F;
			float b = (float) (color & 255) / 255F;

			RenderSystem.color4f(r, g, b, 1F);

			for(int i = 0; i < fluidAmount; i++)
			{
				blit(pose, tankX, tankY - (i * fluidHeight), 176, 33, fluidWidth, fluidHeight, 256, 256);
			}

			RenderSystem.color4f(1F, 1F, 1F, 1F);
		}
	}

	private void renderInfusionProgress(MatrixStack pose)
	{
		getMinecraft().getTextureManager().bind(IFElements.INFUSION_STATION_CONTAINER_SCREEN_TEXTURE);

		int blazeFuel = menu.getBlazeFuel();
		int infuseTime = menu.getInfuseTime();

		int blazeWidth = MathHelper.clamp((18 * blazeFuel + 20 - 1) / 20, 0, 18);

		if(blazeWidth > 0)
			blit(pose, leftPos + 89, topPos + 72, 176, 29, blazeWidth, 4, 256, 256);

		// infuseTime = 200;

		if(infuseTime > 0)
		{
			int j1 = (int) (28F * (1F - (float) infuseTime / 400F));

			if(j1 > 0)
			{
				blit(pose, leftPos + 106, topPos + 25, 189, 0, j1, 9, 256, 256);
				blit(pose, leftPos + 92, topPos + 42 + 29 - j1, 176, 29 - j1, 13, j1, 256, 256);
			}
		}
	}
}
