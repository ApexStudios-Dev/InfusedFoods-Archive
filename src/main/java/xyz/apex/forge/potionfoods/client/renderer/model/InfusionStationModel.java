package xyz.apex.forge.potionfoods.client.renderer.model;

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

	public InfusionStationModel(Function<ResourceLocation, RenderType> renderTypeFunction)
	{
		super(renderTypeFunction);

		texWidth = 64;
		texHeight = 64;

		infusionStation = new ModelRenderer(this);
		infusionStation.setPos(0F, 16F, 0F);
		infusionStation.texOffs(0, 0).addBox(-5F, 7F, -3F, 12F, 1F, 6F, 0F, false);
		infusionStation.texOffs(14, 13).addBox(-7F, -6F, -1F, 2F, 14F, 2F, 0F, false);
		infusionStation.texOffs(0, 22).addBox(-4F, 0F, 0F, 5F, 7F, 0F, 0F, false);
		infusionStation.texOffs(0, 10).addBox(-1.5F, 0F, -2.5F, 0F, 7F, 5F, 0F, false);
		infusionStation.texOffs(16, 7).addBox(-5F, -6F, 0F, 6F, 6F, 0F, 0F, false);
		infusionStation.texOffs(0, 29).addBox(1F, -6F, 0F, 6F, 8F, 0F, 0F, false);
		infusionStation.texOffs(0, 7).addBox(2F, 3F, -2F, 4F, 4F, 4F, 0F, false);
		infusionStation.texOffs(0, 0).addBox(3.5F, 2F, -.5F, 1F, 1F, 1F, 0F, false);

		ModelRenderer apple = new ModelRenderer(this);
		apple.setPos(4.8543F, 2.6472F, 0F);
		infusionStation.addChild(apple);
		setRotationAngle(apple, 0F, 0F, .7854F);
		apple.texOffs(0, 1).addBox(0F, -1.5F, -.5F, 0F, 2F, 1F, 0F, false);
	}

	@Override
	public void renderToBuffer(MatrixStack pose, IVertexBuilder builder, int packedLight, int packedOverlay, float r, float g, float b, float a)
	{
		infusionStation.render(pose, builder, packedLight, packedOverlay, r, g, b, a);
	}

	private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
	{
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}
