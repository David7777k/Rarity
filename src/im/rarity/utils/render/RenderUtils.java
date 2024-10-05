package im.rarity.utils.render;

import com.mojang.blaze3d.matrix.MatrixStack; // Убедитесь, что вы импортируете этот класс
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import static org.lwjgl.opengl.GL11.*;

public class RenderUtils {

    private static final Minecraft mc = Minecraft.getInstance();


    public static void drawRect(int x, int y, int width, int height, int color) {
        int alpha = (color >> 24) & 255;
        int red = (color >> 16) & 255;
        int green = (color >> 8) & 255;
        int blue = color & 255;

        glColor4f(red / 255f, green / 255f, blue / 255f, alpha / 255f);
        glBegin(GL_QUADS);
        glVertex2f(x, y);
        glVertex2f(x + width, y);
        glVertex2f(x + width, y + height);
        glVertex2f(x, y + height);
        glEnd();
    }


    public static void drawString(MatrixStack matrixStack, String text, float x, float y, int color) {
        FontRenderer fontRenderer = mc.fontRenderer;
        fontRenderer.drawString(matrixStack, text, x, y, color);
    }
}
