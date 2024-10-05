package im.rarity.functions.impl.player;

import com.google.common.eventbus.Subscribe;
import im.rarity.events.EventPacket;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import net.minecraft.network.play.client.CConfirmTeleportPacket;

@FunctionRegister(name = "PortalGodMode", type = Category.Player, description = "8")
public class PortalGodMode extends Function {

    public PortalGodMode() {
        super("EnhancedStats", Category.Render);
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof CConfirmTeleportPacket) {
            e.cancel();
        }
    }
}
