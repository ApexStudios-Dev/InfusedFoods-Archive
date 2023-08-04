package xyz.apex.minecraft.infusedfoods.fabric.entrypoint;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import org.jetbrains.annotations.ApiStatus;
import xyz.apex.minecraft.apexcore.common.lib.resgen.ApexDataProvider;
import xyz.apex.minecraft.infusedfoods.common.InfusedFoods;

@ApiStatus.Internal
public final class InfusedFoodsDataGeneratorEntrypoint implements DataGeneratorEntrypoint
{
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator)
    {
        ApexDataProvider.register(InfusedFoods.ID, func -> generator.createPack().addProvider(func::apply));
    }
}
