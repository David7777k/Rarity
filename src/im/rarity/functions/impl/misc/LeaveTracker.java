package im.rarity.functions.impl.misc;

import com.google.common.eventbus.Subscribe;
import im.rarity.events.EventEntityLeave;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;

@FunctionRegister(name = "LeaveTracker", type = Category.Misc, description = "13")
public class LeaveTracker extends Function {


    public LeaveTracker() {
        super("EnhancedStats", Category.Render);
    }

    @Subscribe
    private void onEntityLeave(EventEntityLeave eel) {
        Entity entity = eel.getEntity();

        if (!isEntityValid(entity)) {
            return;
        }

        String message = "Игрок "
                + entity.getDisplayName().getString()
                + " ливнул на "
                + entity.getStringPosition();

        print(message);
    }

    private boolean isEntityValid(Entity entity) {
        if (!(entity instanceof AbstractClientPlayerEntity) || entity instanceof ClientPlayerEntity) {
            return false;
        }

        return !(mc.player.getDistance(entity) < 100);
    }
}
