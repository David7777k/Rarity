package im.rarity.functions.impl.render;

import com.google.common.eventbus.Subscribe;
import im.rarity.Rarity;
import im.rarity.events.EventDisplay;
import im.rarity.events.EventUpdate;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.functions.settings.impl.BooleanSetting;
import im.rarity.functions.settings.impl.ModeListSetting;
import im.rarity.ui.display.impl.*;
import im.rarity.ui.styles.StyleManager;
import im.rarity.utils.drag.DragManager;
import im.rarity.utils.drag.Dragging;
import im.rarity.utils.render.ColorUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@FunctionRegister(name = "HUD", type = Category.Render, description = "69")
public class HUD extends Function {

    private final ModeListSetting elements = new ModeListSetting("Элементы",
            new BooleanSetting("Ватермарка", true),
            new BooleanSetting("Список модулей", true),
            new BooleanSetting("Координаты", true),
            new BooleanSetting("Эффекты", true),
            new BooleanSetting("Список модерации", true),
            new BooleanSetting("Активные бинды", true),
            new BooleanSetting("Активный таргет", true),
            new BooleanSetting("Броня", true),
            new BooleanSetting("Хотбар", true),
            new BooleanSetting("Уведомления", true)
    );

    final WatermarkRenderer watermarkRenderer;
    final ArrayListRenderer arrayListRenderer;
    final CoordsRenderer coordsRenderer;
    final PotionRenderer potionRenderer;

    final KeyBindRenderer keyBindRenderer;
    final TargetInfoRenderer targetInfoRenderer;
    final ArmorRenderer armorRenderer;
    final StaffListRenderer staffListRenderer;

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (mc.gameSettings.showDebugInfo) {
            return;
        }

        if (elements.getValueByName("Список модерации").get()) staffListRenderer.update(e);
        if (elements.getValueByName("Список модулей").get()) arrayListRenderer.update(e);
    }

    @Subscribe
    private void onDisplay(EventDisplay e) {
        if (mc.gameSettings.showDebugInfo || e.getType() != EventDisplay.Type.POST) {
            return;
        }

        if (elements.getValueByName("Координаты").get() && coordsRenderer != null) {
            coordsRenderer.render(e);
        }

        if (elements.getValueByName("Броня").get() && armorRenderer != null) {
            armorRenderer.render(e);
        }

        if (elements.getValueByName("Эффекты").get() && potionRenderer != null) {
            potionRenderer.render(e);
        }

        if (elements.getValueByName("Ватермарка").get() && watermarkRenderer != null) {
            watermarkRenderer.render(e);
        }

        if (elements.getValueByName("Список модулей").get() && arrayListRenderer != null) {
            arrayListRenderer.render(e);
        }

        if (elements.getValueByName("Активные бинды").get() && keyBindRenderer != null) {
            keyBindRenderer.render(e);
        }

        if (elements.getValueByName("Список модерации").get() && staffListRenderer != null) {
            staffListRenderer.render(e);
        }

        if (elements.getValueByName("Активный таргет").get() && targetInfoRenderer != null) {
            targetInfoRenderer.render(e);
        }
    }

    public HUD() {
        super("EnhancedStats", Category.Render);
        watermarkRenderer = new WatermarkRenderer();
        arrayListRenderer = new ArrayListRenderer();
        coordsRenderer = new CoordsRenderer();
        Dragging potions = Rarity.getInstance().createDrag(this, "Potions", 278, 5);
        Dragging armor = Rarity.getInstance().createDrag(this,"ArmorHUD", 279,5);
        armorRenderer = new ArmorRenderer(armor);
        Dragging keyBinds = Rarity.getInstance().createDrag(this, "KeyBinds", 185, 5);
        Dragging dragging = Rarity.getInstance().createDrag(this, "TargetHUD", 74, 128);
        Dragging staffList = Rarity.getInstance().createDrag(this, "StaffList", 96, 5);
        potionRenderer = new PotionRenderer(potions);
        keyBindRenderer = new KeyBindRenderer(keyBinds);
        staffListRenderer = new StaffListRenderer(staffList);
        targetInfoRenderer = new TargetInfoRenderer(dragging);
        addSettings(elements);
        DragManager.load();
    }

    public static int getColor(int index) {
        StyleManager styleManager = Rarity.getInstance().getStyleManager();
        return ColorUtils.gradient(styleManager.getCurrentStyle().getFirstColor().getRGB(), styleManager.getCurrentStyle().getSecondColor().getRGB(), index * 16, 10);
    }

    public static int getColor(int index, float mult) {
        StyleManager styleManager = Rarity.getInstance().getStyleManager();
        return ColorUtils.gradient(styleManager.getCurrentStyle().getFirstColor().getRGB(), styleManager.getCurrentStyle().getSecondColor().getRGB(), (int) (index * mult), 10);
    }

    public static int getColor(int firstColor, int secondColor, int index, float mult) {
        return ColorUtils.gradient(firstColor, secondColor, (int) (index * mult), 10);
    }
}