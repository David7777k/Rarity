package im.rarity.command;

public interface Command {
    void execute(Parameters parameters);

    String name();

    String description();
}
