package xyz.apex.forge.infusedfoods.client.renderer.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public final class InfusionStationModel extends Model
{
	private final ModelRenderer infusionStation;
	private final ModelRenderer base;
	private final ModelRenderer blaze_rod;
	private final ModelRenderer potion;
	private final ModelRenderer potion_tint;
	private final ModelRenderer spout;
	private final ModelRenderer apple;
	private final ModelRenderer appleLeaf;

	private int potionColor = 0x385dc6;

	public InfusionStationModel(Function<ResourceLocation, RenderType> renderTypeFunction)
	{
		super(renderTypeFunction);

		texWidth = 64;
		texHeight = 64;

		infusionStation = new ModelRenderer(this);
		infusionStation.setPos(0F, 16F, 0F);
		
		base = new ModelRenderer(this);
		base.setPos(2F, 8F, -3F);
		infusionStation.addChild(base);
		base.texOffs(0, 0).addBox(-7F, -1F, 0F, 12F, 1F, 6F, 0F, false);

		blaze_rod = new ModelRenderer(this);
		blaze_rod.setPos(-1F, 8F, -3F);
		infusionStation.addChild(blaze_rod);
		blaze_rod.texOffs(14, 13).addBox(-6F, -14F, 2F, 2F, 14F, 2F, 0F, false);

		potion = new ModelRenderer(this);
		potion.setPos(-1.5F, 3.5F, 0F);
		infusionStation.addChild(potion);
		potion.texOffs(0, 22).addBox(-2.5F, -3.5F, 0F, 5F, 7F, 0F, 0F, false);
		potion.texOffs(0, 10).addBox(0F, -3.5F, -2.5F, 0F, 7F, 5F, 0F, false);

		potion_tint = new ModelRenderer(this);
		potion_tint.setPos(-1.5F, 3.5F, 0F);
		infusionStation.addChild(potion_tint);
		potion_tint.texOffs(0, 22).addBox(-2.5F, -3.5F, 0F, 5F, 7F, 0F, 0F, false);
		potion_tint.texOffs(0, 10).addBox(0F, -3.5F, -2.5F, 0F, 7F, 5F, 0F, false);

		spout = new ModelRenderer(this);
		spout.setPos(1F, 0F, 0F);
		infusionStation.addChild(spout);
		spout.texOffs(16, 7).addBox(-6F, -6F, 0F, 6F, 6F, 0F, 0F, false);
		spout.texOffs(0, 29).addBox(0F, -6F, 0F, 6F, 8F, 0F, 0F, false);

		apple = new ModelRenderer(this);
		apple.setPos(4.8543F, 2.6472F, 0F);
		infusionStation.addChild(apple);
		apple.texOffs(0, 7).addBox(-2.8543F, .3528F, -2F, 4F, 4F, 4F, 0F, false);
		apple.texOffs(0, 0).addBox(-1.3543F, -.6472F, -.5F, 1F, 1F, 1F, 0F, false);

		appleLeaf = new ModelRenderer(this);
		appleLeaf.setPos(0F, 0F, 0F);
		apple.addChild(appleLeaf);
		setRotationAngle(appleLeaf, 0F, 0F, .7854F);
		appleLeaf.texOffs(0, 1).addBox(0F, -1.5F, -.5F, 0F, 2F, 1F, 0F, false);
	}

	public void setUpForRender(boolean hasPotion, boolean hasBottle, boolean hasFluid, boolean hasFood, int potionColor)
	{
		this.potionColor = potionColor;

		infusionStation.visible = true;
		base.visible = true;
		blaze_rod.visible = true;
		spout.visible = true;
		potion.visible = hasPotion || hasBottle || hasFluid;
		apple.visible = hasFood;
		appleLeaf.visible = apple.visible;

		potion_tint.copyFrom(potion);
		potion_tint.visible = false;
	}

	@Override
	public void renderToBuffer(MatrixStack pose, IVertexBuilder builder, int packedLight, int packedOverlay, float r, float g, float b, float a)
	{
		renderToBufferTint(pose, builder, packedLight, packedOverlay, r, g, b, a, false);
	}

	public void renderToBufferTint(MatrixStack pose, IVertexBuilder builder, int packedLight, int packedOverlay, float r, float g, float b, float a, boolean tint)
	{
		if(tint)
		{
			if(potion.visible)
			{
				// float pAlpha = (float) (potionColor >> 24 & 255) / 255F;
				float pRed = (float) (potionColor >> 16 & 255) / 255F;
				float pGreen = (float) (potionColor >> 8 & 255) / 255F;
				float pBlue = (float) (potionColor & 255) / 255F;

				potion_tint.visible = true;
				potion_tint.render(pose, builder, packedLight, packedOverlay, pRed, pGreen, pBlue, a);
			}
		}
		else
			infusionStation.render(pose, builder, packedLight, packedOverlay, r, g, b, a);
	}

	private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
	{
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}
