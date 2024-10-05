package im.rarity.functions.impl.misc;

import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;

@FunctionRegister(name = "ItemScroller", type = Category.Misc, description = "34")
public class ItemScroller extends Function {
    public ItemScroller() {
        super("EnhancedStats", Category.Render);
    }
}
