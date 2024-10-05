package im.rarity.command;

public interface CommandProvider {
    Command command(String alias);
}
