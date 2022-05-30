package xyz.apex.forge.infusedfoods.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;

import xyz.apex.forge.infusedfoods.block.InfusionStationBlock;
import xyz.apex.forge.infusedfoods.block.entity.InfusionStationBlockEntity;
import xyz.apex.forge.infusedfoods.block.entity.InfusionStationInventory;
import xyz.apex.forge.infusedfoods.client.renderer.model.InfusionStationModel;
import xyz.apex.forge.infusedfoods.init.IFElements;

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
		InfusionStationInventory inventory = blockEntity.getItemHandler();
		boolean hasBottle = !inventory.getBottle().isEmpty();
		boolean hasPotion = !inventory.getPotion().isEmpty();
		boolean hasFluid = inventory.hasInfusionFluid();
		boolean hasFood = !inventory.getFood().isEmpty();

		int color = inventory.getInfusionFluid().getColor();

		model.setUpForRender(hasPotion, hasBottle, hasFluid, hasFood, color);
		pose.pushPose();

		BlockState blockState = blockEntity.getBlockState();
		Direction facing = blockState.getValue(InfusionStationBlock.FACING);

		pose.translate(.5D, .5D, .5D);
		pose.mulPose(Vector3f.YP.rotationDegrees(-facing.toYRot()));
		pose.mulPose(Vector3f.XP.rotationDegrees(180F));
		pose.translate(0D, -1D, 0D);

		RenderType renderType = RenderType.entityTranslucentCull(IFElements.INFUSION_STATION_BLOCK_TEXTURE);
		IVertexBuilder modelBuffer = buffer.getBuffer(renderType);
		model.renderToBuffer(pose, modelBuffer, combinedLight, combinedOverlay, 1F, 1F, 1F, 1F);

		pose.pushPose();
		pose.translate(0D, 1D, 0D);

		renderType = RenderType.entityTranslucentCull(IFElements.INFUSION_STATION_BLOCK_TEXTURE_TINT);
		modelBuffer = buffer.getBuffer(renderType);
		model.renderToBufferTint(pose, modelBuffer, combinedLight, combinedOverlay, 1F, 1F, 1F, 1F, true);

		pose.popPose();

		pose.popPose();
	}

	public void renderForGUI(InfusionStationBlockEntity blockEntity, float partialTick, MatrixStack pose, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, ItemCameraTransforms.TransformType transformType)
	{
		if(transformType == ItemCameraTransforms.TransformType.NONE)
			return;

		model.setUpForRender(true, true, true, true, 0x720F0F);
		pose.pushPose();

		if(transformType == ItemCameraTransforms.TransformType.GUI)
		{
			pose.mulPose(Vector3f.XP.rotationDegrees(30F));
			pose.mulPose(Vector3f.YP.rotationDegrees(225F));
			pose.mulPose(Vector3f.ZP.rotationDegrees(180F));
			pose.translate(.55D, -1.5D, -.15D);
			pose.scale(.85F, .95F, .9F);
		}
		else if(transformType == ItemCameraTransforms.TransformType.HEAD)
		{
			pose.mulPose(Vector3f.ZP.rotationDegrees(180F));
			pose.translate(-.5D, -2.45D, .5D);
		}
		else if(transformType == ItemCameraTransforms.TransformType.GROUND)
		{
			pose.mulPose(Vector3f.ZP.rotationDegrees(180F));
			pose.translate(-.5D, -1D, .5D);
			pose.scale(.45F, .45F, .45F);
		}
		else if(transformType == ItemCameraTransforms.TransformType.FIXED)
		{
			pose.mulPose(Vector3f.ZP.rotationDegrees(180F));
			pose.translate(-.5D, -1.65D, .5D);
		}
		else if(transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND)
		{
			pose.mulPose(Vector3f.XP.rotationDegrees(180F));
			pose.translate(.25D, -1D, -.25D);
			pose.scale(.45F, .45F, .45F);
			pose.mulPose(Vector3f.YN.rotationDegrees(25F));
		}
		else if(transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND)
		{
			pose.mulPose(Vector3f.ZP.rotationDegrees(180F));
			pose.translate(-.6D, -1D, .25D);
			pose.scale(.45F, .45F, .45F);
			pose.mulPose(Vector3f.YP.rotationDegrees(25F));
		}
		else if(transformType == ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND)
		{
			pose.mulPose(Vector3f.XP.rotationDegrees(180F));
			pose.translate(.5D, -1D, -.4D);
			pose.scale(.45F, .45F, .45F);
		}
		else if(transformType == ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND)
		{
			pose.mulPose(Vector3f.ZP.rotationDegrees(180F));
			pose.translate(-.5D, -1D, .4D);
			pose.scale(.45F, .45F, .45F);
		}

		RenderType renderType = RenderType.entityTranslucentCull(IFElements.INFUSION_STATION_BLOCK_TEXTURE);
		IVertexBuilder modelBuffer = buffer.getBuffer(renderType);
		model.renderToBuffer(pose, modelBuffer, combinedLight, combinedOverlay, 1F, 1F, 1F, 1F);

		pose.pushPose();
		pose.translate(0D, 1D, 0D);

		renderType = RenderType.entityTranslucentCull(IFElements.INFUSION_STATION_BLOCK_TEXTURE_TINT);
		modelBuffer = buffer.getBuffer(renderType);
		model.renderToBufferTint(pose, modelBuffer, combinedLight, combinedOverlay, 1F, 1F, 1F, 1F, true);

		pose.popPose();

		pose.popPose();
	}
}
