package im.rarity.functions.impl.render;

import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.functions.settings.impl.SliderSetting;

@FunctionRegister(name = "ViewModel", type = Category.Render, description = "81")
public class ViewModel extends Function {
    public final SliderSetting right_x = new SliderSetting("RightX", 0.0F, -2.0f, 2.0f, 0.1F);
    public final SliderSetting right_y = new SliderSetting("RightY", 0.0F, -2.0f, 2.0f, 0.1F);
    public final SliderSetting right_z = new SliderSetting("RightZ", 0.0F, -2.0f, 2.0f, 0.1F);
    public final SliderSetting left_x = new SliderSetting("LeftX", 0.0F, -2.0f, 2.0f, 0.1F);
    public final SliderSetting left_y = new SliderSetting("LeftY", 0.0F, -2.0f, 2.0f, 0.1F);
    public final SliderSetting left_z = new SliderSetting("LeftZ", 0.0F, -2.0f, 2.0f, 0.1F);

    public ViewModel() {
        super("EnhancedStats", Category.Render);
        addSettings(right_x, right_y, right_z, left_x, left_y, left_z);
    }
}
