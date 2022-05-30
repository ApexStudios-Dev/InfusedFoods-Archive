package xyz.apex.forge.infusedfoods.client;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.Lazy;

import xyz.apex.forge.infusedfoods.block.entity.InfusionStationBlockEntity;
import xyz.apex.forge.infusedfoods.client.renderer.InfusionStationBlockEntityRenderer;
import xyz.apex.forge.infusedfoods.init.IFElements;

public final class IFItemStackBlockEntityRenderer extends ItemStackTileEntityRenderer
{
	private final InfusionStationBlockEntityRenderer infusionStationBlockEntityRenderer;
	private final Lazy<InfusionStationBlockEntity> infusionStationBlockEntity = Lazy.of(IFElements.INFUSION_STATION_BLOCK_ENTITY::createBlockEntity);

	public IFItemStackBlockEntityRenderer()
	{
		super();

		infusionStationBlockEntityRenderer = new InfusionStationBlockEntityRenderer(TileEntityRendererDispatcher.instance);
	}

	@Override
	public void renderByItem(ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack pose, IRenderTypeBuffer buffer, int light, int overlay)
	{
		float partialTick = Minecraft.getInstance().getDeltaFrameTime();

		if(IFElements.INFUSION_STATION_BLOCK_ITEM.isInStack(stack))
		{
			InfusionStationBlockEntity blockEntity = this.infusionStationBlockEntity.get();
			infusionStationBlockEntityRenderer.renderForGUI(blockEntity, partialTick, pose, buffer, light, overlay, transformType);
		}
		else
			super.renderByItem(stack, transformType, pose, buffer, light, overlay);
	}
}
