package im.rarity.ui.display.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import im.rarity.Rarity;
import im.rarity.events.EventDisplay;
import im.rarity.ui.display.ElementRenderer;
import im.rarity.ui.styles.Style;
import im.rarity.utils.animations.Animation;
import im.rarity.utils.animations.Direction;
import im.rarity.utils.animations.impl.EaseBackIn;
import im.rarity.utils.client.ClientUtil;
import im.rarity.utils.drag.Dragging;
import im.rarity.utils.math.MathUtil;
import im.rarity.utils.math.StopWatch;
import im.rarity.utils.math.Vector4i;
import im.rarity.utils.render.ColorUtils;
import im.rarity.utils.render.DisplayUtils;
import im.rarity.utils.render.Scissor;
import im.rarity.utils.render.Stencil;
import im.rarity.utils.render.font.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class TargetInfoRenderer implements ElementRenderer {
    private final StopWatch stopWatch = new StopWatch();
    private final Dragging drag;
    private LivingEntity entity = null;
    private boolean allow;
    private final Animation animation = new EaseBackIn(400, 1.0, 1.0F);
    private float healthAnimation = 0.0F;
    private float absorptionAnimation = 0.0F;

    public void render(EventDisplay eventDisplay) {
        this.entity = this.getTarget(this.entity);
        float rounding = 6.0F;
        boolean out = !this.allow || this.stopWatch.isReached(1000L);
        this.animation.setDuration(out ? 400 : 300);
        this.animation.setDirection(out ? Direction.BACKWARDS : Direction.FORWARDS);
        if (this.animation.getOutput() == 0.0) {
            this.entity = null;
        }

        if (this.entity != null) {
            String name = this.entity.getName().getString();
            float posX = this.drag.getX();
            float posY = this.drag.getY();
            float headSize = 31.0F;
            float spacing = 5.0F;
            float width = 114.666664F;
            float height = 39.333332F;
            this.drag.setWidth(width);
            this.drag.setHeight(height);
            float shrinking = 1.5F;
            Minecraft var10000 = mc;
            Scoreboard var31 = mc.world.getScoreboard();
            String var10001 = this.entity.getScoreboardName();
            Minecraft var10002 = mc;
            Score score = var31.getOrCreateScore(var10001, mc.world.getScoreboard().getObjectiveInDisplaySlot(2));
            float hp = this.entity.getHealth();
            float maxHp = this.entity.getMaxHealth();
            String header = mc.ingameGUI.getTabList().header == null ? " " : mc.ingameGUI.getTabList().header.getString().toLowerCase();
            if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.contains("funtime") && (header.contains("анархия") || header.contains("гриферский")) && this.entity instanceof PlayerEntity) {
                hp = (float)score.getScorePoints();
                maxHp = 20.0F;
            }

            this.healthAnimation = MathUtil.fast(this.healthAnimation, MathHelper.clamp(hp / maxHp, 0.0F, 1.0F), 10.0F);
            this.absorptionAnimation = MathUtil.fast(this.absorptionAnimation, MathHelper.clamp(this.entity.getAbsorptionAmount() / maxHp, 0.0F, 1.0F), 10.0F);
            if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.contains("funtime") && (header.contains("анархия") || header.contains("гриферский")) && this.entity instanceof PlayerEntity) {
                hp = (float)score.getScorePoints();
                maxHp = 20.0F;
            }

            float animationValue = (float)this.animation.getOutput();
            float halfAnimationValueRest = (1.0F - animationValue) / 2.0F;
            float testX = posX + width * halfAnimationValueRest;
            float testY = posY + height * halfAnimationValueRest;
            float testW = width * animationValue;
            float testH = height * animationValue;
            int windowWidth = ClientUtil.calc(mc.getMainWindow().getScaledWidth());
            GlStateManager.pushMatrix();
            Style style = Rarity.getInstance().getStyleManager().getCurrentStyle();
            sizeAnimation((double)(posX + width / 2.0F), (double)(posY + height / 2.0F), this.animation.getOutput());
            DisplayUtils.drawShadow(posX, posY, 1.0F, 1.0F, 11, Color.BLACK.getRGB());

            drawStyledRect3(posX + 5.0F, posY - 0.5F, width - 15.0F, height - 1.5F, 6);


            Stencil.initStencilToWrite();
            DisplayUtils.drawRoundedRect(posX + 2.5F + 5.5F, posY + 2.5F, headSize + 0.5F, headSize + 0.5F, 6.0F, style.getSecondColor().getRGB());
            Stencil.readStencilBuffer(1);
            this.drawTargetHead(this.entity, posX + 2.5F + 5.5F, posY + 2.5F, headSize + 0.5F, headSize + 0.5F);
            Stencil.uninitStencilBuffer();
            double scale = Math.pow(10.0, 1.0);
            double result = Math.ceil((double)hp * scale) / scale;
            String hpText = String.valueOf(result);
            if (result > 45.0) {
                hpText = "Неизвестно";
            }

            Scissor.push();
            Scissor.setFromComponentCoordinates((double)testX, (double)testY, (double)(testW - 18.85F), (double)testH);
            Fonts.sfui.drawText(eventDisplay.getMatrixStack(), name + "", posX + 42, posY + 5,ColorUtils.rgb(255,255,255), 8.3f);
            Fonts.sfui.drawText(eventDisplay.getMatrixStack(), "HP: " + hpText, posX + 43, posY + 16,ColorUtils.rgb(255,255,255), 6.3f);
            Scissor.unset();
            Scissor.pop();

            Vector4i vector4i = new Vector4i(style.getSecondColor().getRGB(), style.getSecondColor().getRGB(), style.getFirstColor().getRGB(), style.getFirstColor().getRGB());
            Color style1 = Rarity.getInstance().getStyleManager().getCurrentStyle().getFirstColor();
            DisplayUtils.drawRoundedRect(posX + 30.0F + spacing + spacing + 2.5F, posY + height - spacing * 2.0F - 4.0F, (width - 58.0F) * this.healthAnimation, 7.0F, new Vector4f(4.0F, 4.0F, 4.0F, 4.0F), vector4i);
            GlStateManager.popMatrix();
        }

    }

    private LivingEntity getTarget(LivingEntity nullTarget) {
        LivingEntity auraTarget = Rarity.getInstance().getFunctionRegistry().getKillAura().getTarget();
        LivingEntity target = nullTarget;
        if (auraTarget != null) {
            this.stopWatch.reset();
            this.allow = true;
            target = auraTarget;
        } else if (mc.currentScreen instanceof ChatScreen) {
            this.stopWatch.reset();
            this.allow = true;
            Minecraft var10000 = mc;
            target = mc.player;
        } else {
            this.allow = false;
        }

        return (LivingEntity)target;
    }

    public void drawTargetHead(LivingEntity entity, float x, float y, float width, float height) {
        if (entity != null) {
            EntityRenderer<? super LivingEntity> rendererManager = mc.getRenderManager().getRenderer(entity);
            this.drawFace(rendererManager.getEntityTexture(entity), x, y, 8.0F, 8.0F, 8.0F, 8.0F, width, height, 64.0F, 64.0F, entity);
            this.drawFace(rendererManager.getEntityTexture(entity), x, y, 104.0F, 8.0F, 8.0F, 8.0F, width, height, 64.0F, 64.0F, entity);
        }

    }

    public static void sizeAnimation(double width, double height, double scale) {
        GlStateManager.translated(width, height, 0.0);
        GlStateManager.scaled(scale, scale, scale);
        GlStateManager.translated(-width, -height, 0.0);
    }

    public void drawFace(ResourceLocation res, float d, float y, float u, float v, float uWidth, float vHeight, float width, float height, float tileWidth, float tileHeight, LivingEntity target) {
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        mc.getTextureManager().bindTexture(res);
        float hurtPercent = ((float)target.hurtTime - (target.hurtTime != 0 ? mc.timer.renderPartialTicks : 0.0F)) / 10.0F;
        GL11.glColor4f(1.0F, 1.0F - hurtPercent, 1.0F - hurtPercent, 1.0F);
        AbstractGui.drawScaledCustomSizeModalRect(d, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }

    private void drawStyledRect3(float x, float y, float width, float height, float radius) {
        DisplayUtils.drawRoundedRect(x - 0.5f, y - 0.5f, width + 1, height + 1, radius + 0.5f,
                ColorUtils.setAlpha(ColorUtils.rgb(10, 15, 13), 90));
        DisplayUtils.drawRoundedRect(x, y, width, height, radius, ColorUtils.rgba(10, 15, 13, 150));
        DisplayUtils.drawShadow(x - 2, y - 2, width + 4, height + 4, 5, ColorUtils.rgba(10, 15, 13, 95));
    }


    private void drawStyledRect(float x, float y, float width, float height, float radius, int alpha) {
        Style style = Rarity.getInstance().getStyleManager().getCurrentStyle();
        DisplayUtils.drawRoundedRect(x, y, width, height, radius, ColorUtils.rgba(0, 0, 0, alpha));
        Color style1 = Rarity.getInstance().getStyleManager().getCurrentStyle().getFirstColor();
        DisplayUtils.drawRoundedRect(x, y, width, height, radius, ColorUtils.rgba(style1.getRed(), style1.getGreen(), style1.getBlue(), alpha - 150));
    }

    public TargetInfoRenderer(Dragging drag) {
        this.drag = drag;
    }
}
