package im.rarity.functions.impl.movement;

import com.google.common.eventbus.Subscribe;
import im.rarity.events.EventMotion;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.functions.settings.impl.ModeSetting;
import im.rarity.functions.settings.impl.SliderSetting;
import im.rarity.utils.math.StopWatch;
import im.rarity.utils.player.MouseUtil;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;

@FunctionRegister(name = "Spider", type = Category.Movement, description = "49")
public class Spider extends Function {
    public ModeSetting mode = new ModeSetting("Mode", "Grim", "Grim", "Matrix");
    private final SliderSetting spiderSpeed = new SliderSetting(
            "Speed",
            2.0f,
            1.0f,
            10.0f,
            0.05f
    ).setVisible(() -> !mode.is("Grim"));

    StopWatch stopWatch = new StopWatch();


    public Spider() {
        super("EnhancedStats", Category.Render);
        addSettings(spiderSpeed, mode);
    }

    @Subscribe
    private void onMotion(EventMotion motion) {


        switch (mode.get()) {
            case "Matrix" -> {
                if (!mc.player.collidedHorizontally) {
                    return;
                }
                long speed = MathHelper.clamp(500 - (spiderSpeed.get().longValue() / 2 * 100), 0, 500);
                if (stopWatch.isReached(speed)) {
                    motion.setOnGround(true);
                    mc.player.setOnGround(true);
                    mc.player.collidedVertically = true;
                    mc.player.collidedHorizontally = true;
                    mc.player.isAirBorne = true;
                    mc.player.jump();
                    stopWatch.reset();
                }
            }
            case "Grim" -> {
                int slotInHotBar = getSlotInInventoryOrHotbar(true);

                if (slotInHotBar == -1) {
                    print("Блоки не найдены!");
                    toggle();
                    return;
                }
                if (!mc.player.collidedHorizontally) {
                    return;
                }
                if (mc.player.isOnGround()) {
                    motion.setOnGround(true);
                    mc.player.setOnGround(true);
                    mc.player.jump();
                }
                if (mc.player.fallDistance > 0 && mc.player.fallDistance < 2) {
                    placeBlocks(motion, slotInHotBar);
                }
            }
        }
    }

    private void placeBlocks(EventMotion motion, int block) {
        int last = mc.player.inventory.currentItem;
        mc.player.inventory.currentItem = block;
        motion.setPitch(80);
        motion.setYaw(mc.player.getHorizontalFacing().getHorizontalAngle());
        BlockRayTraceResult r = (BlockRayTraceResult) MouseUtil.rayTrace(4, motion.getYaw(), motion.getPitch(), mc.player);
        mc.player.swingArm(Hand.MAIN_HAND);
        mc.playerController.processRightClickBlock(mc.player, mc.world, Hand.MAIN_HAND, r);
        mc.player.inventory.currentItem = last;
        mc.player.fallDistance = 0;
    }

    public int getSlotInInventoryOrHotbar(boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;
        int finalSlot = -1;
        for (int i = firstSlot; i < lastSlot; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.TORCH) {
                continue;
            }

            if (mc.player.inventory.getStackInSlot(i).getItem() instanceof BlockItem
                    || mc.player.inventory.getStackInSlot(i).getItem() == Items.WATER_BUCKET) {
                finalSlot = i;
            }
        }

        return finalSlot;
    }
}
