package im.rarity.functions.impl.combat;

import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;

@FunctionRegister(name = "NoEntityTrace", type = Category.Combat, description = "2")
public class NoEntityTrace extends Function {
    public NoEntityTrace() {
        super("EnhancedStats", Category.Render);
    }
}
