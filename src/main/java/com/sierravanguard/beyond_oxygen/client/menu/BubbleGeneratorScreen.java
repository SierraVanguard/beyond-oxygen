package com.sierravanguard.beyond_oxygen.client.menu;

import com.sierravanguard.beyond_oxygen.network.BubbleRadiusPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Axis;
import com.sierravanguard.beyond_oxygen.network.NetworkHandler;

public class BubbleGeneratorScreen extends AbstractContainerScreen<BubbleGeneratorMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("beyond_oxygen", "textures/gui/bubble_generator.png");
    private TextureButton plusButton;
    private TextureButton minusButton;
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;

 
    private static final int GAUGE_X = 30;
    private static final int GAUGE_Y = 20;
    private static final int GAUGE_SIZE = 50;
    private static final int GAUGE_TEX_U = 0;
    private static final int GAUGE_TEX_V = 170;

    private static final int NEEDLE_TEX_U = 60;
    private static final int NEEDLE_TEX_V = 170;
    private static final int NEEDLE_WIDTH = 8;
    private static final int NEEDLE_HEIGHT = 20;

 
    private static final int POWER_BAR_X = 151;
    private static final int POWER_BAR_Y = 20;
    private static final int POWER_BAR_WIDTH = 16;
    private static final int POWER_BAR_HEIGHT = 50;

    private static final int POWER_BAR_BG_U = 120;
    private static final int POWER_BAR_BG_V = 170;
    private static final int POWER_BAR_FILL_U = 140;
    private static final int POWER_BAR_FILL_V = 170;

 
    private static final int SLOT_X = 10;
    private static final int SLOT_Y = 80;
    private static final int SLOT_SIZE = 19;

    private static final int BUTTON_PLUS_X = SLOT_X + SLOT_SIZE + 1;
    private static final int BUTTON_PLUS_Y = SLOT_Y;
    private static final int BUTTON_MINUS_X = BUTTON_PLUS_X + SLOT_SIZE + 1;
    private static final int BUTTON_MINUS_Y = SLOT_Y;
    private static final int BUTTON_SIZE = 19;

    private static final int SLOT_TEX_U = 0;
    private static final int SLOT_TEX_V = 220;

    private static final int BUTTON_PLUS_TEX_U = 19;
    private static final int BUTTON_PLUS_TEX_V = 220;

    private static final int BUTTON_MINUS_TEX_U = 38;
    private static final int BUTTON_MINUS_TEX_V = 220;

 
    private static final int STATUS_X = 100;
    private static final int STATUS_Y = 80;
    private static final int STATUS_WIDTH = 70;
    private static final int STATUS_HEIGHT = 32;
    private static final int STATUS_TEX_U = 70;
    private static final int STATUS_TEX_V = 222;

    public BubbleGeneratorScreen(BubbleGeneratorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }
    @Override
    protected void init() {
        super.init();
        plusButton = new TextureButton(
                leftPos + BUTTON_PLUS_X,
                topPos + BUTTON_PLUS_Y,
                BUTTON_SIZE,
                BUTTON_SIZE,
                BUTTON_PLUS_TEX_U,
                BUTTON_PLUS_TEX_V,
                TEXTURE,
                Component.literal("+"),
                btn -> {
                    NetworkHandler.CHANNEL.sendToServer(new BubbleRadiusPacket(menu.getBlockPos(), true));
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                }
        );
        minusButton = new TextureButton(
                leftPos + BUTTON_MINUS_X,
                topPos + BUTTON_MINUS_Y,
                BUTTON_SIZE,
                BUTTON_SIZE,
                BUTTON_MINUS_TEX_U,
                BUTTON_MINUS_TEX_V,
                TEXTURE,
                Component.literal("-"),
                btn -> {
                    NetworkHandler.CHANNEL.sendToServer(new BubbleRadiusPacket(menu.getBlockPos(), false));
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                }
        );
        this.addRenderableWidget(plusButton);
        this.addRenderableWidget(minusButton);
    }
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);

        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT);

        graphics.blit(TEXTURE, leftPos + GAUGE_X, topPos + GAUGE_Y, GAUGE_TEX_U, GAUGE_TEX_V, GAUGE_SIZE, GAUGE_SIZE);
        float oxygenRatio = Math.min((float) menu.getOxygenRatio(), 1.0f);
        float startAngle = 225;
        float sweepAngle = 270;
        float angle = (startAngle + oxygenRatio * sweepAngle) % 360;

        graphics.pose().pushPose();
        int pivotX = leftPos + GAUGE_X + GAUGE_SIZE / 2;
        int pivotY = topPos + GAUGE_Y + GAUGE_SIZE / 2;
        graphics.pose().translate(pivotX, pivotY, 0);
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(angle));

        int needleOffsetX = -NEEDLE_WIDTH / 2 + 1;
        int needleOffsetY = -NEEDLE_HEIGHT + 2;
        graphics.blit(TEXTURE, needleOffsetX, needleOffsetY, NEEDLE_TEX_U, NEEDLE_TEX_V, NEEDLE_WIDTH, NEEDLE_HEIGHT);
        graphics.pose().popPose();
        graphics.blit(TEXTURE, leftPos + POWER_BAR_X, topPos + POWER_BAR_Y, POWER_BAR_BG_U, POWER_BAR_BG_V, POWER_BAR_WIDTH, POWER_BAR_HEIGHT);
        int powerLevel = menu.getPowerLevel();
        int fillHeight = (int) ((powerLevel / 100f) * POWER_BAR_HEIGHT);
        int fillY = POWER_BAR_Y + POWER_BAR_HEIGHT - fillHeight;
        graphics.blit(TEXTURE, leftPos + POWER_BAR_X, topPos + fillY,
                POWER_BAR_FILL_U, POWER_BAR_FILL_V + POWER_BAR_HEIGHT - fillHeight,
                POWER_BAR_WIDTH, fillHeight);
        graphics.blit(TEXTURE, leftPos + SLOT_X, topPos + SLOT_Y, SLOT_TEX_U, SLOT_TEX_V, SLOT_SIZE, SLOT_SIZE);
        graphics.blit(TEXTURE, leftPos + BUTTON_PLUS_X, topPos + BUTTON_PLUS_Y, BUTTON_PLUS_TEX_U, BUTTON_PLUS_TEX_V, BUTTON_SIZE, BUTTON_SIZE);
        graphics.blit(TEXTURE, leftPos + BUTTON_MINUS_X, topPos + BUTTON_MINUS_Y, BUTTON_MINUS_TEX_U, BUTTON_MINUS_TEX_V, BUTTON_SIZE, BUTTON_SIZE);
        graphics.blit(TEXTURE, leftPos + STATUS_X, topPos + STATUS_Y, STATUS_TEX_U, STATUS_TEX_V, STATUS_WIDTH, STATUS_HEIGHT);

        String radiusText = String.format("Radius: %.2f", menu.getCurrentRadius()*1.5);
        int textX = leftPos + STATUS_X + STATUS_WIDTH / 2 - font.width(radiusText) / 2;
        int textY = topPos + STATUS_Y + STATUS_HEIGHT / 2 - font.lineHeight / 2;
        graphics.drawString(font, radiusText, textX, textY, 0x00FF00, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        updateButtons();
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
    public void updateButtons() {
        if (plusButton != null) plusButton.tick();
        if (minusButton != null) minusButton.tick();
    }

}
