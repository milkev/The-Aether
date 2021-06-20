package com.aether.client.gui;

import com.aether.Aether;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class BookOfLoreScreen extends Screen {

    public static final Identifier PAGE_TEXTURE_LEFT = Aether.locate("textures/gui/lore_book_l.png");
    public static final Identifier PAGE_TEXTURE_RIGHT = Aether.locate("textures/gui/lore_book_r.png");

    private final Style unicodeStyle;
    private PageType currentPage = PageType.Info;
    private Text landingTitle, landingSubtitle, landingCategories;
    private Text infoMobs, infoItems, infoBlocks;

    public BookOfLoreScreen() {
        super(NarratorManager.EMPTY);
        unicodeStyle = Style.EMPTY.withFont(MinecraftClient.UNICODE_FONT_ID);
    }

    @Override
    public void init(MinecraftClient client, int width, int height) {
        landingTitle = new TranslatableText("item.the_aether.lore_book").setStyle(Style.EMPTY.withBold(true));
        landingSubtitle = new TranslatableText("book.edition", "1").setStyle(unicodeStyle);
        landingCategories = new TranslatableText("book.categories").setStyle(Style.EMPTY.withBold(true));
        infoMobs = new TranslatableText("book.mobs").setStyle(Style.EMPTY.withBold(true));
        infoItems = new TranslatableText("book.items").setStyle(Style.EMPTY.withBold(true));
        infoBlocks = new TranslatableText("book.blocks").setStyle(Style.EMPTY.withBold(true));

        super.init(client, width, height);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        final int w = this.width / 2;
        final int h = this.height / 2;

        if (currentPage == PageType.Landing)
            renderLandingPage(matrices, w, h);
        else
            renderInfoPage(matrices, w, h, mouseX, mouseY, delta);

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void renderLandingPage(MatrixStack matrices, int centerX, int centerY) {
        this.client.getTextureManager().bindTexture(PAGE_TEXTURE_LEFT);
        this.drawTexture(matrices, centerX - 136, centerY - 90, 0, 0, 136, 180);

        this.client.getTextureManager().bindTexture(PAGE_TEXTURE_RIGHT);
        this.drawTexture(matrices, centerX, centerY - 90, 0, 0, 136, 180);

        matrices.push();
        matrices.scale(1.2f, 1.2f, 1.2f);
        this.textRenderer.draw(matrices, landingTitle, (centerX * .8334f) - 95.84f, (centerY * .8334f) - 60f, 0);
        matrices.pop();
        this.textRenderer.draw(matrices, landingSubtitle, centerX - 115, centerY - 60, 0);

        this.textRenderer.draw(matrices, landingCategories, centerX + (textRenderer.getWidth(landingCategories) * .5f), centerY - 71f, 0);
    }

    private void renderInfoPage(MatrixStack matrices, int centerX, int centerY, int mouseX, int mouseY, float delta) {
        this.client.getTextureManager().bindTexture(PAGE_TEXTURE_LEFT);
        this.drawTexture(matrices, centerX - 136, centerY - 90, 0, 0, 136, 180);

        this.textRenderer.draw(matrices, infoBlocks, centerX - (textRenderer.getWidth(infoBlocks) * .5f), centerY - 71f, 0);
    }
    enum PageType {
        Landing, Info
    }
}
