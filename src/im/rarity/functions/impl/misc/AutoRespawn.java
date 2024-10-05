package im.rarity.functions.impl.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.gui.screen.DeathScreen;
import im.rarity.events.EventUpdate;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;

@FunctionRegister(name = "AutoRespawn", type = Category.Misc, description = "30")
public class AutoRespawn extends Function {

    public AutoRespawn() {
        super("EnhancedStats", Category.Render);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) return;

        if (mc.currentScreen instanceof DeathScreen && mc.player.deathTime > 5) {
            mc.player.respawnPlayer();
            mc.displayGuiScreen(null);
        }
    }
}
