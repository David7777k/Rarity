package im.rarity.command.impl;

import im.rarity.command.Parameters;
import im.rarity.command.ParametersFactory;

public class ParametersFactoryImpl implements ParametersFactory {

    @Override
    public Parameters createParameters(String message, String delimiter) {
        return new ParametersImpl(message.split(delimiter));
    }
}
