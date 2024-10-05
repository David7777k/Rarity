package im.rarity.functions.impl.player;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import im.rarity.events.EventDisplay;
import im.rarity.events.EventDisplay.Type;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.functions.settings.Setting;
import im.rarity.functions.settings.impl.ModeSetting;
import im.rarity.utils.math.MathUtil;
import im.rarity.utils.projections.ProjectionUtil;
import im.rarity.utils.render.ColorUtils;
import im.rarity.utils.render.DisplayUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@FunctionRegister(
        name = "VulcanESP", type = Category.Render, description = "1")
public class VulcanESP extends Function {
    private final HashMap<Entity, Vector4f> positions = new HashMap<>();
    private final ModeSetting modeSetting = new ModeSetting("Mode", "All", new String[]{"Diamonds", "Player_Heads", "Totems", "NetheriteItems", "All"});

    // Вызов конструктора базового класса Function
    public VulcanESP() {
        super("VulcanESP", Category.Render);  // Передаем название и категорию в конструктор базового класса
        this.addSettings(new Setting[]{this.modeSetting});
    }

    @Subscribe
    public void onDisplay(EventDisplay e) {
        if (mc.world != null && e.getType() == Type.PRE) {
            this.positions.clear();
            Iterator<Entity> entityIterator = mc.world.getAllEntities().iterator();

            while (entityIterator.hasNext()) {
                Entity entity = entityIterator.next();
                if (entity instanceof ItemEntity) {
                    ItemEntity itemEntity = (ItemEntity) entity;
                    ItemStack itemStack = itemEntity.getItem();

                    // Проверка типа предмета
                    boolean isDiamond = itemStack.getItem() == Items.DIAMOND;
                    boolean isPlayerHead = itemStack.getItem() == Items.PLAYER_HEAD;
                    boolean isTotem = itemStack.getItem() == Items.TOTEM_OF_UNDYING;
                    boolean isTripwireHook = itemStack.getItem() == Items.TRIPWIRE_HOOK;
                    boolean isNetheriteIngot = itemStack.getItem() == Items.NETHERITE_INGOT;
                    boolean isNetheritePickaxe = itemStack.getItem() == Items.NETHERITE_PICKAXE;
                    boolean isNetheriteSword = itemStack.getItem() == Items.NETHERITE_SWORD;

                    // Если предмет видим в соответствии с настройками
                    if (this.isEntityVisible(isDiamond, isPlayerHead, isTotem, isTripwireHook, isNetheriteIngot, isNetheritePickaxe, isNetheriteSword)) {
                        double x = MathUtil.interpolate(entity.getPosX(), entity.lastTickPosX, e.getPartialTicks());
                        double y = MathUtil.interpolate(entity.getPosY(), entity.lastTickPosY, e.getPartialTicks());
                        double z = MathUtil.interpolate(entity.getPosZ(), entity.lastTickPosZ, e.getPartialTicks());

                        Vector3d size = new Vector3d(entity.getBoundingBox().maxX - entity.getBoundingBox().minX, entity.getBoundingBox().maxY - entity.getBoundingBox().minY, entity.getBoundingBox().maxZ - entity.getBoundingBox().minZ);
                        AxisAlignedBB aabb = new AxisAlignedBB(x - size.x / 1.5, y, z - size.z / 1.5, x + size.x / 1.5, y + size.y + 0.1, z + size.z / 1.5);
                        Vector4f position = null;

                        // Проекция 8 точек
                        for (int i = 0; i < 8; ++i) {
                            Vector2f vector = ProjectionUtil.project(i % 2 == 0 ? aabb.minX : aabb.maxX, i / 2 % 2 == 0 ? aabb.minY : aabb.maxY, i / 4 % 2 == 0 ? aabb.minZ : aabb.maxZ);
                            if (position == null) {
                                position = new Vector4f(vector.x, vector.y, 1.0F, 1.0F);
                            } else {
                                position.x = Math.min(vector.x, position.x);
                                position.y = Math.min(vector.y, position.y);
                                position.z = Math.max(vector.x, position.z);
                                position.w = Math.max(vector.y, position.w);
                            }
                        }

                        this.positions.put(entity, position);
                    }
                }
            }

            // Настройки рендеринга
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.defaultBlendFunc();
            RenderSystem.shadeModel(7425);
            BufferBuilder builder = Tessellator.getInstance().getBuffer();
            builder.begin(7, DefaultVertexFormats.POSITION_COLOR);

            // Рендеринг каждого элемента
            for (Map.Entry<Entity, Vector4f> entry : this.positions.entrySet()) {
                Vector4f position = entry.getValue();
                ItemEntity itemEntity = (ItemEntity) entry.getKey();
                int color = 0;

                // Установка цвета для каждого элемента
                if (itemEntity.getItem().getItem() == Items.DIAMOND) {
                    color = ColorUtils.rgba(0, 0, 255, 255);
                } else if (itemEntity.getItem().getItem() == Items.PLAYER_HEAD) {
                    color = ColorUtils.rgba(255, 0, 0, 255);
                } else if (itemEntity.getItem().getItem() == Items.TOTEM_OF_UNDYING) {
                    color = ColorUtils.rgba(255, 128, 0, 255);
                } else if (itemEntity.getItem().getItem() == Items.TRIPWIRE_HOOK) {
                    color = ColorUtils.rgba(128, 0, 128, 255);
                } else if (itemEntity.getItem().getItem() == Items.NETHERITE_INGOT) {
                    color = ColorUtils.rgba(0, 255, 0, 255);
                } else if (itemEntity.getItem().getItem() == Items.NETHERITE_PICKAXE) {
                    color = ColorUtils.rgba(255, 0, 127, 255);
                } else if (itemEntity.getItem().getItem() == Items.NETHERITE_SWORD) {
                    color = ColorUtils.rgba(102, 255, 255, 255);
                }

                DisplayUtils.drawBox((double) position.x, (double) position.y, (double) position.z, (double) position.w, 1.5, color);
            }

            Tessellator.getInstance().draw();
            RenderSystem.shadeModel(7424);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }
    }

    // Проверка видимости сущности
    private boolean isEntityVisible(boolean isDiamond, boolean isPlayerHead, boolean isTotem, boolean isTripwireHook, boolean isNetheriteIngot, boolean isNetheritePickaxe, boolean isNetheriteSword) {
        switch (this.modeSetting.getName()) {
            case "Diamonds":
                return isDiamond;
            case "Player_Heads":
                return isPlayerHead;
            case "Totems":
                return isTotem;
            case "NetheriteIngot":
            case "NetheritePickaxe":
            case "NetheriteSword":
                return isNetheriteIngot || isNetheritePickaxe || isNetheriteSword;
            case "TRIPWIRE_HOOK":
                return isTripwireHook;
            case "All":
            default:
                return isDiamond || isPlayerHead || isTotem || isTripwireHook || isNetheriteIngot || isNetheritePickaxe || isNetheriteSword;
        }
    }
}
