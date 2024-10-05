package im.rarity.functions.impl.misc;

import com.google.common.eventbus.Subscribe;
import im.rarity.events.EventPacket;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.server.SHeldItemChangePacket;

@FunctionRegister(name = "ItemSwapFix", type = Category.Misc, description = "35")
public class ItemSwapFix extends Function {

    public ItemSwapFix() {
        super("EnhancedStats", Category.Render);
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        if (mc.player == null) return;
        if (e.getPacket() instanceof SHeldItemChangePacket wrapper) {
            final int serverSlot = wrapper.getHeldItemHotbarIndex();
            if (serverSlot != mc.player.inventory.currentItem) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(Math.max(mc.player.inventory.currentItem - 1, 0)));
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                e.cancel();
            }
        }
    }
}
