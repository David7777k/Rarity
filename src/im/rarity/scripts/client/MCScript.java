package im.rarity.scripts.client;

import com.google.common.eventbus.Subscribe;

import im.rarity.events.EventUpdate;
import im.rarity.functions.api.Function;
import im.rarity.scripts.interpreter.Globals;
import im.rarity.scripts.interpreter.LuaValue;
import im.rarity.scripts.interpreter.compiler.jse.CoerceJavaToLua;
import im.rarity.scripts.interpreter.globals.Standarts;
import im.rarity.scripts.lua.classes.ModuleClass;
import im.rarity.scripts.lua.classes.events.UpdateClass;
import lombok.Getter;

public class MCScript {

    private final String fileName;
    private String code;

    public MCScript(String fileName) {
        this.fileName = fileName;
    }

    public MCScript(String code, boolean empty) {
        this.fileName = "";
        this.code = code;
    }

    public String getFileName() {
        return fileName;
    }

    Globals globals;
    LuaValue chunk;
    ModuleClass moduleClass;

    @Getter
    private Function function;

    public void compile() {
        globals = Standarts.standardGlobals();
        if (code == null)
            chunk = globals.loadfile(fileName);
        else {
            chunk = globals.load(code);
        }
        chunk.call();

        if (globals.get("module").checkuserdata() instanceof ModuleClass mod) {
            moduleClass = mod;

             this.function = new Function(moduleClass.getModuleName()) {
                 @Override
                 public boolean onEnable() {
                     LuaValue val = globals.get("onEnable");
                     if (val != LuaValue.NIL) {
                         val.call();
                     }
                     return false;
                 }

                 @Subscribe
                 public void onUpdate(EventUpdate e) {
                     LuaValue val = globals.get("onEvent");
                     if (val != LuaValue.NIL) {
                         val.call(CoerceJavaToLua.coerce(new UpdateClass()));
                     }
                 }

             };
        }
    }

    public void call(String method) {
        globals.get(method).call();
    }

}
