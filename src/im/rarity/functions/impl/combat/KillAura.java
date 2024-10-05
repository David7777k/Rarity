package im.rarity.functions.impl.combat;

import com.google.common.eventbus.Subscribe;
import im.rarity.Rarity;
import im.rarity.command.friends.FriendStorage;
import im.rarity.events.EventInput;
import im.rarity.events.EventMotion;
import im.rarity.events.EventUpdate;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.functions.settings.Setting;
import im.rarity.functions.settings.impl.BooleanSetting;
import im.rarity.functions.settings.impl.ModeListSetting;
import im.rarity.functions.settings.impl.ModeSetting;
import im.rarity.functions.settings.impl.SliderSetting;
import im.rarity.utils.client.IMinecraft;
import im.rarity.utils.math.SensUtils;
import im.rarity.utils.math.StopWatch;
import im.rarity.utils.player.InventoryUtil;
import im.rarity.utils.player.MouseUtil;
import im.rarity.utils.player.MoveUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.optifine.CustomColors;

@FunctionRegister(
        name = "Aura", type = Category.Combat, description = "23")
public class KillAura extends Function {
    private final ModeSetting type = new ModeSetting("Ротация", "Funtime", new String[]{"Funtime", "ReallyWorld", "HolyWorld", "HvH", "Плавная","Снапы","Custom","Bypass","Matrix","GrimAC","Noclips","Intave","Ручная"});
    private final SliderSetting attackRange = new SliderSetting("Дистанция аттаки", 3.0F, 2.0F, 6.0F, 0.1F);
    private final SliderSetting tickRot = (new SliderSetting("Тики наводки", 3.0F, 1.0F, 10.0F, 1.0F)).setVisible(() -> {
        return !this.type.is("HvH") && !this.type.is("Плавная") && !this.type.is("FunAC") && !this.type.is("Bypass") && !this.type.is("Custom") && !this.type.is("Matrix") && !this.type.is("GrimAC") && !this.type.is("Intave") && !this.type.is("Funtime") && !this.type.is("HolyWorld");
    });
    private final SliderSetting aimDistance = (new SliderSetting("Дальность наводки", 0.0F, 0.0F, 10.0F, 1.0F)).setVisible(() -> {
        return !this.type.is("Снапы") && !this.type.is("ReallyWorld");
    });
    private final SliderSetting lerpYaw = (new SliderSetting("Custom X Speed", 0.46F, 0.01F, 1.0F, 0.01F)).setVisible(() -> {
        return !this.type.is("HvH") && !this.type.is("Плавная") && !this.type.is("Bypass") && !this.type.is("FunAC") && !this.type.is("Снапы") && !this.type.is("Matrix") && !this.type.is("GrimAC") && !this.type.is("Intave") && !this.type.is("Funtime");
    });
    private final SliderSetting lerpPitch = (new SliderSetting("Custom Y Speed", 0.28F, 0.01F, 1.0F, 0.01F)).setVisible(() -> {
        return !this.type.is("HvH") && !this.type.is("Плавная") && !this.type.is("Bypass") && !this.type.is("Снапы") && !this.type.is("FunAC") && !this.type.is("Matrix") && !this.type.is("GrimAC") && !this.type.is("Intave") && !this.type.is("Funtime");
    });
    final ModeListSetting targets = new ModeListSetting("Таргеты", new BooleanSetting[]{new BooleanSetting("Игроки", true), new BooleanSetting("Голые", true), new BooleanSetting("Мобы", false), new BooleanSetting("Животные", false), new BooleanSetting("Друзья", false), new BooleanSetting("Голые невидимки", true), new BooleanSetting("Невидимки", true)});
    final ModeListSetting options = new ModeListSetting("Опции", new BooleanSetting[]{new BooleanSetting("Только криты", true), new BooleanSetting("Ломать щит", true), new BooleanSetting("Отжимать щит", true), new BooleanSetting("Ускорять ротацию", false), new BooleanSetting("ТПС Синхрон", false), new BooleanSetting("Фокусировать одну цель", true), new BooleanSetting("Коррекция движения", true)});
    private final BooleanSetting legitCorrection = new BooleanSetting("Сайлент Мувфикс", true);
    private final BooleanSetting legitRotations = new BooleanSetting("Ротация Камеры", false);
    private final StopWatch stopWatch = new StopWatch();
    private Vector2f rotateVector = new Vector2f(0.0F, 0.0F);
    private LivingEntity target;
    private Entity selected;
    int ticks = 0;
    boolean isRotated;
    final AutoPotion autoPotion;
    float lastYaw;
    float lastPitch;

    public KillAura(AutoPotion var1) {
        super("EnhancedStats", Category.Render);
        this.autoPotion = var1;
        this.addSettings(new Setting[]{this.type, this.attackRange, this.tickRot, this.aimDistance, this.targets, this.options, this.legitCorrection});
    }

