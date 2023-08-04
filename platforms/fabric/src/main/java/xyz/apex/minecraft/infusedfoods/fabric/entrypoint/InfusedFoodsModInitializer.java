package xyz.apex.minecraft.infusedfoods.fabric.entrypoint;

import net.fabricmc.api.ModInitializer;
import org.jetbrains.annotations.ApiStatus;
import xyz.apex.minecraft.infusedfoods.common.InfusedFoods;

@ApiStatus.Internal
public final class InfusedFoodsModInitializer implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        InfusedFoods.INSTANCE.bootstrap();
    }
}
