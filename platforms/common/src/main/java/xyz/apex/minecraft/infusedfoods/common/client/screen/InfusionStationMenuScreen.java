package xyz.apex.minecraft.infusedfoods.common.client.screen;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.alchemy.PotionUtils;
import org.joml.Math;
import xyz.apex.minecraft.apexcore.common.lib.PhysicalSide;
import xyz.apex.minecraft.apexcore.common.lib.SideOnly;
import xyz.apex.minecraft.apexcore.common.lib.menu.SimpleContainerMenuScreen;
import xyz.apex.minecraft.infusedfoods.common.InfusedFoods;
import xyz.apex.minecraft.infusedfoods.common.InfusionHelper;
import xyz.apex.minecraft.infusedfoods.common.block.entity.InfusionStationBlockEntity;
import xyz.apex.minecraft.infusedfoods.common.menu.InfusionStationMenu;

import java.util.Collections;
import java.util.Optional;

@SideOnly(PhysicalSide.CLIENT)
public final class InfusionStationMenuScreen extends SimpleContainerMenuScreen<InfusionStationMenu>
{
    public static final ResourceLocation SPRITE_PROGRESS_EMPTY = new ResourceLocation(InfusedFoods.ID, "container/infusion_station/brew_progress_empty");
    public static final ResourceLocation SPRITE_PROGRESS_FILL = new ResourceLocation(InfusedFoods.ID, "container/infusion_station/brew_progress_fill");
    public static final ResourceLocation SPRITE_BUBBLES_EMPTY = new ResourceLocation(InfusedFoods.ID, "container/infusion_station/bubbles_empty");
    public static final ResourceLocation SPRITE_BUBBLES_FILL = new ResourceLocation(InfusedFoods.ID, "container/infusion_station/bubbles_fill");
    public static final ResourceLocation SPRITE_FLUID = new ResourceLocation(InfusedFoods.ID, "container/infusion_station/fluid");
    public static final ResourceLocation SPRITE_FUEL_EMPTY = new ResourceLocation(InfusedFoods.ID, "container/infusion_station/fuel_empty");
    public static final ResourceLocation SPRITE_FUEL_FILL = new ResourceLocation(InfusedFoods.ID, "container/infusion_station/fuel_fill");
    public static final ResourceLocation SPRITE_FUEL_PIPE = new ResourceLocation(InfusedFoods.ID, "container/infusion_station/fuel_pipe");
    public static final ResourceLocation SPRITE_TANK = new ResourceLocation(InfusedFoods.ID, "container/infusion_station/tank");
    public static final ResourceLocation SPRITE_TANK_OVERLAY = new ResourceLocation(InfusedFoods.ID, "container/infusion_station/tank_overlay");
    private static final int TANK_WIDTH = 18;
    private static final int TANK_HEIGHT = 42;
    private static final int FLUID_WIDTH = TANK_WIDTH - 2;
    private static final int FLUID_HEIGHT = 8;
    private static final int FUEL_PIPE_WIDTH = 26;
    private static final int FUEL_PIPE_HEIGHT = 62;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 8;
    private static final int FUEL_WIDTH = 18;
    private static final int FUEL_HEIGHT = 6;
    private static final int BUBBLE_WIDTH = 11;
    private static final int BUBBLE_HEIGHT = 28;
    public static final int TANK_OFFSET = 8;

    public InfusionStationMenuScreen(InfusionStationMenu menu, Inventory playerInventory, Component displayName)
    {
        super(menu, playerInventory, displayName);
    }

    private int tankX;
    private int tankY;

