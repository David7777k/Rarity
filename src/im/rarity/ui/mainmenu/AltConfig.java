package im.rarity.ui.mainmenu;

import com.google.gson.*;
import im.rarity.Rarity;
import im.rarity.utils.client.IMinecraft;
import net.minecraft.util.Session;

import java.io.*;
import java.util.UUID;

public class AltConfig implements IMinecraft {

    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final File file = new File(mc.gameDir, "rarity/files/alts.cfg");

    public void init() throws IOException {
        if (!file.exists()) {
            if (file.getParentFile().mkdirs()) {
                file.createNewFile();
            }
        } else {
            readAlts();
        }
    }

    public static void updateFile() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("last", mc.session.getUsername());

        JsonArray altsArray = new JsonArray();
        for (AltWidget.Account alt : Rarity.getInstance().getAltWidget().accounts) {
            altsArray.add(alt.getAccountName());
        }
        jsonObject.add("alts", altsArray);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(gson.toJson(jsonObject));
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readAlts() throws FileNotFoundException {
        JsonElement jsonElement = JsonParser.parseReader(new BufferedReader(new FileReader(file)));

        if (jsonElement == null || jsonElement.isJsonNull()) return;

        JsonObject jsonObject = jsonElement.getAsJsonObject();

        if (jsonObject.has("last")) {
            mc.session = new Session(jsonObject.get("last").getAsString(), UUID.randomUUID().toString(), "", "mojang");
        }

        if (jsonObject.has("alts")) {
            for (JsonElement element : jsonObject.get("alts").getAsJsonArray()) {
                String name = element.getAsString();
                Rarity.getInstance().getAltWidget().accounts.add(new AltWidget.Account(name));
            }
        }
    }
}
