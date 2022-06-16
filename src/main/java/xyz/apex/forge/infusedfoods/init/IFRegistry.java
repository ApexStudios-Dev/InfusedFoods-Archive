package xyz.apex.forge.infusedfoods.init;

import org.apache.commons.lang3.Validate;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.fml.ModLoadingContext;

import xyz.apex.forge.commonality.init.Mods;
import xyz.apex.forge.utility.registrator.AbstractRegistrator;
import xyz.apex.java.utility.Lazy;

public final class IFRegistry extends AbstractRegistrator<IFRegistry>
{
	private static final Lazy<IFRegistry> INSTANCE = create(IFRegistry::new);
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

		IFElements.bootstrap();
	}

	public static IFRegistry getInstance()
	{
		return INSTANCE.get();
	}
}
