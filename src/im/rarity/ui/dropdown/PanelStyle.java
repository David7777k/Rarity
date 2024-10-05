package im.rarity.ui.dropdown;

import com.mojang.blaze3d.matrix.MatrixStack;
import im.rarity.Rarity;
import im.rarity.functions.api.Category;
import im.rarity.ui.styles.Style;
import im.rarity.utils.math.MathUtil;
import im.rarity.utils.math.Vector4i;
import im.rarity.utils.render.*;
import im.rarity.utils.render.font.Fonts;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.glfw.GLFW;

@Getter
public class PanelStyle extends Panel {

    public PanelStyle(Category category) {
        super(category);

    }

    float max = 0;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        float header = 55 / 2f;
        float headerFont = 9;
        setAnimatedScrool(MathUtil.fast(getAnimatedScrool(), getScroll(), 10));

        DisplayUtils.drawRoundedRect(x, y, width, height, 13, ColorUtils.rgba(10, 10, 10, 165));

        DisplayUtils.drawRoundedRect(x + 3.8f, y + 3.5f, width - 8, height - 7, 12,
                ColorUtils.rgba(10, 10, 10, 125));

        DisplayUtils.drawShadow(x + 3.8f, y + 3.5f, width - 8, height - 7, 12,
                ColorUtils.rgba(10, 10, 10, 125));
        Fonts.montserrat.drawCenteredText(stack, "Theme Editor", x + width / 2f,
                y -17 + header / 2f - Fonts.montserrat.getHeight(headerFont) / 2f + 18, ColorUtils.rgb(255, 255, 255),
                headerFont - 1.5f, 0.1f);
        if (max > height - header - 10) {
            setScroll(MathHelper.clamp(getScroll(), -max + height - header - 10, 0));
            setAnimatedScrool(MathHelper.clamp(getAnimatedScrool(), -max + height - header - 10, 0));
        } else {
            setScroll(0);
            setAnimatedScrool(0);
        }
        float animationValue = (float) DropDown.getAnimation().getValue() * DropDown.scale;

        float halfAnimationValueRest = (1 - animationValue) / 2f;
        float height = getHeight();
        float testX = getX() + (getWidth() * halfAnimationValueRest);
        float testY = getY() + 65 / 2f + (height * halfAnimationValueRest);
        float testW = getWidth() * animationValue;
        float testH = height * animationValue;

        testX = testX * animationValue + ((Minecraft.getInstance().getMainWindow().getScaledWidth() - testW) * halfAnimationValueRest);
        Scissor.push();
        Scissor.setFromComponentCoordinates(testX, testY, testW, testH);
        int offset = 0;

        boolean hovered = false;
        for (Style style : Rarity.getInstance().getStyleManager().getStyleList()) {
            float x = this.x + 5;
            float y = this.y + header + 5 + offset * (35 / 2f + 5) + getAnimatedScrool();
            if (MathUtil.isHovered(mouseX, mouseY, x + 5, y + 13, width - 10 - 10, 23 / 2f)) {
                hovered = true;
            }






            y += 1;

            if (Rarity.getInstance().getStyleManager().getCurrentStyle() == style) {
                DisplayUtils.drawShadow(x + 5, y + 13, width - 10 - 10, 23 / 2f, 12, style.getFirstColor().getRGB(), style.getSecondColor().getRGB());
            }
            DisplayUtils.drawRoundedRect(x + 5, y + 13, width - 10 - 10, 23 / 2f, new Vector4f(7, 7, 7, 7), new Vector4i(style.getFirstColor().getRGB(), style.getFirstColor().getRGB(), style.getSecondColor().getRGB(), style.getSecondColor().getRGB()));
            offset++;
        }
        if (MathUtil.isHovered(mouseX, mouseY, x, y, width, height)) {
            if (hovered) {
                GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.HAND);
            } else {
                GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
            }
        }
        Scissor.unset();
        Scissor.pop();
        max = offset * Rarity.getInstance().getStyleManager().getStyleList().size() * 2.2f;
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {

    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int button) {
        float header = 55 / 2f;
        int offset = 0;
        for (Style style : Rarity.getInstance().getStyleManager().getStyleList()) {
            float x = this.x + 5;
            float y = this.y + header + 5 + offset * (35 / 2f + 5) + getAnimatedScrool();
            if (MathUtil.isHovered(mouseX, mouseY, x + 5, y + 13, width - 10 - 10, 23 / 2f)) {
                Rarity.getInstance().getStyleManager().setCurrentStyle(style);
            }
            offset++;
        }

    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int button) {

    }

}