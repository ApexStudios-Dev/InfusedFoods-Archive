package xyz.apex.forge.infusedfoods.init;

import com.tterrag.registrate.AbstractRegistrate;
import org.apache.commons.lang3.Validate;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import xyz.apex.forge.commonality.Mods;

public final class IFRegistry extends AbstractRegistrate<IFRegistry>
{
	public static final IFRegistry INSTANCE = new IFRegistry();
	private static boolean bootstrap = false;

	private IFRegistry()
	{
		super(Mods.INFUSED_FOODS);

		creativeModeTab(() -> CreativeModeTab.TAB_BREWING);
	}

	public static void bootstrap()
	{
		if(bootstrap)
			return;

		Validate.isTrue(ModLoadingContext.get().getActiveContainer().getModId().equals(Mods.INFUSED_FOODS));
		bootstrap = true;

		INSTANCE.registerEventListeners(FMLJavaModLoadingContext.get().getModEventBus());
		IFElements.bootstrap();
	}
}
