package im.rarity;

import com.google.common.eventbus.EventBus;
import im.rarity.command.*;
import im.rarity.command.friends.FriendStorage;
import im.rarity.command.impl.*;
import im.rarity.command.impl.feature.*;
import im.rarity.command.staffs.StaffStorage;
import im.rarity.config.ConfigStorage;
import im.rarity.events.EventKey;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegistry;
import im.rarity.scripts.client.ScriptManager;
import im.rarity.ui.ab.factory.ItemFactory;
import im.rarity.ui.ab.factory.ItemFactoryImpl;
import im.rarity.ui.ab.logic.ActivationLogic;
import im.rarity.ui.ab.model.IItem;
import im.rarity.ui.ab.model.ItemStorage;
import im.rarity.ui.ab.render.Window;
import im.rarity.ui.autobuy.AutoBuyConfig;
import im.rarity.ui.autobuy.AutoBuyHandler;
import im.rarity.ui.dropdown.DropDown;
import im.rarity.ui.mainmenu.AltConfig;
import im.rarity.ui.mainmenu.AltWidget;
import im.rarity.ui.styles.Style;
import im.rarity.ui.styles.StyleFactory;
import im.rarity.ui.styles.StyleFactoryImpl;
import im.rarity.ui.styles.StyleManager;
import im.rarity.utils.TPSCalc;
import im.rarity.utils.client.DiscordRPCUtill;
import im.rarity.utils.client.ServerTPS;
import im.rarity.utils.drag.DragManager;
import im.rarity.utils.drag.Dragging;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;
import via.ViaMCP;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;



