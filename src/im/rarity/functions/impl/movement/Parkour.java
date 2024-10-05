package im.rarity.functions.impl.movement;

import com.google.common.eventbus.Subscribe;
import im.rarity.events.EventUpdate;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.utils.player.MoveUtils;

@FunctionRegister(name = "Parkour", type = Category.Movement, description = "48")
public class Parkour extends Function {

    public Parkour() {
        super("EnhancedStats", Category.Render);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {

        if (MoveUtils.isBlockUnder(0.001f) && mc.player.isOnGround()) {
            mc.player.jump();
        }
    }

}
