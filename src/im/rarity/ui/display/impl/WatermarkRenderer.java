package im.rarity.ui.display.impl;

import com.mojang.blaze3d.matrix.MatrixStack;

import im.rarity.Rarity;
import net.minecraft.util.text.ITextComponent;
import im.rarity.events.EventDisplay;
import im.rarity.ui.display.ElementRenderer;
import im.rarity.ui.styles.Style;
import im.rarity.utils.render.ColorUtils;
import im.rarity.utils.render.DisplayUtils;
import im.rarity.utils.render.font.Fonts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.ResourceLocation;
import im.rarity.utils.client.ClientUtil;
import im.rarity.utils.text.GradientUtil;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class WatermarkRenderer implements ElementRenderer {

    @Override
    public void render(EventDisplay eventDisplay) {
        final ITextComponent name = GradientUtil.gradient("B");
        final ITextComponent name2 = GradientUtil.gradient("W");
        final ITextComponent name3 = GradientUtil.gradient("A");
        final ITextComponent name4 = GradientUtil.gradient("S");
        final ITextComponent name10 = GradientUtil.gradient("" + mc.player.getName().getString());
        final ITextComponent name20 = GradientUtil.gradient("W");
        final ITextComponent name30 = GradientUtil.gradient("A");
        final ITextComponent name40 = GradientUtil.gradient("S");
        MatrixStack ms = eventDisplay.getMatrixStack();

        Style style = Rarity.getInstance().getStyleManager().getCurrentStyle();

        float posY = 4;
        float fontSize = 6.5f;
        float iconSizeX = 10;
        float iconSizeY = 10;
        float spacing = 0.02f;
        float additionalSpacing = 5;

        int windowWidth = ClientUtil.calc(mc.getMainWindow().getScaledWidth());


        String username = "" + mc.player.getName().getString();
        String SERVER = mc.getCurrentServerData() != null ? mc.getCurrentServerData().serverIP : "Singleplayer";
        String FPS = mc.debugFPS + "fps";

        float usernameWidth = Fonts.sfui.getWidth(username, fontSize);
        float serverWidth = Fonts.sfui.getWidth(SERVER, fontSize);
        float fpsWidth = Fonts.sfui.getWidth(FPS, fontSize);

        float iconSpacing = 2.0f;

        float usernameIconWidth = Fonts.icons.getWidth("A", fontSize);
        float serverIconWidth = Fonts.icons.getWidth("B", fontSize);
        float fpsIconWidth = Fonts.icons.getWidth("C", fontSize);

        float totalTextWidth = usernameWidth + usernameIconWidth + serverWidth + serverIconWidth + fpsWidth
                + fpsIconWidth + 3 * spacing + 3 * additionalSpacing;
        float rectWidth = 15 + totalTextWidth + iconSizeX + 5;

        float posX = (windowWidth - rectWidth) / 2;

        final ResourceLocation logo = new ResourceLocation("rarity/images/hud/logo.png");

        float rectWidthIco = 15;
        float rectHeightIco = 15;

        float centerX = posX + rectWidthIco / 2;
        float centerY = posY + 5 + rectHeightIco / 2;

        float finalPosXIcon = centerX - iconSizeX / 2;
        float finalPosYIcon = centerY - iconSizeY / 2;




        drawStyledRect(posX, posY + 5, rectWidthIco, rectHeightIco, 3);




        Fonts.icons.drawCenteredText(ms, name, finalPosXIcon + 5f, finalPosYIcon + 1f,
                 fontSize + 2);


        drawStyledRect(posX + rectWidthIco + 2.5f, posY + 5, rectWidth - rectWidthIco - 5, rectHeightIco, 3);

        float textPosX = posX + rectWidthIco + 7.5f;
        float textPosY = posY + 9.5f;


        Fonts.icons.drawCenteredText(ms, name2, textPosX + 2, textPosY + 0.5f, 6.5F);

        textPosX += usernameIconWidth + iconSpacing;
        Fonts.sfui.drawText(ms, username, textPosX, textPosY,ColorUtils.getColor(400), fontSize);

        textPosX += usernameWidth + spacing + additionalSpacing;
        Fonts.icons.drawCenteredText(ms, name3, textPosX + 1, textPosY - 0f, fontSize);
        textPosX += serverIconWidth + iconSpacing;
        Fonts.sfui.drawText(ms, SERVER, textPosX, textPosY, ColorUtils.getColor(400), fontSize);

        textPosX += serverWidth + spacing + additionalSpacing;
        Fonts.icons.drawCenteredText(ms, name4 , textPosX + 2, textPosY + 0.5f, fontSize);
        textPosX += fpsIconWidth + iconSpacing;
        Fonts.sfui.drawText(ms, FPS, textPosX, textPosY, ColorUtils.getColor(400), fontSize);
    }

    private void drawStyledRect(float x, float y, float width, float height, float radius) {
        DisplayUtils.drawRoundedRect(x - 0.5f, y - 0.5f, width + 1, height + 1, radius + 0.5f,
                ColorUtils.setAlpha(ColorUtils.rgb(10, 15, 13), 90));
        DisplayUtils.drawRoundedRect(x, y, width, height, radius, ColorUtils.rgba(10, 15, 13, 150));
        DisplayUtils.drawShadow(x + 5, y + 5, width, height, 5, ColorUtils.rgba(10, 15, 13, 55));
    }
}