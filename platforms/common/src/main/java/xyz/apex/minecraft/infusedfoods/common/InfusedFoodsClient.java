package xyz.apex.minecraft.infusedfoods.common;

import org.jetbrains.annotations.ApiStatus;
import xyz.apex.lib.Services;
import xyz.apex.minecraft.apexcore.common.lib.PhysicalSide;
import xyz.apex.minecraft.apexcore.common.lib.SideOnly;

@ApiStatus.NonExtendable
@SideOnly(PhysicalSide.CLIENT)
public interface InfusedFoodsClient
{
    InfusedFoodsClient INSTANCE = Services.singleton(InfusedFoodsClient.class);

    default void bootstrap()
    {
    }
}
