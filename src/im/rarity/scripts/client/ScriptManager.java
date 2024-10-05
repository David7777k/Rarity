package im.rarity.scripts.client;

import java.util.ArrayList;
import java.util.List;

import im.rarity.Rarity;
import im.rarity.functions.api.Function;
import lombok.Getter;

public class ScriptManager {
    @Getter
    private List<MCScript> scripts = new ArrayList<>();

    public void add(MCScript script) {
        scripts.add(script);
    }

    public void compileScripts() {

        for (MCScript sc : scripts) {
            try {
                sc.compile();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    public void addModules() {

        for (MCScript sc : scripts) {
            try {
                boolean cancel = false;
                for (Function function : Rarity.getInstance().getFunctionRegistry().getFunctions()) {
                    if (function.getName().equalsIgnoreCase(sc.getFunction().getName())) {
                        cancel = true;
                        break;
                    }
                }
                if (cancel) return;
                Rarity.getInstance().getFunctionRegistry().getFunctions().add(sc.getFunction());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

}
