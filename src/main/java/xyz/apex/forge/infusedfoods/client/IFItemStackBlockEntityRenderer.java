package xyz.apex.forge.infusedfoods.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Lazy;

import xyz.apex.forge.infusedfoods.block.entity.InfusionStationBlockEntity;
import xyz.apex.forge.infusedfoods.client.renderer.InfusionStationBlockEntityRenderer;
import xyz.apex.forge.infusedfoods.init.IFElements;

import static xyz.apex.forge.apexcore.lib.block.entity.BaseBlockEntity.NBT_APEX;

public final class IFItemStackBlockEntityRenderer extends BlockEntityWithoutLevelRenderer
{
	private static final Lazy<BlockEntityWithoutLevelRenderer> INSTANCE = Lazy.of(() -> {
		var mc = Minecraft.getInstance();
		return new IFItemStackBlockEntityRenderer(new BlockEntityRendererProvider.Context(mc.getBlockEntityRenderDispatcher(), mc.getBlockRenderer(), mc.getItemRenderer(), mc.getEntityRenderDispatcher(), mc.getEntityModels(), mc.font));
	});

	private final InfusionStationBlockEntityRenderer infusionStationBlockEntityRenderer;
	private final Lazy<InfusionStationBlockEntity> infusionStationBlockEntity = Lazy.of(() -> {
		var blockState = IFElements.INFUSION_STATION_BLOCK.getDefaultState();
		return IFElements.INFUSION_STATION_BLOCK_ENTITY.create(BlockPos.ZERO, blockState);
	});

	private IFItemStackBlockEntityRenderer(BlockEntityRendererProvider.Context ctx)
	{
		super(ctx.getBlockEntityRenderDispatcher(), ctx.getModelSet());

		infusionStationBlockEntityRenderer = new InfusionStationBlockEntityRenderer(ctx);
	}

	@Override
	public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack pose, MultiBufferSource buffer, int light, int overlay)
	{
		var partialTick = Minecraft.getInstance().getDeltaFrameTime();

		if(IFElements.INFUSION_STATION_BLOCK_ITEM.isIn(stack))
		{
			var stackTag = stack.getOrCreateTagElement(NBT_APEX);
			var blockEntity = this.infusionStationBlockEntity.get();
			blockEntity.deserializeData(stackTag);
			infusionStationBlockEntityRenderer.renderForGUI(stack, blockEntity, partialTick, pose, buffer, light, overlay, transformType);
		}
		else
			super.renderByItem(stack, transformType, pose, buffer, light, overlay);
	}

	public static BlockEntityWithoutLevelRenderer getInstance()
	{
		return INSTANCE.get();
	}
}