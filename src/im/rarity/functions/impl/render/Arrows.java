package im.rarity.functions.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import im.rarity.command.friends.FriendStorage;
import im.rarity.events.EventDisplay;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.utils.math.MathUtil;
import im.rarity.utils.player.MoveUtils;
import im.rarity.utils.player.PlayerUtils;
import im.rarity.utils.render.ColorUtils;
import im.rarity.utils.render.DisplayUtils;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

// Перемещаем аннотацию на класс и заполняем корректные значения
@FunctionRegister(name = "Arrows", type = Category.Render, description = "Displays directional arrows towards players")
public class Arrows extends Function {

    public float animationStep;
    private float lastYaw;
    private float lastPitch;
    private float animatedYaw;
    private float animatedPitch;

    // Конструктор класса
    public Arrows() {
        super("Arrows", Category.Render); // Установка имени и категории для функции
    }

    @Subscribe
    public void onDisplay(EventDisplay e) {
        if (mc.player == null || mc.world == null || e.getType() != EventDisplay.Type.PRE) {
            return;
        }

        animatedYaw = MathUtil.fast(animatedYaw, (mc.player.moveStrafing) * 10, 5);
        animatedPitch = MathUtil.fast(animatedPitch, (mc.player.moveForward) * 10, 5);

        float size = 70;

        if (mc.currentScreen instanceof InventoryScreen) {
            size += 80;
        }

        if (MoveUtils.isMoving()) {
            size += 10;
        }
        animationStep = MathUtil.fast(animationStep, size, 6);

        if (mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
            for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
                if (!PlayerUtils.isNameValid(player.getNameClear()) || mc.player == player) {
                    continue;
                }

                // Вычисление позиций игрока
                double x = player.lastTickPosX + (player.getPosX() - player.lastTickPosX) * mc.getRenderPartialTicks()
                        - mc.getRenderManager().info.getProjectedView().getX();
                double z = player.lastTickPosZ + (player.getPosZ() - player.lastTickPosZ) * mc.getRenderPartialTicks()
                        - mc.getRenderManager().info.getProjectedView().getZ();

                // Вычисление поворота камеры
                double cos = MathHelper.cos((float) (mc.getRenderManager().info.getYaw() * (Math.PI * 2 / 360)));
                double sin = MathHelper.sin((float) (mc.getRenderManager().info.getYaw() * (Math.PI * 2 / 360)));
                double rotY = -(z * cos - x * sin);
                double rotX = -(x * cos + z * sin);

                float angle = (float) (Math.atan2(rotY, rotX) * 180 / Math.PI);

                // Вычисление позиций для отображения стрелки
                double x2 = animationStep * MathHelper.cos((float) Math.toRadians(angle)) + window.getScaledWidth() / 2f;
                double y2 = animationStep * MathHelper.sin((float) Math.toRadians(angle)) + window.getScaledHeight() / 2f;

                x2 += animatedYaw;
                y2 += animatedPitch;

                // Начало рендеринга стрелки
                RenderSystem.pushMatrix();
                RenderSystem.disableBlend();
                RenderSystem.translatef((float) x2, (float) y2, 0);
                RenderSystem.rotatef(angle, 0, 0, 1);

                // Определение цвета для друга или обычного игрока
                int color = FriendStorage.isFriend(player.getGameProfile().getName())
                        ? FriendStorage.getColor()
                        : ColorUtils.rgba(255, 255, 255, 255);

                // Рендеринг треугольника
                drawTriangle(-3F, 0F, 3F, 5F, new Color(color));

                RenderSystem.enableBlend();
                RenderSystem.popMatrix();
            }
        }
        lastYaw = mc.player.rotationYaw;
        lastPitch = mc.player.rotationPitch;
    }

    // Метод для рисования треугольника
    public static void drawTriangle(float x, float y, float width, float height, Color color) {
        // Загрузка текстуры треугольника
        DisplayUtils.drawImage(new ResourceLocation("rarity/images/triangle.png"), -8.0F, -9.0F, 18.0F, 18.0F);

        // Дополнительные операции рендеринга (если нужны)
        RenderSystem.pushMatrix();
        // Рендеринг с нужными преобразованиями
        RenderSystem.popMatrix();
    }
}
