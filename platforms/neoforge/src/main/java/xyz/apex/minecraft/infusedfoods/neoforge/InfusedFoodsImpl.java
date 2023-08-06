package xyz.apex.minecraft.infusedfoods.neoforge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import org.jetbrains.annotations.ApiStatus;
import xyz.apex.minecraft.apexcore.neoforge.lib.EventBusHelper;
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
        EventBusHelper.addListener(MinecraftForge.EVENT_BUS, this::onLivingItemUseFinish);
    }

    private void onLivingItemUseFinish(LivingEntityUseItemEvent.Finish event)
    {
        InfusedFoods.onFinishItemUse(event.getEntity(), event.getItem());
    }
}
