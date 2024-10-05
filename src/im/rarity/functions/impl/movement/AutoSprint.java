package im.rarity.functions.impl.movement;

import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.functions.settings.impl.BooleanSetting;

@FunctionRegister(name = "AutoSprint", type = Category.Movement, description = "41")
public class AutoSprint extends Function {
    public BooleanSetting saveSprint = new BooleanSetting("Сохранять спринт", true);
    public AutoSprint() {
        super("EnhancedStats", Category.Render);
        addSettings(saveSprint);
    }
}
