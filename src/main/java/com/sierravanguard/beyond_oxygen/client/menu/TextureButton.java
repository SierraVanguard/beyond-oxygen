package com.sierravanguard.beyond_oxygen.client.menu;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.GameRenderer;

public class TextureButton extends Button {
    private final ResourceLocation texture;
    private final int texU;
    private final int texV;
    private final int width;
    private final int height;
    private int pressTicks = 0;
    private static final int PRESS_DURATION = 5;
    public TextureButton(int x, int y, int width, int height,
                         int texU, int texV,
                         ResourceLocation texture,
                         Component title,
                         OnPress onPress) {
        super(x, y, width, height, title, onPress, Button.DEFAULT_NARRATION);
        this.texture = texture;
        this.texU = texU;
        this.texV = texV;
        this.width = width;
        this.height = height;
    }
    public void tick() {
        if (pressTicks > 0) pressTicks--;
    }
 
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        graphics.blit(texture, this.getX(), this.getY(), texU, texV, width, height);
        if (this.isHoveredOrFocused() && pressTicks > 0) {
            pressTicks = 5;
            int alpha = 0x44;
            graphics.fill(this.getX(), this.getY(), this.getX() + width, this.getY() + height, alpha << 24);
        }
        tick();
    }
}
