package im.rarity.ui.display;

import im.rarity.events.EventDisplay;
import im.rarity.utils.client.IMinecraft;

public interface ElementRenderer extends IMinecraft {
    void render(EventDisplay eventDisplay);
}
