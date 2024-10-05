package im.rarity.functions.impl.misc;

import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.functions.settings.impl.BindSetting;

@FunctionRegister(name = "AutoBuyUI", type = Category.Misc, description = "6")
public class AutoBuyUI extends Function {

    public BindSetting setting = new BindSetting("Кнопка открытия", -1);

    public AutoBuyUI() {
        super("EnhancedStats", Category.Render);
        addSettings(setting);
    }
}
