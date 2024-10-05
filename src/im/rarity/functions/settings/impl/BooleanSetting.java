package im.rarity.functions.settings.impl;


import im.rarity.functions.settings.Setting;

import java.util.function.Supplier;

public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, Boolean defaultVal) {
        super(name, defaultVal);
    }

    @Override
    public BooleanSetting setVisible(Supplier<Boolean> bool) {
        return (BooleanSetting) super.setVisible(bool);
    }

}