package xyz.apex.forge.potionfoods.init;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.IntArray;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.ToolType;

import xyz.apex.forge.potionfoods.block.InfusionStationBlock;
import xyz.apex.forge.potionfoods.block.entity.InfusionStationBlockEntity;
import xyz.apex.forge.potionfoods.block.entity.InfusionStationInventory;
import xyz.apex.forge.potionfoods.client.renderer.InfusionStationBlockEntityRenderer;
import xyz.apex.forge.potionfoods.client.screen.InfusionStationContainerScreen;
import xyz.apex.forge.potionfoods.container.InfusionStationContainer;
import xyz.apex.forge.utility.registrator.entry.BlockEntityEntry;
import xyz.apex.forge.utility.registrator.entry.BlockEntry;
import xyz.apex.forge.utility.registrator.entry.ContainerEntry;
import xyz.apex.forge.utility.registrator.entry.ItemEntry;

import static xyz.apex.forge.utility.registrator.provider.RegistrateLangExtProvider.EN_GB;

public final class PFElements
{
	private static final PFRegistry REGISTRY = PFRegistry.getInstance();

	public static final ResourceLocation INFUSION_STATION_CONTAINER_SCREEN_TEXTURE = REGISTRY.id("textures/gui/container/infusion_station.png");
	public static final ResourceLocation INFUSION_STATION_BLOCK_TEXTURE = REGISTRY.id("textures/models/infusion_station.png");

	public static final BlockEntry<InfusionStationBlock> INFUSION_STATION_BLOCK = REGISTRY
			.block("infusion_station", InfusionStationBlock::new)

			.lang("Infusion Station")
			.lang(EN_GB, "Infusion Station")

			.blockState((ctx, provider) -> provider
							.getVariantBuilder(ctx.get())
							.forAllStates(blockState -> ConfiguredModel
									.builder()
										.modelFile(provider
												.models()
												.getBuilder(ctx.getName())
												.texture("particle", INFUSION_STATION_BLOCK_TEXTURE.getNamespace() + ":models/infusion_station")
										)
									.build()
							)
			)

			.initialProperties(Material.METAL)
			.sound(SoundType.METAL)
			.requiresCorrectToolForDrops()
			.harvestTool(ToolType.PICKAXE)
			.noOcclusion()

			.addRenderType(() -> RenderType::cutout)

			.item()
				.model((ctx, provider) -> {
							ResourceLocation id = ctx.getId();
							ModelFile.UncheckedModelFile builtInEntity = new ModelFile.UncheckedModelFile("minecraft:builtin/entity");

							provider.getBuilder(id.getNamespace() + ":item/" + id.getPath())
						        .parent(builtInEntity)
								.transforms()
									.transform(ModelBuilder.Perspective.THIRDPERSON_RIGHT)
										.rotation(75F, 45F, 0F)
										.translation(0F, 3F, 4F)
										.scale(.375F, .375F, .375F)
									.end()
									.transform(ModelBuilder.Perspective.THIRDPERSON_LEFT)
										.rotation(75F, 45F, 0F)
										.translation(0F, 3F, 4F)
										.scale(.375F, .375F, .375F)
									.end()
									.transform(ModelBuilder.Perspective.FIRSTPERSON_RIGHT)
										.rotation(0F, 135F, 0F)
										.translation(0F, 7F, 0F)
										.scale(.4F, .4F, .4F)
									.end()
									.transform(ModelBuilder.Perspective.FIRSTPERSON_LEFT)
										.rotation(0F, 135F, 0F)
										.translation(0F, 7F, 0F)
										.scale(.4F, .4F, .4F)
									.end()
									.transform(ModelBuilder.Perspective.HEAD)
										.rotation(0F, 0F, 0F)
										.translation(0F, 30F, 0F)
										.scale(1F, 1F, 1F)
									.end()
									.transform(ModelBuilder.Perspective.GROUND)
										.rotation(0F, 0F, 0F)
										.translation(0F, 6F, 0F)
										.scale(.25F, .25F, .25F)
									.end()
									.transform(ModelBuilder.Perspective.FIXED)
										.rotation(-90F, 0F, 0F)
										.translation(0F, 0F, -23F)
										.scale(1F, 1F, 1F)
									.end()
									.transform(ModelBuilder.Perspective.GUI)
										.rotation(30F, -135F, 0F)
										.translation(0F, 3F, 0F)
										.scale(.5F, .5F, .5F)
									.end()
								.end();
				})
			.build()

			.blockEntity(InfusionStationBlockEntity::new)
				.renderer(() -> InfusionStationBlockEntityRenderer::new)
			.build()

	.register();

	public static final ContainerEntry<InfusionStationContainer> INFUSION_STATION_CONTAINER = REGISTRY
			.container("infusion_station", (containerType, windowId, playerInventory, buffer) -> new InfusionStationContainer(containerType, windowId, playerInventory, new InfusionStationInventory(), new IntArray(InfusionStationBlockEntity.DATA_SLOT_COUNT)), () -> InfusionStationContainerScreen::new)
			.register();

	public static final ItemEntry<BlockItem> INFUSION_STATION_BLOCK_ITEM = ItemEntry.cast(INFUSION_STATION_BLOCK.getSibling(Item.class));
	public static final BlockEntityEntry<InfusionStationBlockEntity> INFUSION_STATION_BLOCK_ENTITY = BlockEntityEntry.cast(INFUSION_STATION_BLOCK.getSibling(TileEntityType.class));

	static void bootstrap()
	{
	}
}
