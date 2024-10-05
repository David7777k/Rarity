package im.rarity.command.impl;

import im.rarity.command.AdviceCommandFactory;
import im.rarity.command.CommandProvider;
import im.rarity.command.Logger;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;


@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdviceCommandFactoryImpl implements AdviceCommandFactory {

    final Logger logger;
    @Override
    public AdviceCommand adviceCommand(CommandProvider commandProvider) {
        return new AdviceCommand(commandProvider, logger);
    }
}
