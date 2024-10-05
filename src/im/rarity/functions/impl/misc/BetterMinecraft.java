package im.rarity.functions.impl.misc;

import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.functions.settings.impl.BooleanSetting;

@FunctionRegister(name = "BetterMinecraft", type = Category.Misc, description = "16")
public class BetterMinecraft extends Function {


    public final BooleanSetting smoothCamera = new BooleanSetting("Плавная камера", true);


    public final BooleanSetting betterTab = new BooleanSetting("Улучшенный таб", true);


    public BetterMinecraft() {
        super("EnhancedStats", Category.Render);
        addSettings(smoothCamera, betterTab);
    }
    public boolean isSmoothCameraEnabled() {
        return smoothCamera.get();
    }
    public boolean isBetterTabEnabled() {
        return betterTab.get();
    }
    private void enableSmoothCamera() {
        if (isSmoothCameraEnabled()) {
        }
    }
    private void enableBetterTab() {
        if (isBetterTabEnabled()) {
        }
    }

    @Override
    public boolean onEnable() {
        enableSmoothCamera();
        enableBetterTab();
        super.onEnable();
        return false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
