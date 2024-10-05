package im.rarity.command;

public interface ParametersFactory {
    Parameters createParameters(String message, String delimiter);
}
