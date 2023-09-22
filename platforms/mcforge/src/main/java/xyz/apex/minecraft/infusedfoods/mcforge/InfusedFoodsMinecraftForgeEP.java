package xyz.apex.minecraft.infusedfoods.mcforge;

import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.ApiStatus;
import xyz.apex.minecraft.infusedfoods.common.InfusedFoods;

@ApiStatus.Internal
@Mod(InfusedFoods.ID)
public final class InfusedFoodsMinecraftForgeEP
{
    public InfusedFoodsMinecraftForgeEP()
    {
        InfusedFoods.INSTANCE.bootstrap();
    }
}
