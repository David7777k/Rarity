package im.rarity.functions.impl.render;

import com.google.common.eventbus.Subscribe;
import im.rarity.events.EventUpdate;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import net.minecraft.entity.player.PlayerEntity;

@FunctionRegister(name = "SeeInvisibles", type = Category.Render, description = "75")
public class SeeInvisibles extends Function {


    public SeeInvisibles() {
        super("EnhancedStats", Category.Render);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player && player.isInvisible()) {
                player.setInvisible(false);
            }
        }
    }

}
