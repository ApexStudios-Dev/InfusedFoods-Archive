package xyz.apex.forge.potionfoods;

import net.minecraftforge.fml.common.Mod;

import xyz.apex.forge.potionfoods.init.PFRegistry;

@Mod(PotionFoods.ID)
public final class PotionFoods
{
	public static final String ID = "potionfoods";

	public PotionFoods()
	{
		PFRegistry.bootstrap();
	}
}