package xyz.apex.minecraft.infusedfoods.common;

import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import xyz.apex.lib.Services;
import xyz.apex.minecraft.apexcore.common.lib.resgen.ProviderTypes;

@ApiStatus.NonExtendable
public interface InfusedFoods
{
    Logger LOGGER = LogManager.getLogger();
    String ID = "infusedfoods";

    InfusedFoods INSTANCE = Services.singleton(InfusedFoods.class);

    default void bootstrap()
    {
        registerGenerators();
    }

    private void registerGenerators()
    {
        var descriptionKey = "pack.%s.description".formatted(ID);

        ProviderTypes.LANGUAGES.addListener(ID, (provider, lookup) -> provider
                .enUS()
                    .add(descriptionKey, "InfusedFoods")
                .end()
        );

        ProviderTypes.registerDefaultMcMetaGenerator(ID, Component.translatable(descriptionKey));
    }
}
