package im.rarity.functions.impl.render;

import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;

@FunctionRegister(name = "ItemPhysic", type = Category.Render, description = "70")
public class ItemPhysic extends Function {
    public ItemPhysic() {
        super("EnhancedStats", Category.Render);
    }
}
