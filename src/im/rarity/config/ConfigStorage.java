package im.rarity.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigStorage {
    public final Logger logger = Logger.getLogger(ConfigStorage.class.getName());

    public final File CONFIG_DIR = new File(Minecraft.getInstance().gameDir, "rarity/configs");
    public final File AUTOCFG_DIR = new File(CONFIG_DIR, "autocfg.cfg");

    public final JsonParser jsonParser = new JsonParser();

    public void init() throws IOException {
        setupFolder();
    }

    public void setupFolder() {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
        } else if (AUTOCFG_DIR.exists()) {
            loadConfiguration("system");
            logger.log(Level.SEVERE, "Load system configuration...");
        } else {
            logger.log(Level.SEVERE, "Creating system configuration...");
            try {
                AUTOCFG_DIR.createNewFile();
                logger.log(Level.SEVERE, "Created!");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to create system configuration file", e);
            }
        }
    }

    public boolean isEmpty() {
        return getConfigs().isEmpty();
    }

    public List<Config> getConfigs() {
        List<Config> configs = new ArrayList<>();
        File[] configFiles = CONFIG_DIR.listFiles();

        if (configFiles != null) {
            for (File configFile : configFiles) {
                if (configFile.isFile() && configFile.getName().endsWith(".cfg")) {
                    String configName = configFile.getName().replace(".cfg", "");
                    Config config = findConfig(configName);
                    if (config != null) {
                        configs.add(config);
                    }
                }
            }
        }

        return configs;
    }

    public void loadConfiguration(String configuration) {
        Config config = findConfig(configuration);
        if (config == null) {
            logger.log(Level.WARNING, "Config " + configuration + " not found!");
            return;
        }
        try (FileReader reader = new FileReader(config.getFile())) {
            JsonParser parser = new JsonParser();
            JsonObject object = (JsonObject) parser.parse(reader);
            config.loadConfig(object);
        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, "Not Found Exception", e);
        } catch (NullPointerException pointerException) {
            logger.log(Level.WARNING, "Fatal error in Config!", pointerException);
        } catch (IOException e) {
            logger.log(Level.WARNING, "IOException occurred while loading config", e);
        }
    }

    public void saveConfiguration(String configuration) {
        Config config = new Config(configuration);
        String contentPrettyPrint = new GsonBuilder().setPrettyPrinting().create().toJson(config.saveConfig());
        try (FileWriter writer = new FileWriter(config.getFile())) {
            writer.write(contentPrettyPrint);
        } catch (IOException e) {
            logger.log(Level.WARNING, "File not found!", e);
        } catch (NullPointerException e) {
            logger.log(Level.WARNING, "Fatal Error in Config!", e);
        }
    }

    public Config findConfig(String configName) {
        if (configName == null) return null;
        if (new File(CONFIG_DIR, configName + ".cfg").exists())
            return new Config(configName);
        return null;
    }

    public void clearConfigurations() {
        File[] configFiles = CONFIG_DIR.listFiles();

        if (configFiles != null) {
            for (File configFile : configFiles) {
                if (configFile.isFile() && configFile.getName().endsWith(".cfg")) {
                    if (configFile.delete()) {
                        logger.log(Level.INFO, "Удалён конфиг: " + configFile.getName());
                    } else {
                        logger.log(Level.WARNING, "Не удалось удалить конфиг: " + configFile.getName());
                    }
                }
            }
        }
        logger.log(Level.INFO, "Все конфигурации были очищены!");
    }
}
