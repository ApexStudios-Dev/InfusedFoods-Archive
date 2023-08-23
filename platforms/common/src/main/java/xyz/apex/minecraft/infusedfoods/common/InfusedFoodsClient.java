package xyz.apex.minecraft.infusedfoods.common;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import org.jetbrains.annotations.ApiStatus;
import xyz.apex.lib.Services;
import xyz.apex.minecraft.apexcore.common.lib.PhysicalSide;
import xyz.apex.minecraft.apexcore.common.lib.SideOnly;
import xyz.apex.minecraft.apexcore.common.lib.hook.RendererHooks;
import xyz.apex.minecraft.infusedfoods.common.client.renderer.model.InfusionStationModel;

import java.util.List;

@ApiStatus.NonExtendable
@SideOnly(PhysicalSide.CLIENT)
public interface InfusedFoodsClient
{
    InfusedFoodsClient INSTANCE = Services.singleton(InfusedFoodsClient.class);

    default void bootstrap()
    {
        RendererHooks.get().registerModelLayerDefinition(InfusionStationModel.LAYER_LOCATION, InfusionStationModel::createDefinition);
    }

    default void onItemTooltip(ItemStack stack, List<Component> tooltips)
    {
        if(InfusionHelper.isInfusedFood(stack))
        {
            if(InfusionHelper.arePotionEffectsHidden(stack))
                return;

            var effects = PotionUtils.getCustomEffects(stack);

            if(!effects.isEmpty())
                PotionUtils.addPotionTooltip(stack, tooltips, 1F);
        }
    }
}
