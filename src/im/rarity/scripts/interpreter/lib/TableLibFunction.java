package im.rarity.scripts.interpreter.lib;

import im.rarity.scripts.interpreter.LuaValue;

class TableLibFunction extends LibFunction {
	public LuaValue call() {
		return argerror(1, "table expected, got no value");
	}
}
