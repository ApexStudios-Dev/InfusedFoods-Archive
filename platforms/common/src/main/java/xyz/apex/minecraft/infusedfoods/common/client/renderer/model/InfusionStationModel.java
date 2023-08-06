package xyz.apex.minecraft.infusedfoods.common.client.renderer.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import xyz.apex.minecraft.apexcore.common.lib.PhysicalSide;
import xyz.apex.minecraft.apexcore.common.lib.SideOnly;
import xyz.apex.minecraft.infusedfoods.common.InfusedFoods;

@SideOnly(PhysicalSide.CLIENT)
public final class InfusionStationModel extends Model
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(InfusedFoods.ID, "infusion_station"), "main");
    public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(InfusedFoods.ID, "textures/models/infusion_station.png");
    public static final ResourceLocation TINT_TEXTURE_LOCATION = new ResourceLocation(InfusedFoods.ID, "textures/models/infusion_station_tint.png");

    private final ModelPart infusionStation;
    private int potionColor = 0x385dc6;

    public InfusionStationModel(ModelPart root)
    {
        super(RenderType::entityTranslucentCull);

        infusionStation = root.getChild("infusion_station");
    }

    public InfusionStationModel(BlockEntityRendererProvider.Context ctx)
    {
        this(ctx.bakeLayer(LAYER_LOCATION));
    }

    public void setUpForRender(boolean hasPotion, boolean hasBottle, boolean hasFluid, boolean hasFood, int potionColor)
    {
        this.potionColor = potionColor;

        infusionStation.visible = true;
        infusionStation.getAllParts().forEach(part -> part.visible = true);

        var potion = infusionStation.getChild("potion");
        var potionTint = infusionStation.getChild("potion_tint");
        var apple = infusionStation.getChild("apple");

        potion.visible = hasPotion || hasBottle || hasFluid;
        apple.visible = hasFood;
        potionTint.visible = false;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer builder, int packedLight, int packedOverlay, float r, float g, float b, float a)
    {
        renderToBufferTint(pose, builder, packedLight, packedOverlay, r, g, b, a, false);
    }

    public void renderToBufferTint(PoseStack pose, VertexConsumer builder, int packedLight, int packedOverlay, float r, float g, float b, float a, boolean tint)
    {
        if(tint)
        {
            var potion = infusionStation.getChild("potion");

            if(potion.visible)
            {
                var potionTint = infusionStation.getChild("potion_tint");

                // var pAlpha = (float) (potionColor >> 24 & 255) / 255F;
                var pRed = (float) (potionColor >> 16 & 255) / 255F;
                var pGreen = (float) (potionColor >> 8 & 255) / 255F;
                var pBlue = (float) (potionColor & 255) / 255F;

                potionTint.visible = true;
                potionTint.render(pose, builder, packedLight, packedOverlay, pRed, pGreen, pBlue, a);
            }
        }
        else
            infusionStation.render(pose, builder, packedLight, packedOverlay, r, g, b, a);
    }

    public static LayerDefinition createDefinition()
    {
        var meshDefinition = new MeshDefinition();
        var partDefinition = meshDefinition.getRoot();

        var infusion_station = partDefinition.addOrReplaceChild("infusion_station", CubeListBuilder.create(), PartPose.offset(0F, 16F, 0F));
        var base = infusion_station.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(-7F, -1F, 0F, 12F, 1F, 6F, new CubeDeformation(0F)), PartPose.offset(2F, 8F, -3F));
        var blaze_rod = infusion_station.addOrReplaceChild("blaze_rod", CubeListBuilder.create().texOffs(14, 13).addBox(-6F, -14F, 2F, 2F, 14F, 2F, new CubeDeformation(0F)), PartPose.offset(-1F, 8F, -3F));
        var potion = infusion_station.addOrReplaceChild("potion", CubeListBuilder.create().texOffs(0, 22).addBox(-2.5F, -3.5F, 0F, 5F, 7F, 0F, new CubeDeformation(0F)).texOffs(0, 10).addBox(0F, -3.5F, -2.5F, 0F, 7F, 5F, new CubeDeformation(0F)), PartPose.offset(-1.5F, 3.5F, 0F));
        var spout = infusion_station.addOrReplaceChild("spout", CubeListBuilder.create().texOffs(16, 7).addBox(-6F, -6F, 0F, 6F, 6F, 0F, new CubeDeformation(0F)).texOffs(0, 29).addBox(0F, -6F, 0F, 6F, 8F, 0F, new CubeDeformation(0F)), PartPose.offset(1F, 0F, 0F));
        var apple = infusion_station.addOrReplaceChild("apple", CubeListBuilder.create().texOffs(0, 7).addBox(-2.8543F, .3528F, -2F, 4F, 4F, 4F, new CubeDeformation(0F)).texOffs(0, 0).addBox(-1.3543F, -.6472F, -.5F, 1F, 1F, 1F, new CubeDeformation(0F)), PartPose.offset(4.8543F, 2.6472F, 0F));
        var apple_3_r1 = apple.addOrReplaceChild("apple_3_r1", CubeListBuilder.create().texOffs(0, 1).addBox(0F, -1.5F, -.5F, 0F, 2F, 1F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, .7854F));

        var potionTint = infusion_station.addOrReplaceChild("potion_tint", CubeListBuilder.create().texOffs(0, 22).addBox(-2.5F, -3.5F, 0F, 5F, 7F, 0F, new CubeDeformation(0F)).texOffs(0, 10).addBox(0F, -3.5F, -2.5F, 0F, 7F, 5F, new CubeDeformation(0F)), PartPose.offset(-1.5F, 3.5F, 0F));

        return LayerDefinition.create(meshDefinition, 64, 64);
    }
}
