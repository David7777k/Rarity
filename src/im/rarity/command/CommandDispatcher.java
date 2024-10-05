package im.rarity.command;

import im.rarity.command.impl.DispatchResult;

public interface CommandDispatcher {
    DispatchResult dispatch(String command);
}
