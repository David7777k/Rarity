package im.rarity.functions.impl.player;

import com.google.common.eventbus.Subscribe;
import im.rarity.events.EventLivingUpdate;
import im.rarity.events.EventMotion;
import im.rarity.events.EventPacket;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.functions.settings.impl.SliderSetting;
import im.rarity.utils.player.MoveUtils;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.gui.screen.DownloadTerrainScreen;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SConfirmTransactionPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.network.play.server.SRespawnPacket;
import net.minecraft.util.math.vector.Vector3d;

@FunctionRegister(name = "FreeCam", type = Category.Player, description = "12")
public class FreeCam extends Function {

    private static final int FAKE_PLAYER_ID = 1337;

    private final SliderSetting speed = new SliderSetting("Скорость по XZ", 1.0f, 0.1f, 5.0f, 0.05f);
    private final SliderSetting motionY = new SliderSetting("Скорость по Y", 0.5f, 0.1f, 1.0f, 0.05f);

    private Vector3d clientPosition = null;
    private RemoteClientPlayerEntity fakePlayer;

    private boolean returningToPosition = false;
    private long returnStartTime = 0;
    private static final long RETURN_DURATION = 3000; // Время возврата 3 секунды

    // Переменные для хранения последних отправленных данных о движении
    private double lastPosX, lastPosY, lastPosZ;
    private boolean lastOnGround;

    public FreeCam() {
        super("EnhancedStats", Category.Render);
        addSettings(speed, motionY);
    }

    @Subscribe
    public void onLivingUpdate(EventLivingUpdate e) {
        if (mc.player != null) {
            if (!returningToPosition) {
                mc.player.noClip = true;
                mc.player.setOnGround(false);
                MoveUtils.setMotion(speed.get());

                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.player.setMotion(mc.player.getMotion().x, motionY.get(), mc.player.getMotion().z);
                }
                if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    mc.player.setMotion(mc.player.getMotion().x, -motionY.get(), mc.player.getMotion().z);
                }
                mc.player.abilities.isFlying = true;

                // Постоянно отправляем пакеты о движении с текущей позицией
                sendPlayerMovementPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), mc.player.isOnGround());
            } else {
                handleSmoothReturn();
            }
        }
    }

    @Subscribe
    public void onMotion(EventMotion e) {
        if (mc.player != null && !returningToPosition && mc.player.ticksExisted % 10 == 0) {
            sendPlayerMovementPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), mc.player.isOnGround());
        }
        if (mc.player != null) {
            e.cancel();  // Останавливаем исходное движение игрока
        }
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (mc.player != null && mc.world != null && !(mc.currentScreen instanceof DownloadTerrainScreen)) {
            if (e.isReceive()) {
                if (e.getPacket() instanceof SConfirmTransactionPacket
                        || e.getPacket() instanceof SEntityVelocityPacket sEntityVelocityPacket
                        && sEntityVelocityPacket.getEntityID() == mc.player.getEntityId()) {
                    e.cancel();
                } else if (e.getPacket() instanceof SPlayerPositionLookPacket packet) {
                    if (fakePlayer != null) {
                        fakePlayer.setPosition(packet.getX(), packet.getY(), packet.getZ());
                    }
                    e.cancel();
                }
                if (e.getPacket() instanceof SRespawnPacket) {
                    mc.player.abilities.isFlying = false;
                    if (clientPosition != null) {
                        mc.player.setPositionAndRotation(clientPosition.x, clientPosition.y, clientPosition.z, mc.player.rotationYaw, mc.player.rotationPitch);
                    }
                    removeFakePlayer();
                    mc.player.setMotion(Vector3d.ZERO);
                }
            }
        }
    }

    @Override
    public boolean onEnable() {
        if (mc.player == null) {
            return false;
        }
        clientPosition = mc.player.getPositionVec();
        saveLastPlayerPosition();
        spawnFakePlayer();
        super.onEnable();
        return true;
    }

    @Override
    public void onDisable() {
        if (mc.player == null) {
            return;
        }
        startReturnToPosition();  // Начинаем плавный возврат
        mc.player.abilities.isFlying = false;
        super.onDisable();
    }

    private void startReturnToPosition() {
        returningToPosition = true;
        returnStartTime = System.currentTimeMillis();
    }

    private void handleSmoothReturn() {
        long elapsedTime = System.currentTimeMillis() - returnStartTime;
        if (elapsedTime > RETURN_DURATION) {
            // Возвращаем на исходную позицию по завершению возврата
            mc.player.setPositionAndRotation(clientPosition.x, clientPosition.y, clientPosition.z, mc.player.rotationYaw, mc.player.rotationPitch);
            removeFakePlayer();
            returningToPosition = false;
            mc.player.setMotion(Vector3d.ZERO);

            // Отправляем финальный пакет с положением игрока
            sendPlayerMovementPacket(clientPosition.x, clientPosition.y, clientPosition.z, mc.player.isOnGround());
        } else {
            // Плавное движение к исходной позиции
            double progress = (double) elapsedTime / RETURN_DURATION;
            Vector3d currentPosition = mc.player.getPositionVec();
            Vector3d newPosition = currentPosition.add(clientPosition.subtract(currentPosition).scale(progress));
            mc.player.setPosition(newPosition.x, newPosition.y, newPosition.z);

            // Отправляем пакеты с текущим положением игрока во время возврата
            sendPlayerMovementPacket(newPosition.x, newPosition.y, newPosition.z, mc.player.isOnGround());
        }
    }

    private void spawnFakePlayer() {
        fakePlayer = new RemoteClientPlayerEntity(mc.world, mc.player.getGameProfile());
        fakePlayer.copyLocationAndAnglesFrom(mc.player);
        fakePlayer.rotationYawHead = mc.player.rotationYawHead;
        fakePlayer.renderYawOffset = mc.player.renderYawOffset;
        fakePlayer.rotationPitchHead = mc.player.rotationPitchHead;
        fakePlayer.container = mc.player.container;
        fakePlayer.inventory = mc.player.inventory;
        mc.world.addEntity(FAKE_PLAYER_ID, fakePlayer);
    }

    private void removeFakePlayer() {
        mc.world.removeEntityFromWorld(FAKE_PLAYER_ID);
    }

    private void sendPlayerMovementPacket(double x, double y, double z, boolean onGround) {
        mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(x, y, z, mc.player.rotationYaw, mc.player.rotationPitch, onGround));
        lastPosX = x;
        lastPosY = y;
        lastPosZ = z;
        lastOnGround = onGround;
    }

    private void saveLastPlayerPosition() {
        lastPosX = mc.player.getPosX();
        lastPosY = mc.player.getPosY();
        lastPosZ = mc.player.getPosZ();
        lastOnGround = mc.player.isOnGround();
    }
}
