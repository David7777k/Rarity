package im.rarity.functions.impl.player;

import com.google.common.eventbus.Subscribe;
import im.rarity.Rarity;
import im.rarity.events.EventKey;
import im.rarity.events.EventMotion;
import im.rarity.events.EventPacket;
import im.rarity.events.EventUpdate;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.functions.api.FunctionRegistry;
import im.rarity.functions.settings.impl.BindSetting;
import im.rarity.utils.math.StopWatch;
import im.rarity.utils.player.InventoryUtil;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;

@FieldDefaults(level = AccessLevel.PRIVATE)
@FunctionRegister(name = "ClickPearl", type = Category.Player, description = "58")
public class ClickPearl extends Function {
    final BindSetting throwKey = new BindSetting("Кнопка", -98);
    final StopWatch stopWatch = new StopWatch();
    final InventoryUtil.Hand handUtil = new InventoryUtil.Hand();

    final ItemCooldown itemCooldown;
    long delay;
    boolean throwPearl;

    public ClickPearl(ItemCooldown itemCooldown) {
        super("EnhancedStats", Category.Render);
        this.itemCooldown = itemCooldown;
        addSettings(throwKey);
    }

    @Subscribe
    public void onKey(EventKey e) {
        throwPearl = e.getKey() == throwKey.get();
    }

    @Subscribe
    private void onMotion(EventMotion e) {
        if (throwPearl) {
            if (!mc.player.getCooldownTracker().hasCooldown(Items.ENDER_PEARL)) {
                boolean isOffhandEnderPearl = mc.player.getHeldItemOffhand().getItem() instanceof EnderPearlItem;
                if (isOffhandEnderPearl) {
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                    mc.player.swingArm(Hand.MAIN_HAND);
                } else {
                    int slot = findPearlAndThrow();
                    if (slot > 8) {
                        mc.playerController.pickItem(slot);
                    }
                }
            }
            throwPearl = false;
        }
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        this.handUtil.onEventPacket(e);
    }

    private int findPearlAndThrow() {
        int hbSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.ENDER_PEARL, true);
        if (hbSlot != -1) {
            this.handUtil.setOriginalSlot(mc.player.inventory.currentItem);
            if (hbSlot != mc.player.inventory.currentItem) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            }
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.swingArm(Hand.MAIN_HAND);

            FunctionRegistry functionRegistry = Rarity.getInstance().getFunctionRegistry();
            ItemCooldown itemCooldown = functionRegistry.getItemCooldown();
            ItemCooldown.ItemEnum itemEnum = ItemCooldown.ItemEnum.getItemEnum(Items.ENDER_PEARL);

            if (itemCooldown.isState() && itemEnum != null && itemCooldown.isCurrentItem(itemEnum)) {
                itemCooldown.lastUseItemTime.put(itemEnum.getItem(), System.currentTimeMillis());
            }

            if (hbSlot != mc.player.inventory.currentItem) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            }
            this.delay = System.currentTimeMillis();
            return hbSlot;
        }

        int invSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.ENDER_PEARL, false);

        if (invSlot != -1) {
            handUtil.setOriginalSlot(mc.player.inventory.currentItem);
            mc.playerController.pickItem(invSlot);
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.swingArm(Hand.MAIN_HAND);

            FunctionRegistry functionRegistry = Rarity.getInstance().getFunctionRegistry();
            ItemCooldown itemCooldown = functionRegistry.getItemCooldown();
            ItemCooldown.ItemEnum itemEnum = ItemCooldown.ItemEnum.getItemEnum(Items.ENDER_PEARL);

            if (itemCooldown.isState() && itemEnum != null && itemCooldown.isCurrentItem(itemEnum)) {
                itemCooldown.lastUseItemTime.put(itemEnum.getItem(), System.currentTimeMillis());
            }
            this.delay = System.currentTimeMillis();
            return invSlot;
        }
        return -1;
    }


    @Override
    public void onDisable() {
        throwPearl = false;
        delay = 0;
        super.onDisable();
    }
}
