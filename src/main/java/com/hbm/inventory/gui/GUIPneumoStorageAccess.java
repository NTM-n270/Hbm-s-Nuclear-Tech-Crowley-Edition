package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.inventory.container.ContainerPneumoStorageAccess;
import com.hbm.inventory.gui.element.GUIElements;
import com.hbm.tileentity.network.TileEntityPneumoStorageAccess;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class GUIPneumoStorageAccess extends GuiInfoContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID + ":textures/gui/storage/gui_pneumatic_access.png");

    protected final TileEntityPneumoStorageAccess access;

    public GUIPneumoStorageAccess(InventoryPlayer invPlayer, TileEntityPneumoStorageAccess access) {
        super(new ContainerPneumoStorageAccess(invPlayer, access));
        this.access = access;
        this.xSize = 176;
        this.ySize = 251;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String name = I18nUtil.resolveKey("container.pneumoStorageAccess");
        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 5, 4210752);
        this.fontRenderer.drawString(I18nUtil.resolveKey("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void renderToolTip(ItemStack stack, int x, int y) {
        List<String> list = stack.getTooltip(this.mc.player, this.mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);

        for(int line = 0; line < list.size(); ++line) {
            if(line == 0) {
                list.set(line, stack.getRarity().color + list.get(line));
            } else {
                list.set(line, TextFormatting.GRAY + list.get(line));
            }
        }

        FontRenderer font = stack.getItem().getFontRenderer(stack);
        if(font == null) font = this.fontRenderer;
        GUIElements.drawHoveringText(list, x, y, font, itemRender, width, height, GUIElements.STANDARD_HEADER_OFFSET, GUIElements.STANDARD_LINE_DIST, GUIElements.STANDARD_COLOR_BACKGROUND, GUIElements.STANDARD_COLOR_BACKGROUND, 0xD57C4F, 0xAB4223);
    }
}
