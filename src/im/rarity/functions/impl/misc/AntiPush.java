package im.rarity.functions.impl.misc;

import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.functions.settings.impl.BooleanSetting;
import im.rarity.functions.settings.impl.ModeListSetting;
import lombok.Getter;

@Getter
@FunctionRegister(name = "AntiPush", type = Category.Player, description = "27")
public class AntiPush extends Function {

    private final ModeListSetting modes = new ModeListSetting("Тип",
            new BooleanSetting("Игроки", true),
            new BooleanSetting("Вода", false),
            new BooleanSetting("Блоки", true));

    public AntiPush() {
        super("EnhancedStats", Category.Render);
        addSettings(modes);
    }

}
