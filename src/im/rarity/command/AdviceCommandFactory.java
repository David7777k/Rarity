package im.rarity.command;

import im.rarity.command.impl.AdviceCommand;

public interface AdviceCommandFactory {
    AdviceCommand adviceCommand(CommandProvider commandProvider);
}
