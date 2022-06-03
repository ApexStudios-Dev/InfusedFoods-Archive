package xyz.apex.forge.infusedfoods.init;

import com.tterrag.registrate.providers.RegistrateRecipeProvider;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.functions.CopyNbt;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.DatagenModLoader;

import xyz.apex.forge.infusedfoods.block.InfusionStationBlock;
import xyz.apex.forge.infusedfoods.block.entity.InfusionStationBlockEntity;
import xyz.apex.forge.infusedfoods.client.IFItemStackBlockEntityRenderer;
import xyz.apex.forge.infusedfoods.client.renderer.InfusionStationBlockEntityRenderer;
import xyz.apex.forge.infusedfoods.client.screen.InfusionStationContainerScreen;
import xyz.apex.forge.infusedfoods.container.InfusionStationContainer;
import xyz.apex.forge.utility.registrator.entry.BlockEntityEntry;
import xyz.apex.forge.utility.registrator.entry.BlockEntry;
import xyz.apex.forge.utility.registrator.entry.ContainerEntry;
import xyz.apex.forge.utility.registrator.entry.ItemEntry;

import static xyz.apex.forge.infusedfoods.block.entity.InfusionStationBlockEntity.*;
import static xyz.apex.forge.utility.registrator.provider.RegistrateLangExtProvider.EN_GB;

public final class IFElements
{
	private static final IFRegistry REGISTRY = IFRegistry.getInstance();

	public static final ResourceLocation INFUSION_STATION_CONTAINER_SCREEN_TEXTURE = REGISTRY.id("textures/gui/container/infusion_station.png");
	public static final ResourceLocation INFUSION_STATION_BLOCK_TEXTURE = REGISTRY.id("textures/models/infusion_station.png");
	public static final ResourceLocation INFUSION_STATION_BLOCK_TEXTURE_TINT = REGISTRY.id("textures/models/infusion_station_tint.png");

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
												.texture("particle", "minecraft:block/stone")
										)
									.build()
							)
			)
			.loot((lootTables, block) -> lootTables.add(block, LootTable
					.lootTable()
					.withPool(BlockLootTables.applyExplosionCondition(block, LootPool
							.lootPool()
							.setRolls(ConstantRange.exactly(1))
							.add(ItemLootEntry.lootTableItem(block))
							.apply(CopyNbt
									.copyData(CopyNbt.Source.BLOCK_ENTITY)
									.copy(buildNbtPath(NBT_APEX, NBT_INFUSION_FLUID), buildNbtPath(NBT_APEX, NBT_INFUSION_FLUID))
									.copy(buildNbtPath(NBT_APEX, NBT_BLAZE_FUEL), buildNbtPath(NBT_APEX, NBT_BLAZE_FUEL))
							)
					))
			))
			.recipe((ctx, provider) -> ShapedRecipeBuilder
					.shaped(ctx.get(), 1)
					.define('B', Tags.Items.RODS_BLAZE)
					.define('#', Tags.Items.STONE)
					.pattern(" B ")
					.pattern("###")
					.unlockedBy("has_blaze_rod", RegistrateRecipeProvider.hasItem(Tags.Items.RODS_BLAZE))
					.save(provider, ctx.getId())
			)

			.initialProperties(Material.METAL)
			.sound(SoundType.METAL)
			.harvestTool(ToolType.PICKAXE)
			.noOcclusion()
			.requiresCorrectToolForDrops()
			.strength(.5F)
			.lightLevel(blockState -> 1)

			.addRenderType(() -> RenderType::cutout)

			.item()
				.setISTER(() -> DatagenModLoader.isRunningDataGen() ? () -> null : IFItemStackBlockEntityRenderer::new)
				.model((ctx, provider) -> {
							ResourceLocation id = ctx.getId();
							ModelFile.UncheckedModelFile builtInEntity = new ModelFile.UncheckedModelFile("minecraft:builtin/entity");

							provider.getBuilder(id.getNamespace() + ":item/" + id.getPath())
						        .parent(builtInEntity)
								.transforms()
									.transform(ModelBuilder.Perspective.THIRDPERSON_RIGHT)
										.rotation(0F, 0F, 0F)
										.translation(0F, 0F, 0F)
										.scale(1F, 1F, 1F)
									.end()
									.transform(ModelBuilder.Perspective.THIRDPERSON_LEFT)
										.rotation(0F, 0F, 0F)
										.translation(0F, 0F, 0F)
										.scale(1F, 1F, 1F)
									.end()
									.transform(ModelBuilder.Perspective.FIRSTPERSON_RIGHT)
										.rotation(0F, 0F, 0F)
										.translation(0F, 0F, 0F)
										.scale(1F, 1F, 1F)
									.end()
									.transform(ModelBuilder.Perspective.FIRSTPERSON_LEFT)
										.rotation(0F, 0F, 0F)
										.translation(0F, 0F, 0F)
										.scale(1F, 1F, 1F)
									.end()
									.transform(ModelBuilder.Perspective.HEAD)
										.rotation(0F, 0F, 0F)
										.translation(0F, 0F, 0F)
										.scale(1F, 1F, 1F)
									.end()
									.transform(ModelBuilder.Perspective.GROUND)
										.rotation(0F, 0F, 0F)
										.translation(0F, 0F, 0F)
										.scale(1F, 1F, 1F)
									.end()
									.transform(ModelBuilder.Perspective.FIXED)
										.rotation(0F, 0F, 0F)
										.translation(0F, 0F, 0F)
										.scale(1F, 1F, 1F)
									.end()
									.transform(ModelBuilder.Perspective.GUI)
										.rotation(0F, 0F, 0F)
										.translation(0F, 0F, 0F)
										.scale(1F, 1F, 1F)
									.end()
								.end();
				})
			.build()

			.blockEntity(InfusionStationBlockEntity::new)
				.renderer(() -> InfusionStationBlockEntityRenderer::new)
			.build()

	.register();

	public static final ContainerEntry<InfusionStationContainer> INFUSION_STATION_CONTAINER = REGISTRY
			.container("infusion_station", InfusionStationContainer::new, () -> InfusionStationContainerScreen::new)
			.register();

	public static final ItemEntry<BlockItem> INFUSION_STATION_BLOCK_ITEM = ItemEntry.cast(INFUSION_STATION_BLOCK.getSibling(Item.class));
	public static final BlockEntityEntry<InfusionStationBlockEntity> INFUSION_STATION_BLOCK_ENTITY = BlockEntityEntry.cast(INFUSION_STATION_BLOCK.getSibling(TileEntityType.class));

	static void bootstrap()
	{
	}

	private static String buildNbtPath(String... paths)
	{
		return String.join(".", paths);
	}
}
