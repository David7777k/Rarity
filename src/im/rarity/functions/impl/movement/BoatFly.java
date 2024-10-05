package im.rarity.functions.impl.movement;

import com.google.common.eventbus.Subscribe;
import im.rarity.events.EventPacket;
import im.rarity.events.EventUpdate;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.functions.settings.impl.BooleanSetting;
import im.rarity.functions.settings.impl.SliderSetting;
import im.rarity.utils.player.MoveUtils;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.network.play.client.CEntityActionPacket;

@FunctionRegister(name = "Pig Fly", type = Category.Movement, description = "42")
public class BoatFly extends Function {
    final SliderSetting speed = new SliderSetting("Скорость", 10.f, 1.f, 20.f, 0.05f);
    final BooleanSetting noDismount = new BooleanSetting("Не вылезать", true);
    final BooleanSetting savePig = new BooleanSetting("Спасать свинью", true);


    public BoatFly() {
        super("EnhancedStats", Category.Render);
        addSettings(speed, noDismount, savePig);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (mc.player.getRidingEntity() != null) {
            if (mc.player.getRidingEntity() instanceof PigEntity) {

                mc.player.getRidingEntity().motion.y = 0;
                if (mc.player.isPassenger()) {
                    if (mc.gameSettings.keyBindJump.isKeyDown()) {
                        mc.player.getRidingEntity().motion.y = 1;
                    } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                        mc.player.getRidingEntity().motion.y = -1;
                    }


                    if (MoveUtils.isMoving()) {
                        final double yaw = MoveUtils.getDirection(true);

                        mc.player.getRidingEntity().motion.x = -Math.sin(yaw) * speed.get();
                        mc.player.getRidingEntity().motion.z = Math.cos(yaw) * speed.get();
                    } else {
                        mc.player.getRidingEntity().motion.x = 0;
                        mc.player.getRidingEntity().motion.z = 0;
                    }
                    if ((!MoveUtils.isBlockUnder(4f) || mc.player.collidedHorizontally || mc.player.collidedVertically) && savePig.get()) {
                        mc.player.getRidingEntity().motion.y += 1;
                    }
                }
            }
        }
    }

    @Subscribe
    private void onPacket(EventPacket e) {


        if (e.getPacket() instanceof CEntityActionPacket actionPacket) {
            if (!noDismount.get() || !(mc.player.getRidingEntity() instanceof BoatEntity)) return;
            CEntityActionPacket.Action action = actionPacket.getAction();
            if (action == CEntityActionPacket.Action.PRESS_SHIFT_KEY || action == CEntityActionPacket.Action.RELEASE_SHIFT_KEY)
                e.cancel();
        }
    }

    public boolean notStopRidding() {
        return this.isState() && noDismount.get();
    }
}
