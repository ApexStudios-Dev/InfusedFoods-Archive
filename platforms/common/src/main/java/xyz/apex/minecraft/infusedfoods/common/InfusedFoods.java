package xyz.apex.minecraft.infusedfoods.common;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.SoundType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
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

import java.util.List;

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

    static void appendPotionEffectTooltips(@Nullable MobEffect effect, int amplifier, int duration, List<Component> tooltips)
    {
        if(effect == null)
            return;

        var potionName = effect.getDisplayName().copy();

        if(amplifier > 0)
            potionName = Component.translatable("potion.withAmplifier", potionName, Component.translatable("potion.potency.%d".formatted(amplifier)));

        if(duration > 20)
        {
            var i = Mth.floor((float) duration);
            var durationFormat = StringUtil.formatTickDuration(i);
            potionName = Component.translatable("potion.withDuration", potionName, durationFormat);
        }

        tooltips.add(potionName.withStyle(effect.getCategory().getTooltipFormatting()));

        var modifiers = effect.getAttributeModifiers();

        if(modifiers.isEmpty())
            return;

        tooltips.add(Component.empty());
        tooltips.add(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

        modifiers.forEach((attribute, modifier) -> {
            var mod = new AttributeModifier(modifier.getName(), effect.getAttributeModifierValue(amplifier, modifier), modifier.getOperation());

            var d0 = mod.getAmount();
            double d1;

            var operation = mod.getOperation();

            if(operation != AttributeModifier.Operation.MULTIPLY_BASE && operation != AttributeModifier.Operation.MULTIPLY_TOTAL)
                d1 = d0;
            else
                d1 = d0 * 100D;

            if(d0 > 0D)
                tooltips.add(Component.translatable("attribute.modifier.plus.%d".formatted(operation.toValue()), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(attribute.getDescriptionId())).withStyle(ChatFormatting.BLUE));
            else if(d0 < 0D)
            {
                d1 = d1 * -1D;
                tooltips.add(Component.translatable("attribute.modifier.take.%d".formatted(operation.toValue()), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(attribute.getDescriptionId())).withStyle(ChatFormatting.BLUE));
            }
        });
    }

    static void appendPotionEffectTooltips(ItemStack stack, List<Component> tooltips)
    {
        var blockEntityTag = BlockItem.getBlockEntityData(stack);

        if(blockEntityTag != null && blockEntityTag.contains(InfusionStationBlockEntity.NBT_INFUSION_FLUID, Tag.TAG_COMPOUND))
        {
            var fluidTag = blockEntityTag.getCompound(InfusionStationBlockEntity.NBT_INFUSION_FLUID);
            var effect = BuiltInRegistries.MOB_EFFECT.get(new ResourceLocation(fluidTag.getString(InfusionStationBlockEntity.NBT_EFFECT)));
            // var effectAmount = fluidTag.getInt(InfusionStationBlockEntity.NBT_AMOUNT);
            var effectDuration = fluidTag.getInt(InfusionStationBlockEntity.NBT_DURATION);
            var effectAmplifier = fluidTag.getInt(InfusionStationBlockEntity.NBT_AMPLIFIER);

            appendPotionEffectTooltips(effect, effectAmplifier, effectDuration, tooltips);
        }
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
