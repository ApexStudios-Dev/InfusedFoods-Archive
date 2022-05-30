package xyz.apex.forge.potionfoods.init;

import org.apache.commons.lang3.Validate;

import net.minecraftforge.fml.ModLoadingContext;

import xyz.apex.forge.potionfoods.PotionFoods;
import xyz.apex.forge.utility.registrator.AbstractRegistrator;
import xyz.apex.java.utility.Lazy;

public final class PFRegistry extends AbstractRegistrator<PFRegistry>
{
	private static final Lazy<PFRegistry> INSTANCE = create(PFRegistry::new);
	private static boolean bootstrap = false;

	private PFRegistry()
	{
		super(PotionFoods.ID);
	}

	public static void bootstrap()
	{
		if(bootstrap)
			return;

		Validate.isTrue(ModLoadingContext.get().getActiveContainer().getModId().equals(PotionFoods.ID));
		bootstrap = true;

		PFElements.bootstrap();
	}

	public static PFRegistry getInstance()
	{
		return INSTANCE.get();
	}
}
