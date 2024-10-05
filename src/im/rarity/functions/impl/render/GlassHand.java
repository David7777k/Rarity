package im.rarity.functions.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;
import im.rarity.events.EventDisplay;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.utils.CustomFramebuffer;
import im.rarity.utils.render.ColorUtils;
import im.rarity.utils.render.KawaseBlur;
import im.rarity.utils.shader.impl.Outline;
import net.minecraft.client.settings.PointOfView;
import org.lwjgl.opengl.GL11;

@FunctionRegister(name = "Glass Hand", type = Category.Render, description = "68")
public class GlassHand extends Function {

    public CustomFramebuffer hands = new CustomFramebuffer(false).setLinear();
    public CustomFramebuffer mask = new CustomFramebuffer(false).setLinear();

    public GlassHand() {
        super("EnhancedStats", Category.Render);
    }

    @Subscribe
    public void onRender(EventDisplay e) {
        if (e.getType() != EventDisplay.Type.HIGH) {
            return;
        }

        if (mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
            KawaseBlur.blur.updateBlur(3, 4);
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.enableAlphaTest();
            ColorUtils.setColor(ColorUtils.getColor(0));
            KawaseBlur.blur.render(() -> {
                hands.draw();
            });

            Outline.registerRenderCall(() -> {
                hands.draw();
            });


            GlStateManager.disableAlphaTest();
            GlStateManager.popMatrix();
        }
    }

    public static void setSaturation(float saturation) {
        float[] saturationMatrix = {0.3086f * (1.0f - saturation) + saturation, 0.6094f * (1.0f - saturation), 0.0820f * (1.0f - saturation), 0, 0, 0.3086f * (1.0f - saturation), 0.6094f * (1.0f - saturation) + saturation, 0.0820f * (1.0f - saturation), 0, 0, 0.3086f * (1.0f - saturation), 0.6094f * (1.0f - saturation), 0.0820f * (1.0f - saturation) + saturation, 0, 0, 0, 0, 0, 1, 0};
        GL11.glLoadMatrixf(saturationMatrix);
    }
}
