package xyz.apex.minecraft.infusedfoods.common.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import xyz.apex.minecraft.apexcore.common.lib.PhysicalSide;
import xyz.apex.minecraft.apexcore.common.lib.SideOnly;
import xyz.apex.minecraft.apexcore.common.lib.component.block.entity.types.BlockEntityComponentTypes;
import xyz.apex.minecraft.apexcore.common.lib.component.block.types.HorizontalFacingBlockComponent;
import xyz.apex.minecraft.infusedfoods.common.InfusionHelper;
import xyz.apex.minecraft.infusedfoods.common.block.entity.InfusionStationBlockEntity;
import xyz.apex.minecraft.infusedfoods.common.client.renderer.model.InfusionStationModel;

@SideOnly(PhysicalSide.CLIENT)
public final class InfusionStationBlockEntityRenderer implements BlockEntityRenderer<InfusionStationBlockEntity>
{
    private final InfusionStationModel model;

    public InfusionStationBlockEntityRenderer(BlockEntityRendererProvider.Context ctx)
    {
        model = new InfusionStationModel(ctx);
    }

    @Override
    public void render(InfusionStationBlockEntity blockEntity, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight, int packedOverlay)
    {
        var forGui = blockEntity.getBlockPos().getY() == Integer.MIN_VALUE;

        boolean hasPotion;
        boolean hasFluid;
        boolean hasFood;
        int color;

        if(forGui)
        {
            hasFluid = true;
            hasPotion = true;
            hasFood = true;
            color = 0x385dc6;
        }
        else
        {
            var container = blockEntity.getRequiredComponent(BlockEntityComponentTypes.INVENTORY);
            var effect = blockEntity.getEffect();
            var effectAmount = blockEntity.getEffectAmount();

            hasPotion = !container.getItem(InfusionStationBlockEntity.SLOT_POTION).isEmpty();
            // hasBottle = !container.getItem(InfusionStationBlockEntity.SLOT_BOTTLE).isEmpty();
            hasFluid = effect != null && effectAmount > 0;
            hasFood = !container.getItem(InfusionStationBlockEntity.SLOT_FOOD).isEmpty();

            color = InfusionHelper.getEffectColor(effect, blockEntity.getEffectAmplifier());
        }

        model.setUpForRender(hasPotion, hasPotion, hasFluid, forGui && hasFood, color);
        pose.pushPose();

        if(!forGui)
        {
            var facing = blockEntity.getBlockState().getValue(HorizontalFacingBlockComponent.FACING);

            pose.translate(.5D, .5D, .5D);
            pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
            pose.mulPose(Axis.XP.rotationDegrees(180F));
            pose.translate(0D, -1D, 0D);
        }

        if(!forGui && hasFood)
        {
            pose.pushPose();

            pose.mulPose(Axis.ZP.rotationDegrees(180F));
            pose.translate(-.275D, -1.2D, 0D);
            pose.scale(.4F, .4F, .4F);

            var food = blockEntity.getRequiredComponent(BlockEntityComponentTypes.INVENTORY).getItem(InfusionStationBlockEntity.SLOT_FOOD);
            var client = Minecraft.getInstance();
            var seed = client.player == null ? 0 : client.player.getId();
            var itemModel = client.getItemRenderer().getModel(food, client.level, client.player, seed);
            client.getItemRenderer().render(food, ItemDisplayContext.NONE, false, pose, buffer, packedLight, packedOverlay, itemModel);

            pose.popPose();
        }

        var renderType = RenderType.entityCutout(InfusionStationModel.TEXTURE_LOCATION);
        var modelBuffer = buffer.getBuffer(renderType);
        model.renderToBuffer(pose, modelBuffer, packedLight, packedOverlay, 1F, 1F, 1F, 1F);

        pose.pushPose();
        pose.translate(0D, 1D, 0D);

        renderType = RenderType.entityTranslucentCull(InfusionStationModel.TINT_TEXTURE_LOCATION);
        modelBuffer = buffer.getBuffer(renderType);
        model.renderToBufferTint(pose, modelBuffer, packedLight, packedOverlay, 1F, 1F, 1F, .75F, true);

        pose.popPose();
        pose.popPose();
    }
}
