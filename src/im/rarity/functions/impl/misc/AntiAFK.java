package im.rarity.functions.impl.misc;

import com.google.common.eventbus.Subscribe;
import im.rarity.events.EventUpdate;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;

import java.util.concurrent.ThreadLocalRandom;

@FunctionRegister(name = "AntiAFK", type = Category.Player, description = "26")
public class AntiAFK extends Function {

    public AntiAFK() {
        super("EnhancedStats", Category.Render);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (mc.player.ticksExisted % 200 != 0) return;

        if (mc.player.isOnGround()) mc.player.jump();
        mc.player.rotationYaw += ThreadLocalRandom.current().nextFloat(-10, 10);
    }
}
