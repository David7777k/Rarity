package im.rarity.functions.impl.misc;

import com.google.common.eventbus.Subscribe;
import im.rarity.Rarity;
import im.rarity.events.EventUpdate;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.functions.settings.impl.StringSetting;
import net.minecraft.client.Minecraft;

@FunctionRegister(name = "NameProtect", type = Category.Misc, description = "36")
public class NameProtect extends Function {

    public static String fakeName = "";

    public StringSetting name = new StringSetting(
            "Заменяемое Имя",
            "dedinside",
            "Укажите текст для замены вашего игрового ника"
    );

    public NameProtect() {
        super("EnhancedStats", Category.Render);
        addSettings(name);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        fakeName = name.get();
    }

    public static String getReplaced(String input) {
        if (Rarity.getInstance() != null && Rarity.getInstance().getFunctionRegistry().getNameProtect().isState()) {
            input = input.replace(Minecraft.getInstance().session.getUsername(), fakeName);
        }
        return input;
    }
}
