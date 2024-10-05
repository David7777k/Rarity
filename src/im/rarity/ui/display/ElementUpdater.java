package im.rarity.ui.display;

import im.rarity.events.EventUpdate;
import im.rarity.utils.client.IMinecraft;

public interface ElementUpdater extends IMinecraft {

    void update(EventUpdate e);
}
