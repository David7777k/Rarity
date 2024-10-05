package im.rarity.functions.impl.player;

import com.google.common.eventbus.Subscribe;
import im.rarity.events.EventKey;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.settings.impl.BindSetting;
import im.rarity.utils.player.PlayerUtils;
import net.minecraft.entity.player.PlayerEntity;
import im.rarity.command.friends.FriendStorage;
import im.rarity.functions.api.FunctionRegister;

@FunctionRegister(name = "ClickFriend", type = Category.Player, description = "57")
public class ClickFriend extends Function {
    final BindSetting throwKey = new BindSetting("Кнопка", -98);
    public ClickFriend() {
        super("EnhancedStats", Category.Render);
        addSettings(throwKey);
    }
    @Subscribe
    public void onKey(EventKey e) {
        if (e.getKey() == throwKey.get() && mc.pointedEntity instanceof PlayerEntity) {

            if (mc.player == null || mc.pointedEntity == null) {
                return;
            }

            String playerName = mc.pointedEntity.getName().getString();

            if (!PlayerUtils.isNameValid(playerName)) {
                print("Невозможно добавить бота в друзья, увы, как бы вам не хотелось это сделать");
                return;
            }

            if (FriendStorage.isFriend(playerName)) {
                FriendStorage.remove(playerName);
                printStatus(playerName, true);
            } else {
                FriendStorage.add(playerName);
                printStatus(playerName, false);
            }
        }
    }

    void printStatus(String name, boolean remove) {
        if (remove) print(name + " удалён из друзей");
        else print(name + " добавлен в друзья");
    }
}
