package xyz.apex.minecraft.infusedfoods.neoforge;

import org.jetbrains.annotations.ApiStatus;
import xyz.apex.minecraft.apexcore.neoforge.lib.EventBuses;
import xyz.apex.minecraft.infusedfoods.common.InfusedFoods;

@ApiStatus.Internal
public final class InfusedFoodsImpl implements InfusedFoods
{
    @Override
    public void bootstrap()
    {
        InfusedFoods.super.bootstrap();
        EventBuses.registerForJavaFML();
    }
}