    @Subscribe
    public void onInput(EventInput var1) {
        if ((Boolean)this.options.getValueByName("Коррекция движения").get() && this.target != null) {
            Minecraft var10000 = IMinecraft.mc;
            if (mc.player != null && (Boolean)this.legitCorrection.get()) {
                if (this.type.is("Плавная")) {
                    MoveUtils.fixMovement(var1, this.rotateVector.x);
                }

                if (this.type.is("Обход")) {
                    MoveUtils.fixMovement(var1, this.rotateVector.x);
                }

                if (this.type.is("HvH")) {
                    MoveUtils.fixMovement(var1, this.rotateVector.x);
                }

                if (this.type.is("Легитная")) {
                    MoveUtils.fixMovement(var1, this.rotateVector.x);
                }

                if (this.type.is("Ручная")) {
                    MoveUtils.fixMovement(var1, this.rotateVector.x);
                }

                if (this.type.is("Custom")) {
                    MoveUtils.fixMovement(var1, this.rotateVector.x);
                }

                if (this.type.is("Bypass")) {
                    MoveUtils.fixMovement(var1, this.rotateVector.x);
                }

                if (this.type.is("Снапы")) {
                    MoveUtils.fixMovement(var1, this.rotateVector.x);
                }

                if (this.type.is("Matrix")) {
                    MoveUtils.fixMovement(var1, this.rotateVector.x);
                }

                if (this.type.is("GrimAC")) {
                    MoveUtils.fixMovement(var1, this.rotateVector.x);
                }

                if (this.type.is("Intave")) {
                    MoveUtils.fixMovement(var1, this.rotateVector.x);
                }

                if (this.type.is("Funtime")) {
                    MoveUtils.fixMovement(var1, this.rotateVector.x);
                }

                if (this.type.is("ReallyWorld")) {
                    MoveUtils.fixMovement(var1, this.rotateVector.x);
                }

                if (this.type.is("HolyWorld")) {
                    MoveUtils.fixMovement(var1, this.rotateVector.x);
                }
            }
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate var1) {
        if ((Boolean)this.options.getValueByName("Фокусировать одну цель").get() && (this.target == null || !this.isValid(this.target)) || !(Boolean)this.options.getValueByName("Фокусировать одну цель").get()) {
            this.updateTarget();
        }

        if (this.target == null || this.autoPotion.isState() && this.autoPotion.isActive()) {
            this.stopWatch.setLastMS(0L);
            this.reset();
        } else {
            this.isRotated = false;
            int var3;
            if (this.shouldPlayerFalling() && this.stopWatch.hasTimeElapsed()) {
                this.updateAttack();
                var3 = Math.round((Float)this.tickRot.get());
                this.ticks = var3;
            }

            if (!this.type.is("Снапы") && !this.type.is("ReallyWorld")) {
                if (!this.isRotated) {
                    this.updateRotation(false, 80.0F, 35.0F);
                }
            } else if (this.ticks > 0) {
                this.updateRotation(true, 180.0F, 90.0F);
                var3 = this.ticks--;
            } else {
                this.reset();
            }
        }

    }

    @Subscribe
    private void onWalking(EventMotion var1) {
        if (this.target != null && (!this.autoPotion.isState() || !this.autoPotion.isActive())) {
            float var2 = this.rotateVector.x;
            float var3 = this.rotateVector.y;
            var1.setYaw(var2);
            var1.setPitch(var3);
            Minecraft var10000 = IMinecraft.mc;
            mc.player.rotationYawHead = var2;
            var10000 = IMinecraft.mc;
            mc.player.renderYawOffset = var2;
            var10000 = IMinecraft.mc;
            mc.player.rotationPitchHead = var3;
            float var4 = this.rotateVector.x;
            if ((Boolean)this.legitRotations.get()) {
                var10000 = IMinecraft.mc;
                mc.player.renderArmYaw = var4;
                var10000 = IMinecraft.mc;
                mc.player.prevRenderArmYaw = var4;
                var10000 = IMinecraft.mc;
                mc.player.rotationYaw = var4;
                var10000 = IMinecraft.mc;

                mc.player.prevRotationYaw = var4;
                var10000 = IMinecraft.mc;
                mc.player.rotationYawHead = var4;
                var10000 = IMinecraft.mc;
                mc.player.prevRotationYawHead = var4;
                var10000 = IMinecraft.mc;
                mc.player.rotationPitch = var3;
                var10000 = IMinecraft.mc;
                mc.player.prevRotationPitch = var3;
            }
        }

    }

    private void updateTarget() {
        ArrayList var1 = new ArrayList();
        Minecraft var10000 = IMinecraft.mc;
        Iterator target = mc.world.getAllEntities().iterator();

        while(target.hasNext()) {
            Entity var3 = (Entity)target.next();
            if (var3 instanceof LivingEntity var4) {
                if (this.isValid(var4)) {
                    var1.add(var4);
                }
            }
        }

        if (var1.isEmpty()) {
            this.target = null;
        } else if (var1.size() == 1) {
            this.target = (LivingEntity)var1.get(0);
        } else {
            var1.sort(Comparator.comparingDouble((var1x) -> {

                if (var1x instanceof PlayerEntity var3) {
                    return -this.getEntityArmor(var3);
                } else if (var1x instanceof LivingEntity var2) {
                    return (double)(-var2.getTotalArmorValue());
                } else {
                    return 0.0;
                }
            }).thenComparing((var1x, var2x) -> {
                double var3 = this.getEntityHealth((LivingEntity)var1x);
                double var5 = this.getEntityHealth((LivingEntity)var2x);
                return Double.compare(var3, var5);
            }).thenComparing((var0, var1x) -> {

                double var2 = (double)mc.player.getDistance((LivingEntity)var0);

                double var4 = (double)mc.player.getDistance((LivingEntity)var1x);
                return Double.compare(var2, var4);
            }));
            this.target = (LivingEntity)var1.get(0);
        }

    }

    private void updateRotation(boolean var1, float var2, float var3) {
        Vector3d var28 = this.target.getPositionVec();
        Minecraft var10000 = IMinecraft.mc;
        double var30 = mc.player.getPosYEye();
        double var10003 = this.target.getPosY();
        double var10002 = var30 - var10003;
        double var10004 = (double)this.target.getHeight();
        Minecraft var10005 = IMinecraft.mc;
        Vector3d var40 = var28.add(0.0, MathHelper.clamp(var10002, 0.0, var10004 * (mc.player.getDistanceEyePos(this.target) / (double)(Float)this.attackRange.get())), 0.0);
        Minecraft var10001 = IMinecraft.mc;
        Vector3d var4 = var40.subtract(mc.player.getEyePosition(1.0F));
        this.isRotated = true;
        double var29 = Math.toDegrees(Math.atan2(var4.z, var4.x));
        float var5 = (float)MathHelper.wrapDegrees(var29 - 90.0);
        float var6 = (float)(-Math.toDegrees(Math.atan2(var4.y, Math.hypot(var4.x, var4.z))));
        float var32 = this.rotateVector.x;
        float var7 = MathHelper.wrapDegrees(var5 - var32);
        var32 = this.rotateVector.y;
        float var8 = MathHelper.wrapDegrees(var6 - var32);
        int var9 = (int)var7;
        float var12;
        float var13;
        float var14;
        float var15;
        float var16;
        float var17;

        float var18;
        float var19;
        float var20;
        float var21;
        float var22;
        float var23;
        float var24;
        float var31;
        switch ((String)this.type.get()) {
            case "Плавная":
                var12 = Math.min(Math.max(Math.abs(var7), 1.0F), var2);
                var13 = Math.min(Math.max(Math.abs(var8), 1.0F), var3);
                if (var1 && this.selected != this.target && (Boolean)this.options.getValueByName("Ускорять ротацию").get()) {
                    var13 = Math.max(Math.abs(var8), 1.0F);
                } else {
                    var13 /= 3.0F;
                }

                var32 = this.lastYaw;
                if (Math.abs(var12 - var32) <= 3.0F) {
                    var12 = this.lastYaw + 3.1F;
                }

                var14 = this.rotateVector.x + (var7 > 0.0F ? var12 : -var12);
                var15 = MathHelper.clamp(this.rotateVector.y + (var8 > 0.0F ? var13 : -var13), -89.0F, 89.0F);
                var16 = SensUtils.getGCDValue();
                var31 = this.rotateVector.x;
                var32 = (var14 - var31) % var16;
                var14 -= var32;
                var31 = this.rotateVector.y;
                var32 = (var15 - var31) % var16;
                var15 -= var32;
                this.rotateVector = new Vector2f(var14, var15);
                this.lastYaw = var12;
                this.lastPitch = var13;
                if ((Boolean)this.options.getValueByName("Коррекция движения").get()) {
                    var10000 = IMinecraft.mc;
                    mc.player.rotationYawOffset = var14;
                }
                break;
            case "Снапы":
                var12 = this.rotateVector.x + (float)var9;
                var13 = MathHelper.clamp(this.rotateVector.y + var8, -90.0F, 90.0F);
                var14 = SensUtils.getGCDValue();
                var31 = this.rotateVector.x;
                var32 = (var12 - var31) % var14;
                var12 -= var32;
                var31 = this.rotateVector.y;
                var32 = (var13 - var31) % var14;
                var13 -= var32;
                this.rotateVector = new Vector2f(var12, var13);
                if ((Boolean)this.options.getValueByName("Коррекция движения").get()) {
                    var10000 = IMinecraft.mc;
                    mc.player.rotationYawOffset = var12;
                }
                break;
            case "ReallyWorld":
                var12 = this.rotateVector.x + (float)var9;
                var13 = MathHelper.clamp(this.rotateVector.y + var8, -90.0F, 90.0F);
                var14 = SensUtils.getGCDValue();
                var31 = this.rotateVector.x;
                var32 = (var12 - var31) % var14;
                var12 -= var32;
                var31 = this.rotateVector.y;
                var32 = (var13 - var31) % var14;
                var13 -= var32;
                this.rotateVector = new Vector2f(var12, var13);
                if ((Boolean)this.options.getValueByName("Коррекция движения").get()) {
                    var10000 = IMinecraft.mc;
                    mc.player.rotationYawOffset = var12;
                }
                break;
            case "HvH":
                var12 = 1.0F;
                var40 = this.target.getPositionVec().add(0.0, (double)(this.target.getHeight() / 2.0F), 0.0);
                var10001 = IMinecraft.mc;
                Vector3d var27 = var40.subtract(mc.player.getEyePosition(1.0F));
                var29 = Math.toDegrees(Math.atan2(var27.z, var27.x));
                var14 = (float)MathHelper.wrapDegrees(var29 - 90.0);
                var15 = (float)(-Math.toDegrees(Math.atan2(var27.y, Math.hypot(var27.x, var27.z))));
                var16 = this.lerp(this.rotateVector.x, var14, var12);
                var17 = this.lerp(this.rotateVector.y, var15, var12);
                var18 = SensUtils.getGCDValue();
                var31 = this.rotateVector.x;
                var32 = (var16 - var31) % var18;
                var16 -= var32;
                var31 = this.rotateVector.y;
                var32 = (var17 - var31) % var18;
                var17 -= var32;
                this.rotateVector = new Vector2f(var16, var17);
                if ((Boolean)this.options.getValueByName("Коррекция движения").get()) {
                    var10000 = IMinecraft.mc;
                    mc.player.rotationYawOffset = var16;
                }
                break;
            case "Custom":
                var12 = (Float)this.lerpYaw.get();
                var13 = (Float)this.lerpPitch.get();
                var14 = MathHelper.clamp(Math.abs(var7), 1.0F, var2);
                var15 = MathHelper.clamp(Math.abs(var8), 1.0F, var3);
                if (var1 && this.selected != this.target && (Boolean)this.options.getValueByName("Ускорять ротацию").get()) {
                    var15 *= 2.0F;
                }

                var15 /= 3.0F;
                var29 = Math.random() * 0.02;
                var16 = (float)(var29 - 0.01);
                var29 = Math.random() * 0.02;
                var17 = (float)(var29 - 0.01);
                var18 = this.rotateVector.x + (var7 > 0.0F ? var14 : -var14) + var16;
                var19 = MathHelper.clamp(this.rotateVector.y + (var8 > 0.0F ? var15 : -var15) + var17, -89.0F, 89.0F);
                var20 = SensUtils.getGCDValue();
                var31 = this.rotateVector.x;
                var32 = (var18 - var31) % var20;
                var18 -= var32;
                var31 = this.rotateVector.y;
                var32 = (var19 - var31) % var20;
                var19 -= var32;
                var18 = MathHelper.lerp(var12, this.rotateVector.x, var18);
                var19 = MathHelper.lerp(var13, this.rotateVector.y, var19);
                this.rotateVector = new Vector2f(var18, var19);
                if ((Boolean)this.options.getValueByName("Коррекция движения").get()) {
                    var10000 = IMinecraft.mc;
                    mc.player.rotationYawOffset = var18;
                }
                break;
            case "Bypass":
                var12 = Math.min(Math.max(Math.abs(var7), 1.1F), var2);
                var13 = Math.min(Math.max(Math.abs(var8), 1.1F), var3);
                if (var1 && this.selected != this.target && (Boolean)this.options.getValueByName("Ускорять ротацию").get()) {
                    var13 = Math.max(Math.abs(var8), 1.0F);
                } else {
                    var13 /= 3.0F;
                }

                var12 = (float)((double)var12 * 1.6432675628475908);
                var32 = this.lastYaw;
                if (Math.abs(var12 - var32) <= 3.0F) {
                    var12 = this.lastYaw + 3.1F;
                }

                var14 = 0.0F;
                var15 = 1.0F;
                var16 = (float)((double)var14 + (double)var15 * Math.random());
                var17 = (float)((double)var14 + (double)var15 * Math.random());
                var18 = this.rotateVector.x + (var7 > 0.0F ? var12 : -var12) + var16;
                var19 = MathHelper.clamp(this.rotateVector.y + (var8 > 0.0F ? var13 : -var13) + var17, -89.0F, 89.0F);
                var20 = SensUtils.getGCDValue();
                var31 = this.rotateVector.x;
                var32 = (var18 - var31) % var20;
                var18 -= var32;
                var31 = this.rotateVector.y;
                var32 = (var19 - var31) % var20;
                var19 -= var32;
                this.rotateVector = new Vector2f(var18, var19);
                this.lastYaw = var12;
                this.lastPitch = var13;
                if ((Boolean)this.options.getValueByName("Коррекция движения").get()) {
                    var10000 = IMinecraft.mc;
                    mc.player.rotationYawOffset = var18;
                }
                break;
            case "Matrix":
                var12 = 0.8148602F;
                var13 = 3.2F;
                var14 = Math.min(Math.max(Math.abs(var7), 1.85F), var2);
                var15 = Math.min(Math.max(Math.abs(var8), 1.98F), var3);
                if (var1 && this.selected != this.target && (Boolean)this.options.getValueByName("Ускорять ротацию").get()) {
                    var15 = Math.max(Math.abs(var8), 1.0F);
                } else {
                    var15 /= 3.1F;
                }

                var14 *= var12;
                var15 *= var12;
                var29 = Math.random();
                "垞婓".length();
                var16 = (float)(var29 - 0.5) * 3.0F * var13;
                var14 += var16;
                var17 = this.rotateVector.x + (var7 > 0.0F ? var14 : -var14);
                var18 = MathHelper.clamp(this.rotateVector.y + (var8 > 0.0F ? var15 : -var15), -89.0F, 89.0F);
                var19 = SensUtils.getGCDValue();
                var31 = this.rotateVector.x;
                var32 = (var17 - var31) % var19;
                var17 -= var32;
                var31 = this.rotateVector.y;
                var32 = (var18 - var31) % var19;
                var18 -= var32;
                this.rotateVector = new Vector2f(var17, var18);
                this.lastYaw = var14;
                this.lastPitch = var15;
                if ((Boolean)this.options.getValueByName("Коррекция движения").get()) {
                    var10000 = IMinecraft.mc;
                    mc.player.rotationYawOffset = var17;
                }
                break;
            case "GrimAC":
                var12 = Math.min(Math.max(Math.abs(var7), 1.15F), var2);
                var13 = Math.min(Math.max(Math.abs(var8), 1.15F), var3);
                if (var1 && this.selected != this.target && (Boolean)this.options.getValueByName("Ускорять ротацию").get()) {
                    var13 = Math.max(Math.abs(var8), 1.0F);
                } else {
                    var13 /= 3.0F;
                }

                var29 = Math.random();
                var14 = (float)(var29 - 0.5) * 2.0F;
                var12 += var14 * 5.0F;
                var32 = this.lastYaw;
                if (Math.abs(var12 - var32) <= 3.0F) {
                    var12 = this.lastYaw + 3.1F;
                }

                var15 = var7 > 0.0F ? var12 : -var12;
                var16 = var8 > 0.0F ? var13 : -var13;
                var17 = this.rotateVector.x + var15;
                var18 = MathHelper.clamp(this.rotateVector.y + var16, -89.0F, 89.0F);
                var19 = SensUtils.getGCDValue();
                var31 = this.rotateVector.x;
                var32 = (var17 - var31) % var19;
                var17 -= var32;
                var31 = this.rotateVector.y;
                var32 = (var18 - var31) % var19;
                var18 -= var32;
                var29 = (double)var17;
                double var33 = Math.random();
                var17 = (float)(var29 + (var33 - 0.62) * 0.456);
                var29 = (double)var18;
                var33 = Math.random();
                var18 = (float)(var29 + (var33 - 0.65) * 0.512);
                this.rotateVector = new Vector2f(var17, var18);
                this.lastYaw = var12;
                this.lastPitch = var13;
                if ((Boolean)this.options.getValueByName("Коррекция движения").get()) {
                    var10000 = IMinecraft.mc;
                    mc.player.rotationYawOffset = var17;
                }

                this.rotateVector = new Vector2f(var17, var18);
                break;
            case "Intave":
                var12 = Math.min(Math.max(Math.abs(var7) * 1.71F, 1.0F), var2 * 2.0F);
                var13 = Math.min(Math.max(Math.abs(var8) * 1.71F, 1.0F), var3 * 1.55F * 2.7F);
                if (var1 && this.selected != this.target && (Boolean)this.options.getValueByName("Ускорять ротацию").get()) {
                    var13 = Math.max(Math.abs(var8) * 1.65F, 1.0F);
                } else {
                    var13 /= 3.0F;
                }

                var32 = this.lastYaw;
                if (Math.abs(var12 - var32) <= 4.0F) {
                    var12 = this.lastYaw + 4.1F;
                }

                var32 = this.lastPitch;
                if (Math.abs(var13 - var32) <= 4.0F) {
                    var13 = this.lastPitch + 4.1F;
                }

                var14 = this.rotateVector.x + (var7 > 0.0F ? var12 : -var12);
                var15 = MathHelper.clamp(this.rotateVector.y + (var8 > 0.0F ? var13 : -var13), -90.0F, 90.0F);
                var16 = SensUtils.getGCDValue();
                var31 = this.rotateVector.x;
                var32 = (var14 - var31) % var16;
                var14 -= var32;
                var31 = this.rotateVector.y;
                var32 = (var15 - var31) % var16;
                var15 -= var32;
                this.rotateVector = new Vector2f(var14, var15);
                this.lastYaw = var12;
                this.lastPitch = var13;
                if ((Boolean)this.options.getValueByName("Коррекция движения").get()) {
                    var10000 = IMinecraft.mc;
                    mc.player.rotationYawOffset = var14;
                }
                break;
            case "Ручная":
                this.scanHitbox(this.target);
                var12 = Math.min(Math.max(Math.abs(var7), 1.14F), var2);
                var13 = Math.min(Math.max(Math.abs(var8), 1.14F), var3);
                if (var1 && this.selected != this.target && (Boolean)this.options.getValueByName("Ускорять ротацию").get()) {
                    var13 = Math.max(Math.abs(var8), 1.0F);
                } else {
                    var13 /= 3.0F;
                }

                var12 *= 1.8F;
                var14 = (float)this.gaussianRandom(0.0, (double)(var12 / 7.6F));
                var32 = this.lastYaw;
                if (Math.abs(var12 - var32) <= 3.0F) {
                    var12 = this.lastYaw + 3.1F;
                }

                var15 = (float)this.gaussianRandom(-0.5, 0.5);
                var16 = (float)this.gaussianRandom(-0.5, 0.5);
                var17 = (float)this.gaussianRandom(0.0, 0.8999999761581421) + var15;
                var18 = (float)this.gaussianRandom(0.0, -0.8999999761581421) + var16;
                var19 = (float)(Math.sin((double)(System.currentTimeMillis() % 1000L) / 1000.0 * Math.PI * 2.0) * 1.5);
                var20 = 0.78F;
                var21 = this.rotateVector.x + (var7 > 0.0F ? (var12 + var17 + var19) * var20 : -(var12 + var17 + var19) * var20);
                var22 = MathHelper.clamp(this.rotateVector.y + (var8 > 0.0F ? (var13 + var18 + var14) * var20 : -(var13 + var18 + var14) * var20), -89.0F, 89.0F);
                var23 = SensUtils.getGCDValue();
                var31 = this.rotateVector.x;
                var32 = (var21 - var31) % var23;
                var21 -= var32;
                var31 = this.rotateVector.y;
                var32 = (var22 - var31) % var23;
                var22 -= var32;
                this.rotateVector = new Vector2f(var21, var22);
                this.lastYaw = var12;
                this.lastPitch = var13;
                if ((Boolean)this.options.getValueByName("Коррекция движения").get()) {
                    var10000 = IMinecraft.mc;
                    mc.player.rotationYawOffset = var21;
                }
                break;
            case "Funtime":
                var12 = (float)(Math.sin((double)(System.currentTimeMillis() % 1000L) / 1000.0 * Math.PI * 2.0) * 1.600000023841858);
                var13 = Math.min(Math.max(Math.abs(var7), 1.24F), var2);
                var14 = Math.min(Math.max(Math.abs(var8), 1.19F), var3);
                if (var1 && this.selected != this.target && (Boolean)this.options.getValueByName("Ускорять ротацию").get()) {
                    var14 = Math.max(Math.abs(var8), 1.0F);
                } else {
                    var14 /= 3.0F;
                }

                var32 = this.lastYaw;
                if (Math.abs(var13 - var32) <= 3.0F) {
                    var13 = this.lastYaw + 3.1F;
                }

                var29 = Math.random();
                var15 = (float)(var29 - 0.5) * 0.89F;
                var29 = Math.random();
                var16 = (float)(var29 - 0.5) * 0.89F;
                var17 = (float)this.gaussianRandom(0.0, 0.10000000149011612);
                var18 = (float)this.gaussianRandom(0.0, 0.10000000149011612);
                var19 = 0.81F;
                var20 = this.rotateVector.x + this.lerp(0.0F, var7 > 0.0F ? var13 + var15 + var12 + var17 : -(var13 + var15 + var12 + var17), var19);
                var21 = MathHelper.clamp(this.rotateVector.y + this.lerp(0.0F, (var8 > 0.0F ? var14 + var16 : -(var14 + var16)) + var18, var19), -89.0F, 89.0F);
                var29 = Math.random() * 0.06700000166893005;
                var22 = (float)(var29 - 0.03400000184774399);
                var29 = Math.random() * 0.06700000166893005;
                var23 = (float)(var29 - 0.03400000184774399);
                var20 += var22;
                var21 += var23;
                var24 = SensUtils.getGCDValue();
                var31 = this.rotateVector.x;
                var32 = (var20 - var31) % var24;
                var20 -= var32;
                var31 = this.rotateVector.y;
                var32 = (var21 - var31) % var24;
                var21 -= var32;
                this.rotateVector = new Vector2f(var20, var21);
                this.lastYaw = var13;
                this.lastPitch = var14;
                if ((Boolean)this.options.getValueByName("Коррекция движения").get()) {
                    var10000 = IMinecraft.mc;
                    mc.player.rotationYawOffset = var20;
                }
                break;
            case "HolyWorld":
                var12 = (float)(Math.cos((double)(System.currentTimeMillis() % 1000L) / 1000.0 * Math.PI * 2.0) * 1.2300000190734863);
                var13 = Math.min(Math.max(Math.abs(var7), 1.3F), var2);
                var14 = Math.min(Math.max(Math.abs(var8), 1.24F), var3);
                if (var1 && this.selected != this.target && (Boolean)this.options.getValueByName("Ускорять ротацию").get()) {
                    var14 = Math.max(Math.abs(var8), 1.0F);
                } else {
                    var14 /= 3.0F;
                }

                var32 = this.lastYaw;
                if (Math.abs(var13 - var32) <= 3.0F) {
                    var13 = this.lastYaw + 3.1F;
                }

                var15 = -1.45F;
                long var26 = System.currentTimeMillis();
                var18 = (float)((new Random(var26)).nextGaussian() * 0.5669999718666077);
                var19 = (float)((new Random(var26 + 1000L)).nextGaussian() * 0.5669999718666077);
                var20 = 1.14F;
                var21 = this.rotateVector.x + this.lerpnew(0.0F, var7 > 0.0F ? var13 + var18 + var12 : -(var13 + var18 + var12), var20);
                var22 = MathHelper.clamp(this.rotateVector.y + this.lerpnew(0.0F, (var8 > 0.0F ? var14 + var19 : -(var14 + var19)) + var15, var20), -89.0F, 89.0F);
                var29 = (new Random(var26 + 2000L)).nextGaussian() * 0.05000000074505806;
                var23 = (float)(var29 - 0.02800000086426735);
                var29 = (new Random(var26 + 3000L)).nextGaussian() * 0.05000000074505806;
                var24 = (float)(var29 - 0.02800000086426735);
                var21 += var23;
                var22 += var24;
                float var25 = SensUtils.getGCDValue();
                var31 = this.rotateVector.x;
                var32 = (var21 - var31) % var25;
                var21 -= var32;
                var31 = this.rotateVector.y;
                var32 = (var22 - var31) % var25;
                var22 -= var32;
                this.rotateVector = new Vector2f(var21, var22);
                this.lastYaw = var13;
                this.lastPitch = var14;
                if ((Boolean)this.options.getValueByName("Коррекция движения").get()) {
                    var10000 = IMinecraft.mc;
                    mc.player.rotationYawOffset = var21;
                }
                break;
            case "Noclips":
                var12 = Math.min(Math.max(Math.abs(var7), 1.4F), var2);
                var13 = Math.min(Math.max(Math.abs(var8), 1.2F), var3);
                var29 = Math.random() * 6.0;
                var14 = (float)(var29 - 3.0);
                var29 = Math.random() * 0.85;
                var15 = (float)(var29 - 0.425);
                if (var1 && this.selected != this.target && (Boolean)this.options.getValueByName("Ускорять ротацию").get()) {
                    var13 = Math.max(Math.abs(var8), 1.0F);
                } else {
                    var13 /= 3.0F;
                }

                var16 = this.rotateVector.x + (var7 > 0.0F ? var12 : -var12);
                var17 = MathHelper.clamp(this.rotateVector.y + (var8 > 0.0F ? var13 : -var13), -89.0F, 89.0F);
                var18 = var16 + var14;
                var19 = var17 + var15;
                var20 = SensUtils.getGCDValue();
                var31 = this.rotateVector.x;
                var32 = (var18 - var31) % var20;
                var18 -= var32;
                var31 = this.rotateVector.y;
                var32 = (var19 - var31) % var20;
                var19 -= var32;
                this.rotateVector = new Vector2f(var18, var19);
                this.lastYaw = var12;
                this.lastPitch = var13;
                if ((Boolean)this.options.getValueByName("Коррекция движения").get()) {
                    var10000 = IMinecraft.mc;
                    mc.player.rotationYawOffset = var18;
                }
        }

    }

    private float lerpnew(float var1, float var2, float var3) {
        return var1 + var3 * (var2 - var1);
    }

    private float clamper(float var1, float var2, float var3) {
        return Math.max(var2, Math.min(var3, var1));
    }

    private double gaussianRandom(double var1, double var3) {
        return var1 + CustomColors.random.nextGaussian() * var3;
    }

    private void scanHitbox(Entity var1) {
        AxisAlignedBB var2 = var1.getBoundingBox();
        System.out.println("Hitbox Min: " + var2.minX + ", " + var2.minY + ", " + var2.minZ);
        System.out.println("Hitbox Max: " + var2.maxX + ", " + var2.maxY + ", " + var2.maxZ);
    }

    private float lerp(float var1, float var2, float var3) {
        return var1 + var3 * (var2 - var1);
    }

    private void updateAttack() {
        this.selected = MouseUtil.getMouseOver(this.target, this.rotateVector.x, this.rotateVector.y, (double)(Float)this.attackRange.get());
        if ((Boolean)this.options.getValueByName("Ускорять ротацию").get()) {
            this.updateRotation(true, 60.0F, 35.0F);
        }

        Minecraft var10000;
        if (this.selected == null || this.selected != this.target) {
            var10000 = IMinecraft.mc;
            if (!mc.player.isElytraFlying()) {
                return;
            }
        }

        var10000 = IMinecraft.mc;
        Minecraft var10001;
        if (mc.player.isBlocking() && (Boolean)this.options.getValueByName("Отжимать щит").get()) {
            var10001 = IMinecraft.mc;
            IMinecraft.mc.playerController.onStoppedUsingItem(mc.player);
        }

        this.stopWatch.setLastMS(500L);
        var10000 = IMinecraft.mc;
        mc.player.setSprinting(false);
        var10001 = IMinecraft.mc;
        IMinecraft.mc.playerController.attackEntity(mc.player, this.target);
        var10000 = IMinecraft.mc;
        mc.player.swingArm(Hand.MAIN_HAND);
        LivingEntity var2 = this.target;
        if (var2 instanceof PlayerEntity var1) {
            if ((Boolean)this.options.getValueByName("Ломать щит").get()) {
                this.breakShieldPlayer(var1);
            }
        }

    }

    private boolean shouldPlayerFalling() {
        Minecraft var10000;
        boolean var3;
        label46: {
            label45: {
                var10000 = IMinecraft.mc;
                if (mc.player.isInWater()) {
                    var10000 = IMinecraft.mc;
                    if (mc.player.areEyesInFluid(FluidTags.WATER)) {
                        break label45;
                    }
                }

                var10000 = IMinecraft.mc;
                if (!mc.player.isInLava()) {
                    var10000 = IMinecraft.mc;
                    if (!mc.player.isOnLadder()) {
                        var10000 = IMinecraft.mc;
                        if (!mc.player.isPassenger()) {
                            var10000 = IMinecraft.mc;
                            if (!mc.player.abilities.isFlying) {
                                var3 = false;
                                break label46;
                            }
                        }
                    }
                }
            }

            var3 = true;
        }

        boolean var1 = var3;
        var10000 = IMinecraft.mc;
        float var2 = mc.player.getCooledAttackStrength((Boolean)this.options.getValueByName("ТПС Синхрон").get() ? Rarity.getInstance().getTpsCalc().getAdjustTicks() : 1.5F);
        if (var2 < 0.92F) {
            return false;
        } else if (!var1 && (Boolean)this.options.getValueByName("Только криты").get()) {
            var10000 = IMinecraft.mc;
            if (!mc.player.isOnGround()) {
                var10000 = IMinecraft.mc;
                if (mc.player.fallDistance > 0.0F) {
                    var3 = true;
                    return var3;
                }
            }

            var3 = false;
            return var3;
        } else {
            return true;
        }
    }

    private boolean isValid(LivingEntity var1) {
        double var2 = (double)((Float)this.attackRange.get() + (Float)this.aimDistance.get());
        if (var1 instanceof PlayerEntity && AntiBot.isBot(var1) && ((PlayerEntity)var1).isBot) {
            return false;
        } else if (var1 instanceof ClientPlayerEntity) {
            return false;
        } else {
            if (var1.ticksExisted >= 3) {
                Minecraft var10000 = IMinecraft.mc;
                if (!((double)mc.player.getDistance(var1) > var2)) {
                    if (var1 instanceof PlayerEntity) {
                        PlayerEntity var4 = (PlayerEntity)var1;
                        if (AntiBot.isBot(var1)) {
                            return false;
                        }

                        if (!(Boolean)this.targets.getValueByName("Друзья").get() && FriendStorage.isFriend(var4.getName().getString())) {
                            return false;
                        }

                        String var5 = var4.getName().getString();
                        Minecraft var10001 = IMinecraft.mc;
                        if (var5.equalsIgnoreCase(mc.player.getName().getString())) {
                            return false;
                        }
                    }

                    if (var1 instanceof PlayerEntity && !(Boolean)this.targets.getValueByName("Игроки").get()) {
                        return false;
                    }

                    if (var1 instanceof PlayerEntity && var1.getTotalArmorValue() == 0 && !(Boolean)this.targets.getValueByName("Голые").get()) {
                        return false;
                    }

                    if (var1 instanceof PlayerEntity && var1.isInvisible() && var1.getTotalArmorValue() == 0 && !(Boolean)this.targets.getValueByName("Голые невидимки").get()) {
                        return false;
                    }

                    if (var1 instanceof PlayerEntity && var1.isInvisible() && !(Boolean)this.targets.getValueByName("Невидимки").get()) {
                        return false;
                    }

                    if (var1 instanceof MonsterEntity && !(Boolean)this.targets.getValueByName("Мобы").get()) {
                        return false;
                    }

                    if (var1 instanceof AnimalEntity && !(Boolean)this.targets.getValueByName("Животные").get()) {
                        return false;
                    }

                    return !var1.isInvulnerable() && var1.isAlive() && !(var1 instanceof ArmorStandEntity);
                }
            }

            return false;
        }
    }

    private void breakShieldPlayer(PlayerEntity var1) {
        if (var1.isBlocking()) {
            int var2 = InventoryUtil.getInstance().getAxeInInventory(false);
            int var3 = InventoryUtil.getInstance().getAxeInInventory(true);
            Minecraft var10000;
            Minecraft var10001;
            Minecraft var10003;
            ClientPlayNetHandler var5;
            if (var3 == -1 && var2 != -1) {
                int var4 = InventoryUtil.getInstance().findBestSlotInHotBar();
                Minecraft var10005 = IMinecraft.mc;
                IMinecraft.mc.playerController.windowClick(0, var2, 0, ClickType.PICKUP, mc.player);
                int var10002 = var4 + 36;
                var10005 = IMinecraft.mc;
                IMinecraft.mc.playerController.windowClick(0, var10002, 0, ClickType.PICKUP, mc.player);
                var10000 = IMinecraft.mc;
                var5 = mc.player.connection;
                var5.sendPacket(new CHeldItemChangePacket(var4));
                var10001 = IMinecraft.mc;
                IMinecraft.mc.playerController.attackEntity(mc.player, var1);
                var10000 = IMinecraft.mc;
                mc.player.swingArm(Hand.MAIN_HAND);
                var10000 = IMinecraft.mc;
                var5 = mc.player.connection;
                var10003 = IMinecraft.mc;
                var5.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                var10002 = var4 + 36;
                var10005 = IMinecraft.mc;
                IMinecraft.mc.playerController.windowClick(0, var10002, 0, ClickType.PICKUP, mc.player);
                var10005 = IMinecraft.mc;
                IMinecraft.mc.playerController.windowClick(0, var2, 0, ClickType.PICKUP, mc.player);
            }

            if (var3 != -1) {
                var10000 = IMinecraft.mc;
                var5 = mc.player.connection;
                var5.sendPacket(new CHeldItemChangePacket(var3));
                var10001 = IMinecraft.mc;
                IMinecraft.mc.playerController.attackEntity(mc.player, var1);
                var10000 = IMinecraft.mc;
                mc.player.swingArm(Hand.MAIN_HAND);
                var10000 = IMinecraft.mc;
                var5 = mc.player.connection;
                var10003 = IMinecraft.mc;
                var5.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            }
        }

    }

    private void reset() {
        if ((Boolean)this.options.getValueByName("Коррекция движения").get()) {
            Minecraft var10000 = IMinecraft.mc;
            mc.player.rotationYawOffset = -2.14748365E9F;
        }

        Minecraft var10003 = IMinecraft.mc;
        Minecraft var10004 = IMinecraft.mc;
        this.rotateVector = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
    }

    public boolean onEnable() {
        super.onEnable();
        this.reset();
        this.target = null;
        return false;
    }

    public void onDisable() {
        super.onDisable();
        this.reset();
        this.stopWatch.setLastMS(0L);
        this.target = null;
    }

    private double getEntityArmor(PlayerEntity var1) {
        double var2 = 0.0;

        for(int var4 = 0; var4 < 4; ++var4) {
            ItemStack var5 = (ItemStack)var1.inventory.armorInventory.get(var4);
            if (var5.getItem() instanceof ArmorItem) {
                var2 += this.getProtectionLvl(var5);
            }
        }

        return var2;
    }

    private double getProtectionLvl(ItemStack var1) {
        Item var3 = var1.getItem();
        if (var3 instanceof ArmorItem var2) {
            double var5 = (double)var2.getDamageReduceAmount();
            if (var1.isEnchanted()) {
                var5 += (double)EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, var1) * 0.25;
            }

            return var5;
        } else {
            return 0.0;
        }
    }

    private double getEntityHealth(Entity var1) {
        if (var1 instanceof PlayerEntity var2) {
            double var4 = this.getEntityArmor(var2) / 20.0;
            return (double)(var2.getHealth() + var2.getAbsorptionAmount()) * var4;
        } else if (var1 instanceof LivingEntity var3) {
            return (double)(var3.getHealth() + var3.getAbsorptionAmount());
        } else {
            return 0.0;
        }
    }

    public ModeSetting getType() {
        return this.type;
    }

    public ModeListSetting getOptions() {
        return this.options;
    }

    public StopWatch getStopWatch() {
        return this.stopWatch;
    }

    public LivingEntity getTarget() {
        return this.target;
    }
}
