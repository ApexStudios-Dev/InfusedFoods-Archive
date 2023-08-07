package xyz.apex.minecraft.infusedfoods.common;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.SoundType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import xyz.apex.lib.Services;
import xyz.apex.minecraft.apexcore.common.lib.helper.TagHelper;
import xyz.apex.minecraft.apexcore.common.lib.hook.CreativeModeTabHooks;
import xyz.apex.minecraft.apexcore.common.lib.registry.Registrar;
import xyz.apex.minecraft.apexcore.common.lib.registry.entry.*;
import xyz.apex.minecraft.apexcore.common.lib.resgen.ProviderTypes;
import xyz.apex.minecraft.infusedfoods.common.block.InfusionStationBlock;
import xyz.apex.minecraft.infusedfoods.common.block.entity.InfusionStationBlockEntity;
import xyz.apex.minecraft.infusedfoods.common.client.renderer.InfusionStationBlockEntityRenderer;
import xyz.apex.minecraft.infusedfoods.common.client.renderer.InfusionStationItemStackRenderer;
import xyz.apex.minecraft.infusedfoods.common.client.screen.InfusionStationMenuScreen;
import xyz.apex.minecraft.infusedfoods.common.menu.InfusionStationMenu;
import xyz.apex.minecraft.infusedfoods.common.recipe.InfusionCleansingRecipe;
import xyz.apex.minecraft.infusedfoods.common.recipe.InfusionHideRecipe;

@ApiStatus.NonExtendable
public interface InfusedFoods
{
    Logger LOGGER = LogManager.getLogger();
    String ID = "infusedfoods";

    InfusedFoods INSTANCE = Services.singleton(InfusedFoods.class);

    Registrar REGISTRAR = Registrar.create(ID).object("infusion_station");

    TagKey<Item> INFUSION_HIDER = TagHelper.itemTag(ID, "infusion_hider");
    TagKey<Item> INFUSION_CLEANSING = TagHelper.itemTag(ID, "infusion_cleansing");

    BlockEntry<InfusionStationBlock> BLOCK = infusionStation();
    ItemEntry<BlockItem> ITEM = ItemEntry.cast(BLOCK.getSibling(Registries.ITEM));
    BlockEntityEntry<InfusionStationBlockEntity> BLOCK_ENTITY = BlockEntityEntry.cast(BLOCK.getSibling(Registries.BLOCK_ENTITY_TYPE));
    MenuEntry<InfusionStationMenu> MENU = REGISTRAR.menu(InfusionStationMenu::forNetwork, () -> () -> InfusionStationMenuScreen::new);
    RegistryEntry<SimpleCraftingRecipeSerializer<InfusionHideRecipe>> INFUSION_HIDE_RECIPE = REGISTRAR.recipeSerializer("infusion_hider", () -> new SimpleCraftingRecipeSerializer<>(InfusionHideRecipe::new)).register();
    RegistryEntry<SimpleCraftingRecipeSerializer<InfusionCleansingRecipe>> INFUSION_CLEANSE_RECIPE = REGISTRAR.recipeSerializer("infusion_cleansing", () -> new SimpleCraftingRecipeSerializer<>(InfusionCleansingRecipe::new)).register();

    default void bootstrap()
    {
        var creativeModeTabs = CreativeModeTabHooks.get();
        creativeModeTabs.modify(CreativeModeTabs.TOOLS_AND_UTILITIES, output -> output.accept(BLOCK));
        creativeModeTabs.modify(CreativeModeTabs.FUNCTIONAL_BLOCKS, output -> output.accept(BLOCK));
        creativeModeTabs.modify(CreativeModeTabs.FOOD_AND_DRINKS, output -> output.accept(BLOCK));

        REGISTRAR.register();
        registerGenerators();
    }

    static void onFinishItemUse(LivingEntity entity, ItemStack stack)
    {
        if(entity.level().isClientSide)
            return;

        if(InfusionHelper.isInfusedFood(stack))
        {
            var effects = PotionUtils.getCustomEffects(stack);

            for(var effectInstance : effects)
            {
                var effect = effectInstance.getEffect();

                if(effect.isInstantenous())
                    effect.applyInstantenousEffect(null, null, entity, effectInstance.getAmplifier(), 1D);
                else
                {
                    if(entity.hasEffect(effect))
                        entity.removeEffect(effect);

                    entity.addEffect(new MobEffectInstance(effectInstance));
                }
            }
        }
    }

    private void registerGenerators()
    {
        var descriptionKey = "pack.%s.description".formatted(ID);

        ProviderTypes.LANGUAGES.addListener(ID, (provider, lookup) -> provider
                .enUS()
                    .add(descriptionKey, "InfusedFoods")
                .end()
        );

        ProviderTypes.registerDefaultMcMetaGenerator(ID, Component.translatable(descriptionKey));

        ProviderTypes.RECIPES.addListener(ID, (provider, lookup) -> {
            SpecialRecipeBuilder.special(INFUSION_CLEANSE_RECIPE.value()).save(provider::add, INFUSION_CLEANSE_RECIPE.getRegistryName().toString());
            SpecialRecipeBuilder.special(INFUSION_HIDE_RECIPE.value()).save(provider::add, INFUSION_HIDE_RECIPE.getRegistryName().toString());
        });

        ProviderTypes.ITEM_TAGS.addListener(ID, (provider, lookup) -> {
            provider.tag(INFUSION_CLEANSING).addElement(Items.MILK_BUCKET);
            provider.tag(INFUSION_HIDER).addElement(Items.SPIDER_EYE);
        });
    }

    private static BlockEntry<InfusionStationBlock> infusionStation()
    {
        return REGISTRAR
                .block(InfusionStationBlock::new)
                .sound(SoundType.METAL)
                .noOcclusion()
                .requiresCorrectToolForDrops()
                .strength(.5F)
                .lightLevel(1)

                .defaultBlockState((modelProvider, lookup, entry) -> modelProvider
                        .getBuilder(entry.getRegistryName().withPrefix("block/"))
                        .texture("particle", "block/stone")
                )
                .recipe((provider, lookup, entry) -> ShapedRecipeBuilder
                        .shaped(RecipeCategory.TOOLS, entry, 1)
                        .define('B', Items.BLAZE_ROD)
                        .define('S', ItemTags.STONE_CRAFTING_MATERIALS)
                        .pattern(" B ")
                        .pattern("SSS")
                        .unlockedBy("has_blaze_rod", provider.has(Items.BLAZE_ROD))
                        .save(provider::add, entry.getRegistryName())
                )
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .renderType(() -> RenderType::cutout)

                .blockEntity(InfusionStationBlockEntity::new)
                    .renderer(() -> () -> InfusionStationBlockEntityRenderer::new)
                .build()

                .item()
                    .renderer(() -> InfusionStationItemStackRenderer::new)
                    .model((provider, lookup, entry) -> provider.withParent(
                            entry.getRegistryName().withPrefix("item/"),
                            provider.existingModel(new ResourceLocation("builtin/entity"))
                    ))
                .build()
        .register();
    }
}
