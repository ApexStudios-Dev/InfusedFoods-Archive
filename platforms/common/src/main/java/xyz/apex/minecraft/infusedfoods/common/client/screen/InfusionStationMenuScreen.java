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
    private static final ResourceLocation TEXTURE = new ResourceLocation(InfusedFoods.ID, "textures/gui/container/infusion_station.png");
    private static final int TANK_WIDTH = 16;
    private static final int TANK_HEIGHT = 40;
    public static final int TANK_OFFSET = 8;

    public InfusionStationMenuScreen(InfusionStationMenu menu, Inventory playerInventory, Component displayName)
    {
        super(menu, playerInventory, displayName, TEXTURE);
    }

    @Override
    protected void init()
    {
        imageWidth = 176;
        imageHeight = 166;

        super.init();

        titleLabelX = imageWidth - font.width(title) - 8;
    }

    @Override
    protected void renderFg(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        renderSlotBackground(graphics, InfusionStationBlockEntity.SLOT_BLAZE, 240, 0);
        renderSlotBackground(graphics, InfusionStationBlockEntity.SLOT_POTION, 240, 16);
        renderSlotBackground(graphics, InfusionStationBlockEntity.SLOT_FOOD, 240, 33);

        renderInfusionFluid(graphics);
        renderInfusionProgress(graphics);
        renderInfusionFluidTankOverlay(graphics, mouseX, mouseY);
    }

    private void renderSlotBackground(GuiGraphics graphics, int slotIndex, int backgroundX, int backgroundY)
    {
        var slot = menu.getContainerSlot(slotIndex);

        if(slot == null || slot.hasItem())
            return;

        graphics.blit(TEXTURE, leftPos + slot.x, topPos + slot.y, backgroundX, backgroundY, 16, 16, 256, 256);
    }

    private void renderInfusionFluidTankOverlay(GuiGraphics graphics, int mouseX, int mouseY)
    {
        var tankX = leftPos + TANK_OFFSET;
        var tankY = topPos + TANK_OFFSET;

        graphics.blit(TEXTURE, tankX, tankY, 176, 41, TANK_WIDTH, TANK_HEIGHT, 256, 256);

        if(mouseX >= tankX && mouseY >= tankY && mouseX < tankX + TANK_WIDTH && mouseY < tankY + TANK_HEIGHT)
        {
            graphics.fillGradient(tankX, tankY, tankX + TANK_WIDTH, tankY + TANK_HEIGHT, 0x80FFFFFF, 0x80FFFFFF);

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

        var tankX = leftPos + TANK_OFFSET;
        var tankY = topPos + TANK_HEIGHT;

        var fluidWidth = TANK_WIDTH;
        var fluidHeight = 8;

        var fluidAmount = menu.getEffectAmount();
        var color = InfusionHelper.getEffectColor(effect, menu.getEffectAmplifier());

        var r = FastColor.ARGB32.red(color) / 255F;
        var g = FastColor.ARGB32.green(color) / 255F;
        var b = FastColor.ARGB32.blue(color) / 255F;

        graphics.setColor(r, g, b, 1F);

        for(var i = 0; i < fluidAmount; i++)
        {
            graphics.blit(TEXTURE, tankX, tankY - (i * fluidHeight), 176, 33, fluidWidth, fluidHeight, 256, 256);
        }

        graphics.setColor(1F, 1F, 1F, 1F);
    }

    private void renderInfusionProgress(GuiGraphics graphics)
    {
        var blazeFuel = menu.getBlazeFuel();
        var infuseTime = menu.getInfuseTime();

        var blazeWidth = Math.clamp(0, 18, (18 * blazeFuel + 20 - 1) / 20);

        if(blazeWidth > 0)
            graphics.blit(TEXTURE, leftPos + 89, topPos + 72, 176, 29, blazeWidth, 4, 256, 256);

        if(infuseTime > 0)
        {
            var j1 = (int) (28F * (1F - (float) infuseTime / 400F));

            if(j1 > 0)
            {
                graphics.blit(TEXTURE, leftPos + 106, topPos + 25, 189, 0, j1, 9, 256, 256);
                graphics.blit(TEXTURE, leftPos + 92, topPos + 42 + 29 - j1, 176, 29 - j1, 13, j1, 256, 256);
            }
        }
    }
}
