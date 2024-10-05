package im.rarity.functions.impl.movement;

import com.google.common.eventbus.Subscribe;
import im.rarity.events.EventUpdate;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.functions.settings.impl.SliderSetting;

@FunctionRegister(name = "Timer", type = Category.Movement, description = "53")
public class Timer extends Function {

    private final SliderSetting speed = new SliderSetting("Скорость", 2f, 0.1f, 10f, 0.1f);

    public Timer() {
        super("EnhancedStats", Category.Render);
        addSettings(speed);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        mc.timer.timerSpeed = speed.get();
    }

    private void reset() {
        mc.timer.timerSpeed = 1;
    }

    @Override
    public boolean onEnable() {
        super.onEnable();
        reset();
        return false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        reset();
    }
}
