package im.rarity.functions.impl.render;

import com.google.common.eventbus.Subscribe;
import im.rarity.events.EventCancelOverlay;
import im.rarity.events.EventUpdate;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.functions.settings.impl.BooleanSetting;
import im.rarity.functions.settings.impl.ModeListSetting;
import net.minecraft.potion.Effects;

/* ДОДЕЛАТЬ!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */

@FunctionRegister(name = "NoRender", type = Category.Render, description = "71")
public class NoRender extends Function {

    public ModeListSetting element = new ModeListSetting("Удалять",
            new BooleanSetting("Огонь на экране", true),
            new BooleanSetting("Линия босса", true),
            new BooleanSetting("Анимация тотема", true),
            new BooleanSetting("Тайтлы", true),
            new BooleanSetting("Таблица", true),
            new BooleanSetting("Туман", true),
            new BooleanSetting("Тряску камеры", true),
            new BooleanSetting("Плохие эффекты", true),
            new BooleanSetting("Дождь", true));

    public NoRender() {
        super("EnhancedStats", Category.Render);
        addSettings(element);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        handleEventUpdate(e);
    }

    @Subscribe
    private void onEventCancelOverlay(EventCancelOverlay e) {
        handleEventOverlaysRender(e);
    }

    private void handleEventOverlaysRender(EventCancelOverlay event) {
        boolean cancelOverlay = switch (event.overlayType) {
            case FIRE_OVERLAY -> element.getValueByName("Огонь на экране").get();
            case BOSS_LINE -> element.getValueByName("Линия босса").get();
            case SCOREBOARD -> element.getValueByName("Таблица").get();
            case TITLES -> element.getValueByName("Тайтлы").get();
            case TOTEM -> element.getValueByName("Анимация тотема").get();
            case FOG -> element.getValueByName("Туман").get();
            case HURT -> element.getValueByName("Тряску камеры").get();
        };

        if (cancelOverlay) {
            event.cancel();
        }
    }

    private void handleEventUpdate(EventUpdate event) {
        boolean isRaining = mc.world.isRaining() && element.getValueByName("Дождь").get();

        boolean hasEffects = (mc.player.isPotionActive(Effects.BLINDNESS)
                || mc.player.isPotionActive(Effects.NAUSEA)) && element.getValueByName("Плохие эффекты").get();

        if (isRaining) {
            mc.world.setRainStrength(0);
            mc.world.setThunderStrength(0);
        }

        if (hasEffects) {
            mc.player.removePotionEffect(Effects.NAUSEA);
            mc.player.removePotionEffect(Effects.BLINDNESS);
        }
    }
}
