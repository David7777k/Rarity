package im.rarity.events;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InventoryCloseEvent extends CancelEvent {

    public int windowId;

}
