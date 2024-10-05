package im.rarity.functions.impl.misc;

import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.functions.settings.impl.ModeSetting;
import im.rarity.functions.settings.impl.SliderSetting;

@FunctionRegister(name = "ClientSounds", type = Category.Misc, description = "15")
public class ClientSounds extends Function {

    // Настройки режима и громкости
    public ModeSetting mode = new ModeSetting("Тип", "Обычный", "Обычный", "Пузырьки", "Личный", "Тестовый");
    public SliderSetting volume = new SliderSetting("Громкость", 70.0f, 0.0f, 100.0f, 1.0f);

    public ClientSounds() {
        // Добавление настроек
        super("EnhancedStats", Category.Render);
        addSettings(mode, volume);
    }

    // Получение имени файла звука в зависимости от состояния и режима
    public String getFileName(boolean state) {
        switch (mode.get()) {
            case "Обычный" -> {
                return state ? "enable" : "disable";
            }
            case "Пузырьки" -> {
                return state ? "enableBubbles" : "disableBubbles";
            }
            case "Личный" -> {
                return state ? "enablePersonal" : "disablePersonal";
            }
            case "Тестовый" -> {
                return state ? "enableTest" : "disableTest"; // позже добавить новые звуки
            }
            default -> {
                return "";
            }
        }
    }
}
