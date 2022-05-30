package xyz.apex.forge.potionfoods.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.vector.Vector3f;

import xyz.apex.forge.potionfoods.block.entity.InfusionStationBlockEntity;
import xyz.apex.forge.potionfoods.client.renderer.model.InfusionStationModel;
import xyz.apex.forge.potionfoods.init.PFElements;

public final class InfusionStationBlockEntityRenderer extends TileEntityRenderer<InfusionStationBlockEntity>
{
	private final InfusionStationModel model;

	public InfusionStationBlockEntityRenderer(TileEntityRendererDispatcher rendererDispatcher)
	{
		super(rendererDispatcher);

		model = new InfusionStationModel(RenderType::entityTranslucent);
	}

	@Override
	public void render(InfusionStationBlockEntity blockEntity, float partialTick, MatrixStack pose, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay)
	{
		RenderType renderType = model.renderType(PFElements.INFUSION_STATION_BLOCK_TEXTURE);
		IVertexBuilder modelBuffer = buffer.getBuffer(renderType);

		pose.pushPose();

		if(blockEntity.hasLevel())
		{
			BlockState blockState = blockEntity.getBlockState();
			// Direction facing = blockState.getValue(WidowBloomBlock.FACING);

			pose.translate(.5D, .5D, .5D);
			// pose.mulPose(Vector3f.YP.rotationDegrees(-facing.toYRot()));
			pose.mulPose(Vector3f.XP.rotationDegrees(180F));
			pose.translate(0D, -1D, 0D);
		}
		else
		{
			pose.translate(.5D, .5D, .5D);
			pose.mulPose(Vector3f.ZP.rotationDegrees(180F));
		}

		model.renderToBuffer(pose, modelBuffer, combinedLight, combinedOverlay, 1F, 1F, 1F, 1F);
		pose.popPose();
	}
}