@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class  Rarity {

    public static UserData userData;
    public boolean playerOnServer = false;
    public static final String CLIENT_NAME = "rarity solutions";


    @Getter
    private static Rarity instance;


    private FunctionRegistry functionRegistry;
    private ConfigStorage configStorage;
    private CommandDispatcher commandDispatcher;
    private ServerTPS serverTPS;
    private MacroManager macroManager;
    private StyleManager styleManager;


    private final EventBus eventBus = new EventBus();
    private final ScriptManager scriptManager = new ScriptManager();


    private final File clientDir = new File(Minecraft.getInstance().gameDir + "\\rarity");
    private final File filesDir = new File(Minecraft.getInstance().gameDir + "\\rarity\\files");


    private AltWidget altWidget;
    private AltConfig altConfig;
    private DropDown dropDown;
    private Window autoBuyUI;


    private AutoBuyConfig autoBuyConfig = new AutoBuyConfig();
    private AutoBuyHandler autoBuyHandler;
    private ViaMCP viaMCP;
    private TPSCalc tpsCalc;
    private ActivationLogic activationLogic;
    private ItemStorage itemStorage;

    public Rarity() {
        instance = this;

        if (!clientDir.exists()) {
            clientDir.mkdirs();
        }
        if (!filesDir.exists()) {
            filesDir.mkdirs();
        }

        clientLoad();
        FriendStorage.load();
        StaffStorage.load();
        DiscordRPCUtill.startRPC();
    }



    public Dragging createDrag(Function module, String name, float x, float y) {
        DragManager.draggables.put(name, new Dragging(module, name, x, y));
        return DragManager.draggables.get(name);
    }

    private void clientLoad() {
        viaMCP = new ViaMCP();
        serverTPS = new ServerTPS();
        functionRegistry = new FunctionRegistry();
        macroManager = new MacroManager();
        configStorage = new ConfigStorage();
        functionRegistry.init();
        initCommands();
        initStyles();
        altWidget = new AltWidget();
        altConfig = new AltConfig();
        tpsCalc = new TPSCalc();


        try {
            autoBuyConfig.init();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            altConfig.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            configStorage.init();
        } catch (IOException e) {
            System.out.println("Ошибка при подгрузке конфига.");
        }
        try {
            macroManager.init();
        } catch (IOException e) {
            System.out.println("Ошибка при подгрузке конфига макросов.");
        }
        DragManager.load();
        dropDown = new DropDown(new StringTextComponent(""));
        initAutoBuy();
        autoBuyUI = new Window(new StringTextComponent(""), itemStorage);
        //autoBuyUI = new AutoBuyUI(new StringTextComponent("A"));
        autoBuyHandler = new AutoBuyHandler();
        autoBuyConfig = new AutoBuyConfig();

        eventBus.register(this);
    }

    private final EventKey eventKey = new EventKey(-1);

    public void onKeyPressed(int key) {
        if (functionRegistry.getSelfDestruct().unhooked) return;
        eventKey.setKey(key);
        eventBus.post(eventKey);

        macroManager.onKeyPressed(key);

        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            Minecraft.getInstance().displayGuiScreen(dropDown);
        }
        if (this.functionRegistry.getAutoBuyUI().isState() && this.functionRegistry.getAutoBuyUI().setting.get() == key) {
            Minecraft.getInstance().displayGuiScreen(autoBuyUI);
        }


    }

    private void initAutoBuy() {
        ItemFactory itemFactory = new ItemFactoryImpl();
        CopyOnWriteArrayList<IItem> items = new CopyOnWriteArrayList<>();
        itemStorage = new ItemStorage(items, itemFactory);

        activationLogic = new ActivationLogic(itemStorage, eventBus);
    }

    private void initCommands() {
        Minecraft mc = Minecraft.getInstance();
        Logger logger = new MultiLogger(List.of(new ConsoleLogger(), new MinecraftLogger()));
        List<Command> commands = new ArrayList<>();
        Prefix prefix = new PrefixImpl();
        commands.add(new ListCommand(commands, logger));
        commands.add(new FriendCommand(prefix, logger, mc));
        commands.add(new BindCommand(prefix, logger));
        commands.add(new GPSCommand(prefix, logger));
        commands.add(new ConfigCommand(configStorage, prefix, logger));
        commands.add(new MacroCommand(macroManager, prefix, logger));
        commands.add(new VClipCommand(prefix, logger, mc));
        commands.add(new HClipCommand(prefix, logger, mc));
        commands.add(new StaffCommand(prefix, logger));
        commands.add(new MemoryCommand(logger));
        commands.add(new RCTCommand(logger, mc));

        AdviceCommandFactory adviceCommandFactory = new AdviceCommandFactoryImpl(logger);
        ParametersFactory parametersFactory = new ParametersFactoryImpl();

        commandDispatcher = new StandaloneCommandDispatcher(commands, adviceCommandFactory, prefix, parametersFactory, logger);
    }

    private void initStyles() {
        StyleFactory styleFactory = new StyleFactoryImpl();
        List<Style> styles = new ArrayList<>();

        styles.add(styleFactory.createStyle("Морской", new Color(5, 63, 111), new Color(133, 183, 246)));
        styles.add(styleFactory.createStyle("Малиновый", new Color(109, 10, 40), new Color(239, 96, 136)));
        styles.add(styleFactory.createStyle("Черничный", new Color(78, 5, 127), new Color(193, 140, 234)));
        styles.add(styleFactory.createStyle("Необычный", new Color(243, 160, 232), new Color(171, 250, 243)));
        styles.add(styleFactory.createStyle("Огненный", new Color(194, 21, 0), new Color(255, 197, 0)));
        styles.add(styleFactory.createStyle("Металлический", new Color(40, 39, 39), new Color(178, 178, 178)));
        styles.add(styleFactory.createStyle("Прикольный", new Color(82, 241, 171), new Color(66, 172, 245)));
        styles.add(styleFactory.createStyle("Новогодний", new Color(190, 5, 60), new Color(255, 255, 255)));
        styles.add(styleFactory.createStyle("Солнечный", new Color(255, 223, 0), new Color(255, 165, 0)));
        styles.add(styleFactory.createStyle("Лесной", new Color(34, 139, 34), new Color(107, 142, 35)));
        styles.add(styleFactory.createStyle("Космический", new Color(25, 25, 112), new Color(138, 43, 226)));
        styles.add(styleFactory.createStyle("Розовый закат", new Color(255, 105, 180), new Color(255, 69, 0)));
        styles.add(styleFactory.createStyle("Песчаный", new Color(244, 164, 96), new Color(210, 180, 140)));
        styles.add(styleFactory.createStyle("Глубокий океан", new Color(0, 105, 148), new Color(72, 61, 139)));
        styles.add(styleFactory.createStyle("Изумрудный", new Color(0, 128, 128), new Color(46, 139, 87)));
        styles.add(styleFactory.createStyle("Ретро", new Color(255, 87, 34), new Color(85, 139, 47)));
        styles.add(styleFactory.createStyle("Туманный", new Color(169, 169, 169), new Color(105, 105, 105)));
        styles.add(styleFactory.createStyle("Фиолетовый взрыв", new Color(148, 0, 211), new Color(186, 85, 211)));


        styleManager = new StyleManager(styles, styles.get(0));
    }

    public Object HUD() {
        return null;
    }


    @Getter
    @Setter
    @AllArgsConstructor
    public static class UserData {
        final String user;
        final int uid;
    }

}
