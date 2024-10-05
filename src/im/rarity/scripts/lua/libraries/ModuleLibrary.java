package im.rarity.scripts.lua.libraries;

import im.rarity.scripts.interpreter.LuaValue;
import im.rarity.scripts.interpreter.compiler.jse.CoerceJavaToLua;
import im.rarity.scripts.interpreter.lib.OneArgFunction;
import im.rarity.scripts.interpreter.lib.TwoArgFunction;
import im.rarity.scripts.lua.classes.ModuleClass;

public class ModuleLibrary extends TwoArgFunction {

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue library = tableOf();
        library.set("register", new register());

        env.set("module", library);
        return library;
    }

    public class register extends OneArgFunction {

        @Override
        public LuaValue call(LuaValue arg) {
            return CoerceJavaToLua.coerce(new ModuleClass(arg.toString()));
        }

    }

}
