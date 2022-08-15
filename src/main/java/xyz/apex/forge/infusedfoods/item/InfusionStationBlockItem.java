package xyz.apex.forge.infusedfoods.item;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.IItemRenderProperties;

import xyz.apex.forge.infusedfoods.client.IFItemStackBlockEntityRenderer;

import java.util.function.Consumer;

public final class InfusionStationBlockItem extends BlockItem
{
	public InfusionStationBlockItem(Block block, Properties properties)
	{
		super(block, properties);
	}

	@Override
	public void initializeClient(Consumer<IItemRenderProperties> consumer)
	{
		consumer.accept(new IItemRenderProperties() {
			@Override
			public BlockEntityWithoutLevelRenderer getItemStackRenderer()
			{
				return IFItemStackBlockEntityRenderer.getInstance();
			}
		});
	}
}