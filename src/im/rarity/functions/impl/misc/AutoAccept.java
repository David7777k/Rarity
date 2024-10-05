package im.rarity.functions.impl.misc;

import com.google.common.eventbus.Subscribe;
import im.rarity.command.friends.FriendStorage;
import im.rarity.events.EventPacket;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.functions.settings.impl.BooleanSetting;
import net.minecraft.network.play.server.SChatPacket;

import java.util.Locale;

@FunctionRegister(name = "AutoAccept", type = Category.Misc, description = "28")
public class AutoAccept extends Function {

    private final BooleanSetting onlyFriend = new BooleanSetting("Только друзья", true);

    public AutoAccept() {
        super("EnhancedStats", Category.Render);
        addSettings(onlyFriend);
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (mc.player == null || mc.world == null) return;

        if (e.getPacket() instanceof SChatPacket p) {
            String raw = p.getChatComponent().getString().toLowerCase(Locale.ROOT);
            if (raw.contains("телепортироваться") || raw.contains("has requested teleport") || raw.contains("просит к вам телепортироваться")) {
                if (onlyFriend.get()) {
                    boolean yes = false;

                    for (String friend : FriendStorage.getFriends()) {
                        if (raw.contains(friend.toLowerCase(Locale.ROOT))) {
                            yes = true;
                            break;
                        }
                    }

                    if (!yes) return;
                }

                mc.player.sendChatMessage("/tpaccept");
                //print("accepted: " + raw);
            }
        }
    }
}
