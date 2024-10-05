package im.rarity.scripts.interpreter.globals;

import im.rarity.scripts.interpreter.compiler.LuaC;
import im.rarity.scripts.interpreter.Globals;
import im.rarity.scripts.interpreter.LoadState;
import im.rarity.scripts.interpreter.lib.*;
import im.rarity.scripts.lua.libraries.ModuleLibrary;
import im.rarity.scripts.lua.libraries.PlayerLibrary;

public class Standarts {
    public static Globals standardGlobals() {
        Globals globals = new Globals();
        globals.load(new BaseLib());
        globals.load(new Bit32Lib());
        globals.load(new MathLib());
        globals.load(new TableLib());
        globals.load(new StringLib());
        globals.load(new PlayerLibrary());
        globals.load(new ModuleLibrary());
        LoadState.install(globals);
        LuaC.install(globals);
        return globals;
    }
}
