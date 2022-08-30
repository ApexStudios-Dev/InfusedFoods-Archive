package xyz.apex.forge.infusedfoods.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import xyz.apex.forge.apexcore.lib.client.screen.BaseMenuScreen;
import xyz.apex.forge.commonality.SideOnly;
import xyz.apex.forge.infusedfoods.InfusedFoods;
import xyz.apex.forge.infusedfoods.block.entity.InfusionStationBlockEntity;
import xyz.apex.forge.infusedfoods.container.InfusionStationMenu;
import xyz.apex.forge.infusedfoods.init.IFElements;

import java.util.Optional;

@SideOnly(SideOnly.Side.CLIENT)
public final class InfusionStationMenuScreen extends BaseMenuScreen<InfusionStationMenu>
{
	public InfusionStationMenuScreen(InfusionStationMenu menu, Inventory playerInventory, Component title)
	{
		super(menu, playerInventory, title, IFElements.INFUSION_STATION_CONTAINER_SCREEN_TEXTURE);
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
	public void renderFg(PoseStack pose, float partialTick, int mouseX, int mouseY)
	{
		renderSlotBackground(pose, InfusionStationBlockEntity.SLOT_BLAZE, 240, 0);
		renderSlotBackground(pose, InfusionStationBlockEntity.SLOT_POTION, 240, 16);
		renderSlotBackground(pose, InfusionStationBlockEntity.SLOT_FOOD, 240, 33);

		renderInfusionFluid(pose);
		renderInfusionProgress(pose);
		renderInfusionFluidTankOverlay(pose, mouseX, mouseY);
	}

	private void renderSlotBackground(PoseStack pose, int slotIndex, int backgroundX, int backgroundY)
	{
		var slot = menu.getSlot(slotIndex);

		if(!slot.hasItem())
		{
			RenderSystem.setShaderTexture(0, IFElements.INFUSION_STATION_CONTAINER_SCREEN_TEXTURE);
			blit(pose, leftPos + slot.x, topPos + slot.y, backgroundX, backgroundY, 16, 16, 256, 256);
		}
	}

	private void renderInfusionFluidTankOverlay(PoseStack pose, int mouseX, int mouseY)
	{
		var tankWidth = 16;
		var tankHeight = 40;
		var tankX = leftPos + 8;
		var tankY = topPos + 8;

		RenderSystem.setShaderTexture(0, IFElements.INFUSION_STATION_CONTAINER_SCREEN_TEXTURE);
		blit(pose, tankX, tankY, 176, 41, tankWidth, tankHeight, 256, 256);

		if(mouseX >= tankX && mouseY >= tankY && mouseX < tankX + tankWidth && mouseY < tankY + tankHeight)
		{
			fillGradient(pose, tankX, tankY, tankX + tankWidth, tankY + tankHeight, 0x80ffffff, 0x80ffffff);

			var effect = menu.getEffect();

			if(effect != null)
			{
				var tooltip = Lists.<Component>newArrayList();
				InfusedFoods.appendPotionEffectTooltips(effect, menu.getEffectAmplifier(), menu.getEffectDuration(), tooltip);
				renderTooltip(pose, tooltip, Optional.empty(), mouseX, mouseY);
			}
		}
	}

	private void renderInfusionFluid(PoseStack pose)
	{
		var effect = menu.getEffect();

		if(effect != null)
		{
			var tankWidth = 16;
			var tankHeight = 40;
			var tankX = leftPos + 8;
			var tankY = topPos + tankHeight;

			var fluidWidth = 16;
			var fluidHeight = 8;

			var fluidAmount = menu.getEffectAmount();
			var color = InfusionStationBlockEntity.getColor(effect, menu.getEffectAmplifier());

			RenderSystem.setShaderTexture(0, IFElements.INFUSION_STATION_CONTAINER_SCREEN_TEXTURE);

			// var a = (float) (color >> 24 & 255) / 255F;
			var r = (float) (color >> 16 & 255) / 255F;
			var g = (float) (color >> 8 & 255) / 255F;
			var b = (float) (color & 255) / 255F;

			RenderSystem.setShaderColor(r, g, b, 1F);

			for(var i = 0; i < fluidAmount; i++)
			{
				blit(pose, tankX, tankY - (i * fluidHeight), 176, 33, fluidWidth, fluidHeight, 256, 256);
			}

			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		}
	}

	private void renderInfusionProgress(PoseStack pose)
	{
		RenderSystem.setShaderTexture(0, IFElements.INFUSION_STATION_CONTAINER_SCREEN_TEXTURE);

		var blazeFuel = menu.getBlazeFuel();
		var infuseTime = menu.getInfuseTime();

		var blazeWidth = Mth.clamp((18 * blazeFuel + 20 - 1) / 20, 0, 18);

		if(blazeWidth > 0)
			blit(pose, leftPos + 89, topPos + 72, 176, 29, blazeWidth, 4, 256, 256);

		// infuseTime = 200;

		if(infuseTime > 0)
		{
			var j1 = (int) (28F * (1F - (float) infuseTime / 400F));

			if(j1 > 0)
			{
				blit(pose, leftPos + 106, topPos + 25, 189, 0, j1, 9, 256, 256);
				blit(pose, leftPos + 92, topPos + 42 + 29 - j1, 176, 29 - j1, 13, j1, 256, 256);
			}
		}
	}
}