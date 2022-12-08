package xyz.apex.forge.infusedfoods.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;

import xyz.apex.forge.commonality.SideOnly;
import xyz.apex.forge.infusedfoods.block.InfusionStationBlock;
import xyz.apex.forge.infusedfoods.block.entity.InfusionStationBlockEntity;
import xyz.apex.forge.infusedfoods.client.renderer.model.InfusionStationModel;
import xyz.apex.forge.infusedfoods.init.IFElements;

@SideOnly(SideOnly.Side.CLIENT)
public final class InfusionStationBlockEntityRenderer implements BlockEntityRenderer<InfusionStationBlockEntity>
{
	private final InfusionStationModel model;

	public InfusionStationBlockEntityRenderer(BlockEntityRendererProvider.Context ctx)
	{
		model = new InfusionStationModel(ctx);
	}

	@Override
	public void render(InfusionStationBlockEntity blockEntity, float partialTick, PoseStack pose, MultiBufferSource buffer, int combinedLight, int combinedOverlay)
	{
		var inventory = blockEntity.getItemHandler();
		var effect = blockEntity.getEffect();
		var effectAmount = blockEntity.getEffectAmount();

		var hasBottle = !inventory.getStackInSlot(InfusionStationBlockEntity.SLOT_BOTTLE).isEmpty();
		var hasPotion = !inventory.getStackInSlot(InfusionStationBlockEntity.SLOT_POTION).isEmpty();
		var hasFluid = effect != null && effectAmount > 0;
		var hasFood = !inventory.getStackInSlot(InfusionStationBlockEntity.SLOT_FOOD).isEmpty();

		var color = InfusionStationBlockEntity.getColor(effect, blockEntity.getEffectAmplifier());

		model.setUpForRender(hasPotion, hasBottle, hasFluid, hasFood, color);
		pose.pushPose();

		var blockState = blockEntity.getBlockState();
		var facing = blockState.getValue(InfusionStationBlock.FACING_4_WAY);

		pose.translate(.5D, .5D, .5D);
		pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
		pose.mulPose(Axis.XP.rotationDegrees(180F));
		pose.translate(0D, -1D, 0D);

		var renderType = RenderType.entityTranslucentCull(IFElements.INFUSION_STATION_BLOCK_TEXTURE);
		var modelBuffer = buffer.getBuffer(renderType);
		model.renderToBuffer(pose, modelBuffer, combinedLight, combinedOverlay, 1F, 1F, 1F, 1F);

		pose.pushPose();
		pose.translate(0D, 1D, 0D);

		renderType = RenderType.entityTranslucentCull(IFElements.INFUSION_STATION_BLOCK_TEXTURE_TINT);
		modelBuffer = buffer.getBuffer(renderType);
		model.renderToBufferTint(pose, modelBuffer, combinedLight, combinedOverlay, 1F, 1F, 1F, 1F, true);

		pose.popPose();

		pose.popPose();
	}

	public void renderForGUI(ItemStack stack, InfusionStationBlockEntity blockEntity, float partialTick, PoseStack pose, MultiBufferSource buffer, int combinedLight, int combinedOverlay, ItemTransforms.TransformType transformType)
	{
		if(transformType == ItemTransforms.TransformType.NONE)
			return;

		var hasPotion = false;
		var potionColor = 0x720F0F;

		var effect = blockEntity.getEffect();
		var effectAmount = blockEntity.getEffectAmount();

		if(effect != null && effectAmount > 0)
		{
			hasPotion = true;
			potionColor = InfusionStationBlockEntity.getColor(effect, blockEntity.getEffectAmplifier());
		}

		model.setUpForRender(hasPotion, hasPotion, hasPotion, true, potionColor);
		pose.pushPose();

		if(transformType == ItemTransforms.TransformType.GUI)
		{
			pose.mulPose(Axis.XP.rotationDegrees(30F));
			pose.mulPose(Axis.YP.rotationDegrees(225F));
			pose.mulPose(Axis.ZP.rotationDegrees(180F));
			pose.translate(.55D, -1.5D, -.15D);
			pose.scale(.85F, .95F, .9F);
		}
		else if(transformType == ItemTransforms.TransformType.HEAD)
		{
			pose.mulPose(Axis.ZP.rotationDegrees(180F));
			pose.translate(-.5D, -2.45D, .5D);
		}
		else if(transformType == ItemTransforms.TransformType.GROUND)
		{
			pose.mulPose(Axis.ZP.rotationDegrees(180F));
			pose.translate(-.5D, -1D, .5D);
			pose.scale(.45F, .45F, .45F);
		}
		else if(transformType == ItemTransforms.TransformType.FIXED)
		{
			pose.mulPose(Axis.ZP.rotationDegrees(180F));
			pose.translate(-.5D, -1.65D, .5D);
		}
		else if(transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND)
		{
			pose.mulPose(Axis.XP.rotationDegrees(180F));
			pose.translate(.25D, -1D, -.25D);
			pose.scale(.45F, .45F, .45F);
			pose.mulPose(Axis.YN.rotationDegrees(25F));
		}
		else if(transformType == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND)
		{
			pose.mulPose(Axis.ZP.rotationDegrees(180F));
			pose.translate(-.6D, -1D, .25D);
			pose.scale(.45F, .45F, .45F);
			pose.mulPose(Axis.YP.rotationDegrees(25F));
		}
		else if(transformType == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND)
		{
			pose.mulPose(Axis.XP.rotationDegrees(180F));
			pose.translate(.5D, -1D, -.4D);
			pose.scale(.45F, .45F, .45F);
		}
		else if(transformType == ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND)
		{
			pose.mulPose(Axis.ZP.rotationDegrees(180F));
			pose.translate(-.5D, -1D, .4D);
			pose.scale(.45F, .45F, .45F);
		}

		var renderType = RenderType.entityTranslucentCull(IFElements.INFUSION_STATION_BLOCK_TEXTURE);
		var modelBuffer = buffer.getBuffer(renderType);
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
