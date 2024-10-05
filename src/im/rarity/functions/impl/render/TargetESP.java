package im.rarity.functions.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import im.rarity.Rarity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import im.rarity.events.EventDisplay;
import im.rarity.events.WorldEvent;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.functions.impl.combat.KillAura;
import im.rarity.functions.settings.Setting;
import im.rarity.functions.settings.impl.ModeSetting;
import im.rarity.functions.settings.impl.SliderSetting;
import im.rarity.utils.math.Vector4i;
import im.rarity.utils.projections.ProjectionUtil;
import im.rarity.utils.render.ColorUtils;
import im.rarity.utils.render.DisplayUtils;

@FunctionRegister(
        name = "TargetESP", type = Category.Render, description = "79")
public class TargetESP extends Function {

    private final KillAura killAura;
    public ModeSetting mod = new ModeSetting("Мод", "Client", new String[]{"Client", "Призраки", "Арбуз"});
    public SliderSetting speed = new SliderSetting("Скорость", 3.0F, 0.7F, 9.0F, 1.0F);
    public SliderSetting size = new SliderSetting("Размер", 30.0F, 5.0F, 140.0F, 1.0F);
    public SliderSetting bright = new SliderSetting("Яркость", 255.0F, 1.0F, 255.0F, 1.0F);

    public TargetESP(KillAura killAura) {
        super("EnhancedStats", Category.Render);
        this.killAura = killAura;
        this.addSettings(new Setting[]{this.mod, this.speed, this.size, this.bright});
    }

    @Subscribe
    private void onDisplay(EventDisplay e) {
        if (this.mod.is("Client")) {
            if (e.getType() != EventDisplay.Type.PRE) {
                return;
            }

            if (this.killAura.isState() && this.killAura.getTarget() != null) {
                double sin = Math.sin((double) System.currentTimeMillis() / 1000.0);
                float size = 70.0F;
                Vector3d interpolated = this.killAura.getTarget().getPositon(e.getPartialTicks());
                Vector2f pos = ProjectionUtil.project(interpolated.x, interpolated.y + (double) (this.killAura.getTarget().getHeight() / 2.0F), interpolated.z);
                RenderSystem.pushMatrix();
                RenderSystem.translatef(pos.x, pos.y, 0.0F);
                RenderSystem.rotatef((float) sin * 360.0F, 0.0F, 0.0F, 1.0F);
                RenderSystem.translatef(-pos.x, -pos.y, 0.0F);
                DisplayUtils.drawImage(new ResourceLocation("rarity/images/target.png"), pos.x - size / 2.0F, pos.y - size / 2.0F, size, size, new Vector4i(ColorUtils.rgb(255, 255, 255), ColorUtils.setAlpha(HUD.getColor(90, 1.0F), 220), ColorUtils.setAlpha(HUD.getColor(180, 1.0F), 220), ColorUtils.setAlpha(HUD.getColor(270, 1.0F), 220)));
                RenderSystem.popMatrix();
            }
        }

        if (this.mod.is("Призраки")) {
            if (e.getType() != EventDisplay.Type.PRE) {
                return;
            }

            if (this.killAura.isState() && this.killAura.getTarget() != null) {
                float speedi = (Float) this.speed.get();
                float sizik = (Float) this.size.get();
                int yarkost = ((Float) this.bright.get()).intValue();
                double speed = (double) speedi;
                double time = (double) System.currentTimeMillis() / (500.0 / speed);
                double sin = Math.sin(time);
                double cos = Math.cos(time);
                float size = sizik;
                int brightness = yarkost;
                Vector3d headPos = this.killAura.getTarget().getPositon(e.getPartialTicks()).add(0.0, (double) this.killAura.getTarget().getHeight(), 0.0);
                Vector3d bodyPos = this.killAura.getTarget().getPositon(e.getPartialTicks()).add(0.0, (double) (this.killAura.getTarget().getHeight() / 2.0F), 0.0);
                Vector3d legPos = this.killAura.getTarget().getPositon(e.getPartialTicks());
                Vector3d[] upperPositions = new Vector3d[]{bodyPos.add(0.0, 0.5, 0.0)};
                Vector3d[] lowerPositions = new Vector3d[]{legPos.add(0.0, 0.5, 0.0)};
                Vector3d[] lowerPositions2 = new Vector3d[]{headPos.add(0.0, 0.5, 0.0)};
                ResourceLocation image = new ResourceLocation("rarity/images/hud/glow.png");
            }
        }
    }

    @Subscribe
    private void onWorldEvent(WorldEvent e) {
        if (this.mod.is("Арбуз")) {
            KillAura killAura = Rarity.getInstance().getFunctionRegistry().getKillAura();
            if (killAura.isState() && killAura.getTarget() != null) {
                MatrixStack ms = new MatrixStack();
                ms.push();
                RenderSystem.pushMatrix();
                RenderSystem.disableLighting();
                RenderSystem.depthMask(false);
                RenderSystem.enableBlend();
                RenderSystem.shadeModel(7425);
                RenderSystem.disableCull();
                RenderSystem.disableAlphaTest();
                RenderSystem.blendFuncSeparate(770, 1, 0, 1);
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();
                RenderSystem.enableCull();
                RenderSystem.enableAlphaTest();
                RenderSystem.depthMask(true);
                RenderSystem.popMatrix();
                ms.pop();
            }
        }
    }
}
