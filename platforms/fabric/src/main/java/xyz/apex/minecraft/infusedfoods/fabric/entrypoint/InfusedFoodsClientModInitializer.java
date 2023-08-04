package xyz.apex.minecraft.infusedfoods.fabric.entrypoint;

import net.fabricmc.api.ClientModInitializer;
import org.jetbrains.annotations.ApiStatus;
import xyz.apex.minecraft.infusedfoods.common.InfusedFoodsClient;

@ApiStatus.Internal
public final class InfusedFoodsClientModInitializer implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        InfusedFoodsClient.INSTANCE.bootstrap();
    }
}
