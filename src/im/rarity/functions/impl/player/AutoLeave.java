package im.rarity.functions.impl.player;

import com.google.common.eventbus.Subscribe;
import im.rarity.events.EventUpdate;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.functions.settings.impl.ModeSetting;
import im.rarity.functions.settings.impl.SliderSetting;
import im.rarity.utils.player.PlayerUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;

@FieldDefaults(level = AccessLevel.PRIVATE)
@FunctionRegister(name = "AutoLeave", type = Category.Player, description = "10")
public class AutoLeave extends Function {

    final ModeSetting action = new ModeSetting("Действие", "Kick", "Kick", "/hub", "/spawn", "/home");
    final SliderSetting distance = new SliderSetting("Дистанция", 50.0f, 1.0f, 100.0f, 1.0f);

    public AutoLeave() {
        super("EnhancedStats", Category.Render);
        addSettings(action, distance);
    }
    @Subscribe
    private void onUpdate(EventUpdate event) {
        mc.world.getPlayers().stream()
                .filter(this::isValidPlayer)
                .findFirst()
                .ifPresent(this::performAction);
    }

    private boolean isValidPlayer(PlayerEntity player) {
        return player.isAlive()
                && player.getHealth() > 0.0f
                && player.getDistance(mc.player) <= distance.get()
                && player != mc.player
                && PlayerUtils.isNameValid(player.getName().getString());
    }

    private void performAction(PlayerEntity player) {
        if (!action.get().equalsIgnoreCase("Kick")) {
            mc.player.sendChatMessage(action.get());
            mc.ingameGUI.func_238452_a_(new StringTextComponent("[AutoLeave] " + player.getGameProfile().getName()),
                    new StringTextComponent("test"), -1,
                    -1, -1);
        } else {
            mc.player.connection.getNetworkManager().closeChannel(new StringTextComponent("Вы вышли с сервера! \n" + player.getGameProfile().getName()));
        }
        toggle();
    }
}
