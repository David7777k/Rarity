package im.rarity.ui.display.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import im.rarity.Rarity;
import im.rarity.events.EventDisplay;
import im.rarity.functions.api.Function;
import im.rarity.ui.display.ElementRenderer;
import im.rarity.utils.client.KeyStorage;
import im.rarity.utils.drag.Dragging;
import im.rarity.utils.render.ColorUtils;
import im.rarity.utils.render.DisplayUtils;
import im.rarity.utils.render.Scissor;
import im.rarity.utils.render.font.Fonts;
import im.rarity.utils.text.GradientUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class KeyBindRenderer implements ElementRenderer {

    final Dragging dragging;
    final ResourceLocation logo = new ResourceLocation("rarity/images/hud/hotkeys.png");
    float iconSizeX = 10;
    float iconSizeY = 10;


    float width;
    float height;

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();

        float posX = dragging.getX();
        float posY = dragging.getY();
        float fontSize = 6.5f;
        float padding = 5;

        ITextComponent name = GradientUtil.gradient("Hotkeys");
        String namemod = "Hotkeys";

        float finalPosY = posY;
        drawStyledRect(posX, finalPosY, width, height, 5);

        DisplayUtils.drawShadow(posX, posY, width, height, 15, ColorUtils.rgba(21,24,40,165));

        Scissor.push();
        Scissor.setFromComponentCoordinates(posX, posY, width, height);
        Fonts.sfui.drawText(ms, namemod, posX + padding, posY + padding + 1, ColorUtils.rgb(255, 255, 255),fontSize);

        float imagePosX = posX + width - iconSizeX - padding;
        DisplayUtils.drawImage(logo, imagePosX, posY + 3.7f, iconSizeX, iconSizeY, ColorUtils.rgb(129, 135, 255));

        posY += fontSize + padding * 2;

        float maxWidth = Fonts.sfMedium.getWidth(name, fontSize) + padding * 2;
        float localHeight = fontSize + padding * 2;

        for (Function f : Rarity.getInstance().getFunctionRegistry().getFunctions()) {
            f.getAnimation().update();
            if (!(f.getAnimation().getValue() > 0) || f.getBind() == 0) continue;
            String nameText = f.getName();
            float nameWidth = Fonts.sfMedium.getWidth(nameText, fontSize);

            String bindText = KeyStorage.getKey(f.getBind());
            float bindWidth = Fonts.sfMedium.getWidth(bindText, fontSize);


            float localWidth = nameWidth + bindWidth + padding * 3;

            Fonts.sfMedium.drawText(ms, nameText, posX + padding, posY + 0.5f, ColorUtils.rgba(210, 210, 210, (int) (255 * f.getAnimation().getValue())), fontSize);
            Fonts.sfMedium.drawText(ms, bindText, posX + width - padding - bindWidth, posY + 0.5f, ColorUtils.rgba(210, 210, 210, (int) (255 * f.getAnimation().getValue())), fontSize);

            if (localWidth > maxWidth) {
                maxWidth = localWidth;
            }

            posY += (float) ((fontSize + padding) * f.getAnimation().getValue());
            localHeight += (float) ((fontSize + padding) * f.getAnimation().getValue());
        }
        Scissor.unset();
        Scissor.pop();
        width = Math.max(maxWidth, 80);
        height = localHeight + 2.5f;
        dragging.setWidth(width);
        dragging.setHeight(height);
    }

    private void drawStyledRect(float x,
                                float y,
                                float width,
                                float height,
                                float radius) {

        DisplayUtils.drawRoundedRect(x - 0.5f, y - 0.5f, width + 1, height + 1, radius + 0.5f, ColorUtils.setAlpha(ColorUtils.rgb(21,24,40), 90));
        DisplayUtils.drawRoundedRect(x, y, width, height, radius, ColorUtils.rgba(21, 24, 40, 90));
    }
}
