package im.rarity.functions.impl.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.play.client.CPlayerPacket;
import im.rarity.events.EventPacket;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;

@FunctionRegister(name = "NoRotate", type = Category.Player, description = "63")
public class NoRotate extends Function {
    private float targetYaw;
    private float targetPitch;
    private boolean isPacketSent;

    public NoRotate() {
        super("EnhancedStats", Category.Render);
    }

    @Subscribe
    public void onPacket(EventPacket event) {
        if (event.isSend()) {
            if (this.isPacketSent) {
                if (event.getPacket() instanceof CPlayerPacket playerPacket) {
                    playerPacket.setRotation(targetYaw, targetPitch);
                    this.isPacketSent = false;
                }
            }
        }
    }

    public void sendRotationPacket(float yaw, float pitch) {
        this.targetYaw = yaw;
        this.targetPitch = pitch;
        this.isPacketSent = true;
    }
}
