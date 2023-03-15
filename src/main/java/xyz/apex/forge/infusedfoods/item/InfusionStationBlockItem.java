package xyz.apex.forge.infusedfoods.item;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import xyz.apex.forge.infusedfoods.client.IFItemStackBlockEntityRenderer;

import java.util.function.Consumer;

public final class InfusionStationBlockItem extends BlockItem
{
	public InfusionStationBlockItem(Block block, Properties properties)
	{
		super(block, properties);
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer)
	{
		consumer.accept(new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer()
			{
				return IFItemStackBlockEntityRenderer.getInstance();
			}
		});
	}
}
