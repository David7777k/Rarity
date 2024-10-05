package im.rarity.ui.display.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import im.rarity.Rarity;
import im.rarity.events.EventDisplay;
import im.rarity.events.EventUpdate;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.ui.display.ElementRenderer;
import im.rarity.ui.display.ElementUpdater;
import im.rarity.ui.styles.Style;
import im.rarity.utils.math.StopWatch;
import im.rarity.utils.render.ColorUtils;
import im.rarity.utils.render.DisplayUtils;
import im.rarity.utils.render.font.Fonts;
import net.minecraft.util.math.vector.Vector4f;
import ru.hogoshi.Animation;

import java.awt.*;
import java.util.List;

public class ArrayListRenderer implements ElementRenderer, ElementUpdater {


    private void drawStyledRect2(float x, float y, float width, float height, float radius, int alpha) {
        Style style = Rarity.getInstance().getStyleManager().getCurrentStyle();
        DisplayUtils.drawRoundedRect(x, y, width, height, radius, ColorUtils.rgba(0, 0, 0, alpha));
        Color style1 = Rarity.getInstance().getStyleManager().getCurrentStyle().getFirstColor();
        DisplayUtils.drawRoundedRect(x, y, width, height, radius, ColorUtils.rgba(style1.getRed(), style1.getGreen(), style1.getBlue(), alpha - 150));
    }


    private int lastIndex;

    List<Function> list;


    StopWatch stopWatch = new StopWatch();

    @Override
    public void update(EventUpdate e) {
        if (stopWatch.isReached(1000)) {
            list = Rarity.getInstance().getFunctionRegistry().getSorted(Fonts.sfui, 9 - 1.5f)
                    .stream()
                    .filter(m -> m.getCategory() != Category.Render)
                    .filter(m -> m.getCategory() != Category.Misc)
                    .toList();
            stopWatch.reset();
        }
    }

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();
        float rounding = 6;
        float padding = 3.5f;
        float posX = 5;
        float posY = 4 + 28;
        int index = 0;

        if (list == null) return;

        for (Function f : list) {
            float fontSize = 6.5f;
            Animation anim = f.getAnimation();
            float value = (float) anim.getValue();
            String text = f.getName();
            float textWidth = Fonts.sfui.getWidth(text, fontSize);

            if (value != 0) {
                float localFontSize = fontSize * value;
                float localTextWidth = textWidth * value;

                posY += (fontSize + padding * 2) * value;
                index++;
            }
        }
        index = 0;
        posY = 4 + 28;
        for (Function f : list) {
            float fontSize = 6.5f;
            Animation anim = f.getAnimation();
            anim.update();

            float value = (float) anim.getValue();

            String text = f.getName();
            float textWidth = Fonts.sfMedium.getWidth(text, fontSize);

            if (value != 0) {
                float localFontSize = fontSize * value;
                float localTextWidth = textWidth * value;

                boolean isFirst = index == 0;
                boolean isLast = index == lastIndex;

                float localRounding = rounding;

                for (Function f2 : list.subList(list.indexOf(f) + 1, list.size())) {
                    if (f2.getAnimation().getValue() != 0) {
                        localRounding = isLast ? rounding : Math.min(textWidth - Fonts.sfui.getWidth(f2.getName(), fontSize), rounding);
                        break;
                    }
                }

                Vector4f rectVec = new Vector4f(isFirst ? rounding : 0, isLast ? rounding : 0, isFirst ? rounding : 0, isLast ? rounding : localRounding);

                float finalPosY = posY;
                drawStyledRect3(posX - 0.5f, finalPosY - 0.5f,  localTextWidth + padding * 2 + 1, localFontSize + padding * 2 , 3);
                drawStyledRect3(posX, finalPosY, localTextWidth + padding * 2 + 1, localFontSize + padding * 2, 3);



                Style style = Rarity.getInstance().getStyleManager().getCurrentStyle();



                Fonts.sfui.drawText(ms, f.getName(), posX + padding + 2, posY + padding, ColorUtils.rgb(255,255,255), 6f);

                posY += (fontSize + padding * 2) * value;
                index++;



            }
        }

        lastIndex = index - 1;
    }

    private void drawStyledRect3(float x, float y, float width, float height, float radius) {
        DisplayUtils.drawRoundedRect(x - 0.5f, y - 0.5f, width + 1, height + 1, radius + 0.5f,
                ColorUtils.setAlpha(ColorUtils.rgb(10, 15, 13), 90));
        DisplayUtils.drawRoundedRect(x, y, width, height, radius, ColorUtils.rgba(10, 15, 13, 150));
        DisplayUtils.drawShadow(x - 2, y - 2, width + 4, height + 4, 5, ColorUtils.rgba(10, 15, 13, 95));
    }

}