    @Override
    protected void init()
    {
        super.init();

        titleLabelX = imageWidth - font.width(title) - 10;

        tankX = leftPos + TANK_OFFSET;
        tankY = topPos + TANK_OFFSET;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        super.render(graphics, mouseX, mouseY, partialTick);

        renderArrow(graphics);
        renderBubbles(graphics);
        renderFuel(graphics);

        renderInfusionFluid(graphics);
        graphics.blitSprite(SPRITE_TANK_OVERLAY, tankX, tankY, TANK_WIDTH, TANK_HEIGHT);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY)
    {
        super.renderBg(graphics, partialTick, mouseX, mouseY);

        graphics.blitSprite(SPRITE_FUEL_PIPE, leftPos + (imageWidth / 2) - 25, topPos + 15, FUEL_PIPE_WIDTH, FUEL_PIPE_HEIGHT);
        graphics.blitSprite(SPRITE_TANK, tankX - 1, tankY - 1, TANK_WIDTH, TANK_HEIGHT);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY)
    {
        super.renderTooltip(graphics, mouseX, mouseY);

        if(menu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.hasItem())
            return;

        var tankHoverMaxX = tankX + TANK_WIDTH - 2;
        var tankHoverMaxY = tankY + TANK_HEIGHT - 2;

        if(mouseX >= tankX && mouseY >= tankY && mouseX < tankHoverMaxX && mouseY < tankHoverMaxY)
        {
            graphics.fillGradient(tankX, tankY, tankHoverMaxX, tankHoverMaxY, 0x80FFFFFF, 0x80FFFFFF);

            var effect = menu.getEffect();

            if(effect != null)
            {
                var tooltip = Lists.<Component>newArrayList();
                PotionUtils.addPotionTooltip(Collections.singletonList(new MobEffectInstance(effect, menu.getEffectDuration(), menu.getEffectAmplifier())), tooltip, 1F);
                graphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
            }
        }
    }

    private void renderInfusionFluid(GuiGraphics graphics)
    {
        var effect = menu.getEffect();

        if(effect == null)
            return;

        var effectAmount = menu.getEffectAmount();
        var color = InfusionHelper.getEffectColor(effect, menu.getEffectAmplifier());

        var r = FastColor.ARGB32.red(color) / 255F;
        var g = FastColor.ARGB32.green(color) / 255F;
        var b = FastColor.ARGB32.blue(color) / 255F;

        graphics.setColor(r, g, b, 1F);
        graphics.blitSprite(SPRITE_FLUID, tankX, tankY + TANK_HEIGHT - FLUID_HEIGHT * effectAmount - 2, FLUID_WIDTH, FLUID_HEIGHT * effectAmount);
        graphics.setColor(1F, 1F, 1F, 1F);
    }

    private void renderArrow(GuiGraphics graphics)
    {
        var arrowX = leftPos + 109;
        var arrowY = topPos + 26;

        graphics.blitSprite(SPRITE_PROGRESS_EMPTY, arrowX, arrowY, ARROW_WIDTH, ARROW_HEIGHT);

        var infuseTime = menu.getInfuseTime();
        var infuseProgress = infuseTime <= 0 ? 0 : (int) (ARROW_WIDTH * (1F - (float) infuseTime / 400F));

        if(infuseProgress > 0)
            graphics.blitSprite(SPRITE_PROGRESS_FILL, ARROW_WIDTH, ARROW_HEIGHT, 0, 0, arrowX, arrowY, infuseProgress, ARROW_HEIGHT);
    }

    private void renderFuel(GuiGraphics graphics)
    {
        var fuelX = leftPos + 89;
        var fuelY = topPos + 72;

        graphics.blitSprite(SPRITE_FUEL_EMPTY, fuelX, fuelY, FUEL_WIDTH, FUEL_HEIGHT);

        var fuel = menu.getBlazeFuel();
        var fuelProgress = fuel <= 0 ? 0 : Math.clamp(0, FUEL_WIDTH, (FUEL_WIDTH * fuel + InfusionStationBlockEntity.BLAZE_FUEL - 1) / InfusionStationBlockEntity.BLAZE_FUEL);

        if(fuelProgress > 0)
            graphics.blitSprite(SPRITE_FUEL_FILL, FUEL_WIDTH, FUEL_HEIGHT, 0, 0, fuelX, fuelY, fuelProgress, FUEL_HEIGHT);
    }

    private void renderBubbles(GuiGraphics graphics)
    {
        var bubbleX = leftPos + 92;
        var bubbleY = topPos + 43;

        graphics.blitSprite(SPRITE_BUBBLES_EMPTY, bubbleX, bubbleY, BUBBLE_WIDTH, BUBBLE_HEIGHT);

        var infuseTime = menu.getInfuseTime();
        var infuseProgress = infuseTime <= 0 ? 0 : (int) (BUBBLE_HEIGHT * (1F - (float) infuseTime / 400F));

        if(infuseProgress > 0)
            graphics.blitSprite(SPRITE_BUBBLES_FILL, BUBBLE_WIDTH, BUBBLE_HEIGHT, 0, BUBBLE_HEIGHT - infuseProgress, bubbleX, bubbleY + BUBBLE_HEIGHT - infuseProgress, BUBBLE_WIDTH, infuseProgress);
    }
}
