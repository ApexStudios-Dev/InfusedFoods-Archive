package xyz.apex.forge.infusedfoods.init;

import org.apache.commons.lang3.Validate;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.fml.ModLoadingContext;

import xyz.apex.forge.apexcore.registrate.BasicRegistrate;
import xyz.apex.forge.commonality.Mods;

public final class IFRegistry
{
	public static final BasicRegistrate INSTANCE = BasicRegistrate.create(Mods.INFUSED_FOODS, registrate -> registrate
			.modifyCreativeModeTab(() -> CreativeModeTabs.TOOLS_AND_UTILITIES, modifier -> modifier.accept(IFElements.INFUSION_STATION_BLOCK::asItem))
			.modifyCreativeModeTab(() -> CreativeModeTabs.FUNCTIONAL_BLOCKS, modifier -> modifier.accept(IFElements.INFUSION_STATION_BLOCK::asItem))
			.modifyCreativeModeTab(() -> CreativeModeTabs.FOOD_AND_DRINKS, modifier -> modifier.accept(IFElements.INFUSION_STATION_BLOCK::asItem))
	);

	private static boolean bootstrap = false;

	public static void bootstrap()
	{
		if(bootstrap)
			return;

		Validate.isTrue(ModLoadingContext.get().getActiveContainer().getModId().equals(Mods.INFUSED_FOODS));
		bootstrap = true;

		IFElements.bootstrap();
	}
}
