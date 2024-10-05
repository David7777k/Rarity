package im.rarity.functions.impl.player;

import com.google.common.eventbus.Subscribe;
import im.rarity.events.EventUpdate;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;

@FunctionRegister(name = "NoJumpDelay", type = Category.Player, description = "62")
public class NoJumpDelay extends Function {
    public NoJumpDelay() {
        super("EnhancedStats", Category.Render);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        mc.player.jumpTicks = 0;
    }
}
