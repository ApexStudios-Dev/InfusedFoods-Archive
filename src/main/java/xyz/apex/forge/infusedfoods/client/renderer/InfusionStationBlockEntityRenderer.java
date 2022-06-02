package xyz.apex.forge.infusedfoods.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import xyz.apex.forge.infusedfoods.block.InfusionStationBlock;
import xyz.apex.forge.infusedfoods.block.entity.InfusionStationBlockEntity;
import xyz.apex.forge.infusedfoods.block.entity.InfusionStationInventory;
import xyz.apex.forge.infusedfoods.client.renderer.model.InfusionStationModel;
import xyz.apex.forge.infusedfoods.init.IFElements;

public final class InfusionStationBlockEntityRenderer implements BlockEntityRenderer<InfusionStationBlockEntity>
{
	private final InfusionStationModel model;

	public InfusionStationBlockEntityRenderer(BlockEntityRendererProvider.Context ctx)
	{
		model = new InfusionStationModel(ctx);
	}

	private void renderModel(PoseStack pose, MultiBufferSource buffer, int combinedLight, int combinedOverlay)
	{
		var renderType = model.renderType(IFElements.INFUSION_STATION_BLOCK_TEXTURE);
		var modelBuffer = buffer.getBuffer(renderType);
		model.renderToBuffer(pose, modelBuffer, combinedLight, combinedOverlay, 1F, 1F, 1F, 1F);

		pose.pushPose();
		pose.translate(0D, 1D, 0D);

		renderType = model.renderType(IFElements.INFUSION_STATION_BLOCK_TEXTURE_TINT);
		modelBuffer = buffer.getBuffer(renderType);
		model.renderToBufferTint(pose, modelBuffer, combinedLight, combinedOverlay, 1F, 1F, 1F, 1F, true);

		pose.popPose();

		pose.popPose();
	}

	@Override
	public void render(InfusionStationBlockEntity blockEntity, float partialTick, PoseStack pose, MultiBufferSource buffer, int combinedLight, int combinedOverlay)
	{
		var inventory = blockEntity.getItemHandler();
		var hasBottle = !inventory.getBottle().isEmpty();
		var hasPotion = !inventory.getPotion().isEmpty();
		var hasFluid = inventory.hasInfusionFluid();
		var hasFood = !inventory.getFood().isEmpty();

		var color = inventory.getInfusionFluid().getColor();

		model.setUpForRender(hasPotion, hasBottle, hasFluid, hasFood, color);
		pose.pushPose();

		var blockState = blockEntity.getBlockState();
		var facing = blockState.getValue(InfusionStationBlock.FACING);

		pose.translate(.5D, .5D, .5D);
		pose.mulPose(Vector3f.YP.rotationDegrees(-facing.toYRot()));
		pose.mulPose(Vector3f.XP.rotationDegrees(180F));
		pose.translate(0D, -1D, 0D);

		renderModel(pose, buffer, combinedLight, combinedOverlay);
	}

	public void renderForGUI(ItemStack stack, InfusionStationBlockEntity blockEntity, float partialTick, PoseStack pose, MultiBufferSource buffer, int combinedLight, int combinedOverlay, ItemTransforms.TransformType transformType)
	{
		if(transformType == ItemTransforms.TransformType.NONE)
			return;

		var hasPotion = false;
		var potionColor = 0x720F0F;

		var stackTag = stack.getTag();

		if(stackTag != null && stackTag.contains(InfusionStationBlockEntity.NBT_INVENTORY, Tag.TAG_COMPOUND))
		{
			var inventoryTag = stackTag.getCompound(InfusionStationBlockEntity.NBT_INVENTORY);

			if(inventoryTag.contains(InfusionStationInventory.NBT_INFUSION_FLUID, Tag.TAG_COMPOUND))
			{
				var fluidTag = inventoryTag.getCompound(InfusionStationInventory.NBT_INFUSION_FLUID);
				var fluid = new InfusionStationInventory.InfusionFluid(fluidTag);
				hasPotion = !fluid.isEmpty();

				if(hasPotion)
					potionColor = fluid.getColor();
			}
		}

		model.setUpForRender(hasPotion, hasPotion, hasPotion, true, potionColor);
		pose.pushPose();

		if(transformType == ItemTransforms.TransformType.GUI)
		{
			pose.mulPose(Vector3f.XP.rotationDegrees(30F));
			pose.mulPose(Vector3f.YP.rotationDegrees(225F));
			pose.mulPose(Vector3f.ZP.rotationDegrees(180F));
			pose.translate(.55D, -1.5D, -.15D);
			pose.scale(.85F, .95F, .9F);
		}
		else if(transformType == ItemTransforms.TransformType.HEAD)
		{
			pose.mulPose(Vector3f.ZP.rotationDegrees(180F));
			pose.translate(-.5D, -2.45D, .5D);
		}
		else if(transformType == ItemTransforms.TransformType.GROUND)
		{
			pose.mulPose(Vector3f.ZP.rotationDegrees(180F));
			pose.translate(-.5D, -1D, .5D);
			pose.scale(.45F, .45F, .45F);
		}
		else if(transformType == ItemTransforms.TransformType.FIXED)
		{
			pose.mulPose(Vector3f.ZP.rotationDegrees(180F));
			pose.translate(-.5D, -1.65D, .5D);
		}
		else if(transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND)
		{
			pose.mulPose(Vector3f.XP.rotationDegrees(180F));
			pose.translate(.25D, -1D, -.25D);
			pose.scale(.45F, .45F, .45F);
			pose.mulPose(Vector3f.YN.rotationDegrees(25F));
		}
		else if(transformType == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND)
		{
			pose.mulPose(Vector3f.ZP.rotationDegrees(180F));
			pose.translate(-.6D, -1D, .25D);
			pose.scale(.45F, .45F, .45F);
			pose.mulPose(Vector3f.YP.rotationDegrees(25F));
		}
		else if(transformType == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND)
		{
			pose.mulPose(Vector3f.XP.rotationDegrees(180F));
			pose.translate(.5D, -1D, -.4D);
			pose.scale(.45F, .45F, .45F);
		}
		else if(transformType == ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND)
		{
			pose.mulPose(Vector3f.ZP.rotationDegrees(180F));
			pose.translate(-.5D, -1D, .4D);
			pose.scale(.45F, .45F, .45F);
		}

		renderModel(pose, buffer, combinedLight, combinedOverlay);
	}
}
