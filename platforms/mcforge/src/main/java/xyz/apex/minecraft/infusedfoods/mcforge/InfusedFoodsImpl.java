package xyz.apex.minecraft.infusedfoods.mcforge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import org.jetbrains.annotations.ApiStatus;
import xyz.apex.minecraft.apexcore.common.lib.PhysicalSide;
import xyz.apex.minecraft.apexcore.mcforge.lib.EventBusHelper;
import xyz.apex.minecraft.apexcore.mcforge.lib.EventBuses;
import xyz.apex.minecraft.infusedfoods.common.InfusedFoods;
import xyz.apex.minecraft.infusedfoods.common.InfusedFoodsClient;

@ApiStatus.Internal
public final class InfusedFoodsImpl implements InfusedFoods
{
    @Override
    public void bootstrap()
    {
        InfusedFoods.super.bootstrap();
        EventBuses.registerForJavaFML();
        EventBusHelper.addListener(MinecraftForge.EVENT_BUS, this::onLivingItemUseFinish);
        PhysicalSide.CLIENT.runWhenOn(() -> InfusedFoodsClient.INSTANCE::bootstrap);
    }

    private void onLivingItemUseFinish(LivingEntityUseItemEvent.Finish event)
    {
        InfusedFoods.onFinishItemUse(event.getEntity(), event.getItem());
    }
}
