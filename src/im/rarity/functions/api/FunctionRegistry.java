package im.rarity.functions.api;

import com.google.common.eventbus.Subscribe;
import im.rarity.Rarity;
import im.rarity.events.EventKey;
import im.rarity.functions.impl.combat.*;
import im.rarity.functions.impl.misc.*;
import im.rarity.functions.impl.movement.*;
import im.rarity.functions.impl.player.*;
import im.rarity.functions.impl.render.*;
import im.rarity.utils.render.font.Font;
import lombok.Getter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class FunctionRegistry {
    private final List<Function> functions = new CopyOnWriteArrayList<>();

    private SwingAnimation swingAnimation;
    private HUD hud;
    private AutoGapple autoGapple;
    private AutoSprint autoSprint;
    private Velocity velocity;
    private NoRender noRender;
    private Timer timer;
    private AutoTool autoTool;
    private ElytraHelper elytrahelper;
    private AutoBuyUI autoBuyUI;
    private ItemSwapFix itemswapfix;
    private AutoPotion autopotion;
    private TriggerBot triggerbot;
    private NoJumpDelay nojumpdelay;
    private ClickFriend clickfriend;
    private InventoryMove inventoryMove;
    private ESP esp;
    private AutoTransfer autoTransfer;
    private GriefHelper griefHelper;
    private ItemCooldown itemCooldown;
    private ClickPearl clickPearl;
    private AutoSwap autoSwap;
    private AutoArmor autoArmor;
    private Hitbox hitbox;
    private HitSound hitsound;
    private AntiPush antiPush;
    private FreeCam freeCam;
    private ChestStealer chestStealer;
    private AutoLeave autoLeave;
    private AutoAccept autoAccept;
    private NoEventDelay noEventDelay;
    private AutoRespawn autoRespawn;
    private Fly fly;
    private TargetStrafe targetStrafe;
    private ClientSounds clientSounds;
    private AutoTotem autoTotem;
    private Pointers pointers;
    private AutoExplosion autoExplosion;
    private NoRotate noRotate;
    private KillAura killAura;
    private AntiBot antiBot;
    private Trails trails;
    private Crosshair crosshair;
    private Strafe strafe;
    private World world;
    private ViewModel viewModel;
    private ElytraFly elytraFly;
    private ChinaHat chinaHat;
    private Snow snow;
    private Particles particles;
    private TargetESP targetESP;
    private JumpCircle jumpCircle;
    private ItemPhysic itemPhysic;
    private Predictions predictions;
    private NoEntityTrace noEntityTrace;
    private NoClip noClip;
    private ItemScroller itemScroller;
    private AutoFish autoFish;
    private StorageESP storageESP;
    private Spider spider;
    private NameProtect nameProtect;
    private NoInteract noInteract;
    private GlassHand glassHand;
    private Tracers tracers;
    private SelfDestruct selfDestruct;
    private LeaveTracker leaveTracker;
    private BoatFly boatFly;
    private AntiAFK antiAFK;
    private PortalGodMode portalGodMode;
    private BetterMinecraft betterMinecraft;
    private Backtrack backtrack;
    private SeeInvisibles seeInvisibles;
    private CasinoBOT casinoBOT;
    private BaseFinder baseFinder;
    private Arrows arrows;
    private Jesus jesus;
    private VulcanESP vulcanESP;

    public FunctionRegistry() {
        vulcanESP = new VulcanESP();
        jesus = new Jesus();
        arrows = new Arrows();
    }

    public void init() {

        registerAll(
                hud = new HUD(),
                autoGapple = new AutoGapple(),
                autoSprint = new AutoSprint(),
                velocity = new Velocity(),
                noRender = new NoRender(),
                autoTool = new AutoTool(),
                seeInvisibles = new SeeInvisibles(),
                elytrahelper = new ElytraHelper(),
                itemswapfix = new ItemSwapFix(),
                autopotion = new AutoPotion(),
                noClip = new NoClip(),
                triggerbot = new TriggerBot(),
                nojumpdelay = new NoJumpDelay(),
                clickfriend = new ClickFriend(),
                inventoryMove = new InventoryMove(),
                esp = new ESP(),
                autoTransfer = new AutoTransfer(),
                griefHelper = new GriefHelper(),
                autoArmor = new AutoArmor(),
                hitbox = new Hitbox(),
                hitsound = new HitSound(),
                antiPush = new AntiPush(),
                autoBuyUI = new AutoBuyUI(),
                freeCam = new FreeCam(),
                chestStealer = new ChestStealer(),
                autoLeave = new AutoLeave(),
                autoAccept = new AutoAccept(),
                autoRespawn = new AutoRespawn(),
                fly = new Fly(),
                clientSounds = new ClientSounds(),
                pointers = new Pointers(),
                autoExplosion = new AutoExplosion(),
                noRotate = new NoRotate(),
                antiBot = new AntiBot(),
                trails = new Trails(),
                crosshair = new Crosshair(),
                autoTotem = new AutoTotem(),
                itemCooldown = new ItemCooldown(),
                killAura = new KillAura(autopotion),
                clickPearl = new ClickPearl(itemCooldown),
                autoSwap = new AutoSwap(autoTotem),
                targetStrafe = new TargetStrafe(killAura),
                strafe = new Strafe(targetStrafe, killAura),
                swingAnimation = new SwingAnimation(killAura),
                targetESP = new TargetESP(killAura),
                world = new World(),
                viewModel = new ViewModel(),
                elytraFly = new ElytraFly(),
                chinaHat = new ChinaHat(),
                snow = new Snow(),
                particles = new Particles(),
                jumpCircle = new JumpCircle(),
                itemPhysic = new ItemPhysic(),
                predictions = new Predictions(),
                noEntityTrace = new NoEntityTrace(),
                itemScroller = new ItemScroller(),
                autoFish = new AutoFish(),
                storageESP = new StorageESP(),
                spider = new Spider(),
                timer = new Timer(),
                nameProtect = new NameProtect(),
                noInteract = new NoInteract(),
                glassHand = new GlassHand(),
                tracers = new Tracers(),
                selfDestruct = new SelfDestruct(),
                leaveTracker = new LeaveTracker(),
                antiAFK = new AntiAFK(),
                portalGodMode = new PortalGodMode(),
                betterMinecraft = new BetterMinecraft(),
                backtrack = new Backtrack(),
                new LongJump(),
                new XrayBypass(),
                new Parkour(),
                new RWHelper(),
                casinoBOT = new CasinoBOT(),
                baseFinder = new BaseFinder(),
                jesus = new Jesus(),
                vulcanESP = new VulcanESP(),
                arrows = new Arrows()
        );


        Rarity.getInstance().getEventBus().register(this);
    }


    private void registerAll(Function... functions) {
        Arrays.sort(functions, Comparator.comparing(Function::getName));
        this.functions.addAll(List.of(functions));
    }
    public List<Function> getSorted(Font font, float size) {
        return functions.stream()
                .sorted((f1, f2) -> Float.compare(font.getWidth(f2.getName(), size), font.getWidth(f1.getName(), size)))
                .toList();
    }
    @Subscribe
    private void onKey(EventKey e) {
        if (selfDestruct.unhooked) return;
        for (Function function : functions) {
            if (function.getBind() == e.getKey()) {
                function.toggle();
            }
        }
    }
}
