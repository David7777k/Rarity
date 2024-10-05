package im.rarity.functions.impl.misc;

import com.google.common.eventbus.Subscribe;
import im.rarity.events.EventPacket;
import im.rarity.events.EventUpdate;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.functions.settings.impl.StringSetting;
import im.rarity.utils.client.ClientUtil;
import im.rarity.utils.math.StopWatch;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

@FunctionRegister(name = "AutoTransfer", type = Category.Misc, description = "31")
public class AutoTransfer extends Function {

    // Настройки
    private final StringSetting anarchyNumberSetting = new StringSetting("Анархия", "", "Введите номер анархии", true);
    private final StringSetting itemsCountSetting = new StringSetting("Количество предметов", "", "Количество", true);
    private final StringSetting sellPriceSetting = new StringSetting("Цена", "", "Укажите цену (от 10$)", true);

    // Переменные для таймеров и состояния
    private final StopWatch stopWatch = new StopWatch();
    private final StopWatch changeServerStopWatch = new StopWatch();
    private boolean allItemsToSell = false;
    private boolean connectedToServer = false;
    private final List<Item> playerItems = new ArrayList<>();
    private int sellCount = 0;
    private boolean isReadyToSell;

    public AutoTransfer() {
        // Добавление настроек
        super("EnhancedStats", Category.Render);
        addSettings(anarchyNumberSetting, itemsCountSetting, sellPriceSetting);
    }

    @Subscribe
    private void onPacket(EventPacket packetEvent) {
        // Проверяем, что игрок подключен к нужному серверу
        if (!ClientUtil.isConnectedToServer("funtime")) return;

        // Обработка чата
        if (packetEvent.getPacket() instanceof SChatPacket chatPacket) {
            String chatMessage = chatPacket.getChatComponent().getString().toLowerCase(Locale.ROOT);
            if (chatMessage.contains("освободите хранилище") && !playerItems.isEmpty()) allItemsToSell = true;
            if (chatMessage.contains("вы уже подключены")) connectedToServer = true;
            if (chatMessage.contains("выставлен на продажу")) {
                sellCount++;
            }
        }
    }

    List<ItemStack> stacks = new LinkedList<>();

    @Subscribe
    private void onUpdate(EventUpdate updateEvent) {
        // Проверка задержки перед началом работы
        if (mc.player.ticksExisted < 500 && !isReadyToSell) {
            int ticksRemaining = 500 - mc.player.ticksExisted;
            int secondsRemaining = ticksRemaining / 20;
            print("Пожалуйста, подождите еще " + TextFormatting.RED + secondsRemaining + TextFormatting.RESET + " секунд(ы) перед началом использования.");
            toggle();
            return;
        }

        // Проверка подключенного сервера
        if (mc.ingameGUI.getTabList().header != null) {
            String serverHeader = TextFormatting.getTextWithoutFormattingCodes(mc.ingameGUI.getTabList().header.getString());
            if (serverHeader != null && serverHeader.contains(anarchyNumberSetting.get())) connectedToServer = true;
        }

        // Проверка количества предметов
        int itemCountToSell;
        try {
            itemCountToSell = Integer.parseInt(itemsCountSetting.get());
        } catch (NumberFormatException e) {
            print("Ошибка: введите корректное количество предметов.");
            toggle();
            return;
        }

        if (itemCountToSell > 9) {
            print("Ошибка: количество предметов не должно превышать 9.");
            toggle();
            return;
        }

        // Проверка цены продажи
        int sellPrice;
        try {
            sellPrice = Integer.parseInt(sellPriceSetting.get());
        } catch (NumberFormatException e) {
            print("Ошибка: введите корректную цену.");
            toggle();
            return;
        }

        if (sellPrice < 10) {
            print("Ошибка: цена должна быть не менее 10$.");
            toggle();
            return;
        }

        // Продажа предметов
        if (!isReadyToSell) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.inventory.getStackInSlot(i);
                if (stack.getItem() == Items.AIR) continue;
                if (stopWatch.isReached(100)) {
                    mc.player.inventory.currentItem = i;
                    mc.player.sendChatMessage("/ah dsell " + sellPrice);
                    playerItems.add(stack.getItem());
                    stopWatch.reset();
                }
            }
        }

        // Обработка продажи предметов
        if (sellCount >= itemCountToSell || allItemsToSell) {
            isReadyToSell = true;
            int anarchyNumber = Integer.parseInt(anarchyNumberSetting.get());

            // Подключение к серверу анархии
            if (!connectedToServer) {
                if (changeServerStopWatch.isReached(100)) {
                    mc.player.sendChatMessage("/an" + anarchyNumber);
                    changeServerStopWatch.reset();
                }
                return;
            }

            // Продажа предметов из сундука
            if (mc.player.openContainer instanceof ChestContainer container) {
                IInventory lowerChestInventory = container.getLowerChestInventory();

                for (int index = 0; index < lowerChestInventory.getSizeInventory(); ++index) {
                    if (stopWatch.isReached(200) && lowerChestInventory.getStackInSlot(index).getItem() != Items.AIR) {
                        if (playerItems.contains(lowerChestInventory.getStackInSlot(index).getItem())) {
                            mc.playerController.windowClick(container.windowId, index, 0, ClickType.QUICK_MOVE, mc.player);
                            stopWatch.reset();
                        } else {
                            resetAndToggle();
                            toggle();
                        }
                    }
                }
            } else {
                if (stopWatch.isReached(500)) {
                    mc.player.sendChatMessage("/ah " + mc.player.getNameClear());
                    stopWatch.reset();
                }
            }
        }
    }

    @Override
    public void onDisable() {
        resetAndToggle();
        super.onDisable();
    }

    private void resetAndToggle() {
        allItemsToSell = false;
        connectedToServer = false;
        playerItems.clear();
        isReadyToSell = false;
        sellCount = 0;
    }
}
