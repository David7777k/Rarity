package im.rarity.functions.impl.misc;

import com.google.common.eventbus.Subscribe;
import im.rarity.events.EventKey;
import im.rarity.events.WorldEvent;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.ChestMinecartEntity;
import net.minecraft.tileentity.*;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import net.optifine.render.RenderUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@FunctionRegister(name = "BaseFinder", type = Category.Misc, description = "5")
public class BaseFinder extends Function {

    private boolean notifEnabled = true;  // Статус для сообщения о сундуке

    private final Map<TileEntityType<?>, Integer> tiles = new HashMap<>(Map.of(
            new ChestTileEntity().getType(), new Color(0, 187, 8).getRGB() //установка зеленого цвета есп, кто хочет может поменять
    ));

    public BaseFinder() {
        // Удаляем настройку биндов и оставляем только логику
        super("EnhancedStats", Category.Render);
    }

    @Subscribe
    public void onKey(EventKey e) {
        boolean foundChest = false;
        for (TileEntity t : mc.world.loadedTileEntityList) {
            if (t instanceof ChestTileEntity) {
                int x = t.getPos().getX();
                int y = t.getPos().getY();
                int z = t.getPos().getZ();
                double distanceSq = mc.player.getDistanceSq(x, y, z);
                if (distanceSq < 20000.0D) {
                    foundChest = true;
                    if (notifEnabled) {
                        print("" + TextFormatting.GREEN + "Вероятней всего нашлась база");
                    }
                    BlockRayTraceResult rayTraceResult = new BlockRayTraceResult(new Vector3d(mc.player.getPosX(), mc.player.getPosY() - 1.0D, mc.player.getPosZ()), Direction.UP, new BlockPos(x, y, z), false);
                    mc.playerController.processRightClickBlock(mc.player, mc.world, Hand.MAIN_HAND, rayTraceResult);
                }
            }
        }
        if (!foundChest) {
            print("" + TextFormatting.RED + "В этом регионе не нашлось баз");
        }

        // Логика нажатия кнопок для переключения уведомлений
        if (e.getKey() == mc.gameSettings.keyBindTogglePerspective.getKeyCode()) {  // Пример клавиши
            notifEnabled = !notifEnabled;  // Переключаем состояние
            print(notifEnabled ? "Уведомления включены" : "Уведомления выключены");
        }
    }

    @Subscribe
    private void onRender(WorldEvent e) {
        for (TileEntity tile : mc.world.loadedTileEntityList) {
            if (!tiles.containsKey(tile.getType())) continue;

            BlockPos pos = tile.getPos();

            RenderUtils.drawBlockBox(pos, tiles.get(tile.getType()));
        }

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof ChestMinecartEntity) {
                RenderUtils.drawBlockBox(entity.getPosition(), -1);
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
