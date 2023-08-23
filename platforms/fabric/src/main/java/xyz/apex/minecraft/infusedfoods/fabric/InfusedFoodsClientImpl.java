package xyz.apex.minecraft.infusedfoods.fabric;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import org.jetbrains.annotations.ApiStatus;
import xyz.apex.minecraft.infusedfoods.common.InfusedFoodsClient;

@ApiStatus.Internal
public final class InfusedFoodsClientImpl implements InfusedFoodsClient
{
    @Override
    public void bootstrap()
    {
        InfusedFoodsClient.super.bootstrap();

        ItemTooltipCallback.EVENT.register((stack, flag, tooltips) -> onItemTooltip(stack, tooltips));
    }
}
