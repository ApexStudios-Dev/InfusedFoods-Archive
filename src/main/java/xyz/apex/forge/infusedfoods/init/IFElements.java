package xyz.apex.forge.infusedfoods.init;

import com.tterrag.registrate.providers.RegistrateRecipeProvider;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.Tags;

import xyz.apex.forge.infusedfoods.block.InfusionStationBlock;
import xyz.apex.forge.infusedfoods.block.entity.InfusionStationBlockEntity;
import xyz.apex.forge.infusedfoods.block.entity.InfusionStationInventory;
import xyz.apex.forge.infusedfoods.client.renderer.InfusionStationBlockEntityRenderer;
import xyz.apex.forge.infusedfoods.client.screen.InfusionStationContainerScreen;
import xyz.apex.forge.infusedfoods.container.InfusionStationMenu;
import xyz.apex.forge.infusedfoods.item.InfusionStationBlockItem;
import xyz.apex.forge.utility.registrator.entry.BlockEntityEntry;
import xyz.apex.forge.utility.registrator.entry.BlockEntry;
import xyz.apex.forge.utility.registrator.entry.ItemEntry;
import xyz.apex.forge.utility.registrator.entry.MenuEntry;

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
					.withPool(BlockLoot.applyExplosionCondition(block, LootPool
							.lootPool()
							.setRolls(ConstantValue.exactly(1))
							.add(LootItem.lootTableItem(block))
							.apply(CopyNbtFunction
									.copyData(ContextNbtProvider.BLOCK_ENTITY)
									.copy(InfusionStationBlockEntity.NBT_BLAZE_FUEL, InfusionStationBlockEntity.NBT_BLAZE_FUEL)
									.copy(InfusionStationBlockEntity.NBT_CUSTOM_NAME, InfusionStationBlockEntity.NBT_CUSTOM_NAME)
									.copy(InfusionStationBlockEntity.NBT_INVENTORY + '.' + InfusionStationInventory.NBT_INFUSION_FLUID, InfusionStationBlockEntity.NBT_INVENTORY + '.' + InfusionStationInventory.NBT_INFUSION_FLUID)
							)
					))
			))
			.recipe((ctx, provider) -> ShapedRecipeBuilder
					.shaped(ctx.get(), 1)
					.define('B', Tags.Items.RODS_BLAZE)
					.define('#', Tags.Items.STONE)
					.pattern(" B ")
					.pattern("###")
					.unlockedBy("has_blaze_rod", RegistrateRecipeProvider.has(Tags.Items.RODS_BLAZE))
					.save(provider, ctx.getId())
			)

			.initialProperties(Material.METAL)
			.sound(SoundType.METAL)
			.noOcclusion()
			.requiresCorrectToolForDrops()
			.strength(.5F)
			.lightLevel(blockState -> 1)

			.addRenderType(() -> RenderType::cutout)

			.tag(BlockTags.MINEABLE_WITH_PICKAXE)

			.item(InfusionStationBlockItem::new)
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

	public static final MenuEntry<InfusionStationMenu> INFUSION_STATION_CONTAINER = REGISTRY
			.container("infusion_station", (containerType, windowId, playerInventory, buffer) -> new InfusionStationMenu(containerType, windowId, playerInventory, new InfusionStationInventory(), new SimpleContainerData(InfusionStationBlockEntity.DATA_SLOT_COUNT)), () -> InfusionStationContainerScreen::new)
			.register();

	public static final ItemEntry<InfusionStationBlockItem> INFUSION_STATION_BLOCK_ITEM = ItemEntry.cast(INFUSION_STATION_BLOCK.getSibling(Item.class));
	public static final BlockEntityEntry<InfusionStationBlockEntity> INFUSION_STATION_BLOCK_ENTITY = BlockEntityEntry.cast(INFUSION_STATION_BLOCK.getSibling(BlockEntityType.class));

	static void bootstrap()
	{
	}
}
