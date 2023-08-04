package xyz.apex.minecraft.infusedfoods.neoforge;

import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.ApiStatus;
import xyz.apex.minecraft.apexcore.common.lib.PhysicalSide;
import xyz.apex.minecraft.infusedfoods.common.InfusedFoods;
import xyz.apex.minecraft.infusedfoods.common.InfusedFoodsClient;

@ApiStatus.Internal
@Mod(InfusedFoods.ID)
public final class InfusedFoodsForgeEP
{
    public InfusedFoodsForgeEP()
    {
        InfusedFoods.INSTANCE.bootstrap();
        PhysicalSide.CLIENT.runWhenOn(() -> InfusedFoodsClient.INSTANCE::bootstrap);
    }
}
