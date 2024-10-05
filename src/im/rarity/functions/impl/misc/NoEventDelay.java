package im.rarity.functions.impl.misc;

import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;

@FunctionRegister(name = "NoEventDelay", type = Category.Misc, description = "37")
public class NoEventDelay extends Function {
    public NoEventDelay() {
        super("EnhancedStats", Category.Render);
    }
}
