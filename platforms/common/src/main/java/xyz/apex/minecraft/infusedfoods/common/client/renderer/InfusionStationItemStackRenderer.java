package xyz.apex.minecraft.infusedfoods.common.client.renderer;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import xyz.apex.minecraft.apexcore.common.lib.client.renderer.ItemStackRenderer;
import xyz.apex.minecraft.infusedfoods.common.InfusedFoods;
import xyz.apex.minecraft.infusedfoods.common.block.entity.InfusionStationBlockEntity;

import java.util.Objects;
import java.util.function.Supplier;

public final class InfusionStationItemStackRenderer implements ItemStackRenderer
{
    private final Supplier<InfusionStationBlockEntity> blockEntity;

    public InfusionStationItemStackRenderer()
    {
        blockEntity = Suppliers.memoize(() -> Objects.requireNonNull(InfusedFoods.BLOCK_ENTITY.create(new BlockPos(0, Integer.MIN_VALUE, 0), InfusedFoods.BLOCK.defaultBlockState())));
    }

    @Override
    public void render(ItemStack stack, ItemDisplayContext displayContext, PoseStack pose, MultiBufferSource buffer, int packedLight, int packedOverlay)
    {
        var client = Minecraft.getInstance();

        if(client.level == null)
            return;

        var blockEntity = this.blockEntity.get();
        blockEntity.setLevel(client.level);

        var renderer = client.getBlockEntityRenderDispatcher().getRenderer(blockEntity);

        if(renderer == null)
            return;

        pose.pushPose();

        if(displayContext == ItemDisplayContext.GUI)
        {
            pose.mulPose(Axis.XP.rotationDegrees(30F));
            pose.mulPose(Axis.YP.rotationDegrees(225F));
            pose.mulPose(Axis.ZP.rotationDegrees(180F));
            pose.translate(.55D, -1.5D, -.15D);
            pose.scale(.85F, .95F, .9F);
        }
        else if(displayContext == ItemDisplayContext.HEAD)
        {
            pose.mulPose(Axis.ZP.rotationDegrees(180F));
            pose.translate(-.5D, -2.45D, .5D);
        }
        else if(displayContext == ItemDisplayContext.GROUND)
        {
            pose.mulPose(Axis.ZP.rotationDegrees(180F));
            pose.translate(-.5D, -1D, .5D);
            pose.scale(.45F, .45F, .45F);
        }
        else if(displayContext == ItemDisplayContext.FIXED)
        {
            pose.mulPose(Axis.ZP.rotationDegrees(180F));
            pose.translate(-.5D, -1.65D, .5D);
        }
        else if(displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
        {
            pose.mulPose(Axis.XP.rotationDegrees(180F));
            pose.translate(.25D, -1D, -.25D);
            pose.scale(.45F, .45F, .45F);
            pose.mulPose(Axis.YN.rotationDegrees(25F));
        }
        else if(displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
        {
            pose.mulPose(Axis.ZP.rotationDegrees(180F));
            pose.translate(-.6D, -1D, .25D);
            pose.scale(.45F, .45F, .45F);
            pose.mulPose(Axis.YP.rotationDegrees(25F));
        }
        else if(displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
        {
            pose.mulPose(Axis.XP.rotationDegrees(180F));
            pose.translate(.5D, -1D, -.4D);
            pose.scale(.45F, .45F, .45F);
        }
        else if(displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
        {
            pose.mulPose(Axis.ZP.rotationDegrees(180F));
            pose.translate(-.5D, -1D, .4D);
            pose.scale(.45F, .45F, .45F);
        }

        client.getItemRenderer().getModel(stack, null, null, 0).getTransforms().getTransform(displayContext).apply(false, pose);
        renderer.render(blockEntity, 0, pose, buffer, packedLight, packedOverlay);
        pose.popPose();
    }
}
