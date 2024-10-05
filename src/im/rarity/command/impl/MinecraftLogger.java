package im.rarity.command.impl;

import im.rarity.command.Logger;
import im.rarity.utils.client.IMinecraft;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class MinecraftLogger implements Logger, IMinecraft {
    @Override
    public void log(String message) {
        print(message);
    }
}
