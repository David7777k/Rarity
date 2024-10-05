package im.rarity.functions.impl.player;

import com.google.common.eventbus.Subscribe;
import im.rarity.events.EventPacket;
import im.rarity.events.EventUpdate;
import im.rarity.events.InventoryCloseEvent;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.utils.client.ClientUtil;
import im.rarity.utils.math.StopWatch;
import im.rarity.utils.player.MoveUtils;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.EditSignScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CClickWindowPacket;

import java.util.ArrayList;
import java.util.List;

@FunctionRegister(name = "InventoryMove", type = Category.Player, description = "59")
public class InventoryMove extends Function {

    private final List<IPacket<?>> packet = new ArrayList<>();

    public InventoryMove() {
        super("EnhancedStats", Category.Render);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player != null) {

            final KeyBinding[] pressedKeys = {mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack,
                    mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump,
                    mc.gameSettings.keyBindSprint};
            if (ClientUtil.isConnectedToServer("funtime")) {
                if (!wait.isReached(400)) {
                    for (KeyBinding keyBinding : pressedKeys) {
                        keyBinding.setPressed(false);
                    }
                    return;
                }
            }


            if (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof EditSignScreen) {
                return;
            }

            updateKeyBindingState(pressedKeys);

        }
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (ClientUtil.isConnectedToServer("funtime")) {
            if (e.getPacket() instanceof CClickWindowPacket p && MoveUtils.isMoving()) {
                if (mc.currentScreen instanceof InventoryScreen) {
                    packet.add(p);
                    e.cancel();
                }
            }
        }
    }

    public StopWatch wait = new StopWatch();

    @Subscribe
    public void onClose(InventoryCloseEvent e) {
        if (ClientUtil.isConnectedToServer("funtime")) {
            if (mc.currentScreen instanceof InventoryScreen && !packet.isEmpty() && MoveUtils.isMoving()) {
                new Thread(() -> {
                    wait.reset();
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    for (IPacket p : packet) {
                        mc.player.connection.sendPacketWithoutEvent(p);
                    }
                    packet.clear();
                }).start();
                e.cancel();
            }
        }
    }

    private void updateKeyBindingState(KeyBinding[] keyBindings) {
        for (KeyBinding keyBinding : keyBindings) {
            boolean isKeyPressed = InputMappings.isKeyDown(mc.getMainWindow().getHandle(), keyBinding.getDefault().getKeyCode());
            keyBinding.setPressed(isKeyPressed);
        }
    }
}
