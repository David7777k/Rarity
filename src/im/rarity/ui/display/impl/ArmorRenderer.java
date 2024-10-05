package im.rarity.ui.display.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import im.rarity.ui.display.ElementRenderer;
import im.rarity.events.EventDisplay;
import im.rarity.utils.drag.Dragging;
import im.rarity.utils.render.ColorUtils;
import im.rarity.utils.render.DisplayUtils;
import im.rarity.utils.render.font.Fonts;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ArmorRenderer implements ElementRenderer {

    @Getter
    final ResourceLocation logo = new ResourceLocation("rarity/images/hud/armor.png");
    final Dragging dragging;

    boolean isHorizontal = true;


    static final float ICON_SIZE = 15;
    static final float PADDING = 6;
    static final float INDICATOR_HEIGHT = 2;

    @Override
    public void render(EventDisplay eventDisplay) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return;
        }

        if (Fonts.sfui == null) {
            return;
        }

        if (dragging == null) {
            return;
        }

        MatrixStack ms = eventDisplay.getMatrixStack();
        float width = isHorizontal ? 90 : 38;
        float height = isHorizontal ? 38 : 90;
        float x = dragging.getX();
        float y = dragging.getY();


        DisplayUtils.drawRoundedRect(x, y, width, height, 5, ColorUtils.rgba(25, 26, 40, 165));


        Fonts.sfui.drawText(ms, "Armor", x + 5, y + 5, -1, 6.5f);


        renderArmor(mc, ms, (int) x + 3, (int) y + 20);


        dragging.setWidth(width);
        dragging.setHeight(height);
    }

    private void renderArmor(Minecraft mc, MatrixStack ms, int posX, int posY) {
        boolean isEmpty = true;


        for (ItemStack itemStack : mc.player.getArmorInventoryList()) {
            if (!itemStack.isEmpty()) {
                isEmpty = false;
                                renderDurabilityBar(itemStack, posX, posY, mc);
                mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, posX, posY);
            } else {
                mc.getItemRenderer().renderItemAndEffectIntoGUI(ItemStack.EMPTY, posX, posY);
            }


            if (isHorizontal) {
                posX += ICON_SIZE + PADDING;
            } else {
                posY += ICON_SIZE + PADDING;
            }
        }


        if (isEmpty) {
            Fonts.sfui.drawText(ms, "No Armor", posX +  -81, posY + 3, ColorUtils.rgb(135, 136, 148), 7.5f);
        }
    }

    private void renderDurabilityBar(ItemStack itemStack, int posX, int posY, Minecraft mc) {
        int damage = itemStack.getDamage();
        int maxDamage = itemStack.getMaxDamage();


        if (maxDamage == 0) return;


        float damagePercentage = (damage * 100.0f) / maxDamage;


        if (damagePercentage > 0) {
            float barWidth = ICON_SIZE;
            DisplayUtils.drawRoundedRect(posX, posY + ICON_SIZE, barWidth, INDICATOR_HEIGHT, 1, ColorUtils.rgb(15, 15, 15));
            DisplayUtils.drawRoundedRect(posX, posY + ICON_SIZE, (barWidth * (100 - damagePercentage)) / 100, INDICATOR_HEIGHT, 1, ColorUtils.rgb(255, 0, 0));
        }
    }


    public void onMouseClick(float mouseX, float mouseY) {
        float x = dragging.getX();
        float y = dragging.getY();


        if (mouseX >= x && mouseX <= x + (isHorizontal ? 90 : 38) && mouseY >= y && mouseY <= y + (isHorizontal ? 38 : 90)) {
            isHorizontal = !isHorizontal;
        }
    }
}
