package im.rarity.ui.display.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import im.rarity.command.staffs.StaffStorage;
import im.rarity.events.EventDisplay;
import im.rarity.events.EventUpdate;
import im.rarity.ui.display.ElementRenderer;
import im.rarity.ui.display.ElementUpdater;
import im.rarity.utils.drag.Dragging;
import im.rarity.utils.render.ColorUtils;
import im.rarity.utils.render.DisplayUtils;
import im.rarity.utils.render.Scissor;
import im.rarity.utils.render.font.Fonts;
import im.rarity.utils.text.GradientUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;


@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class StaffListRenderer implements ElementRenderer, ElementUpdater {


    final Dragging dragging;
    final ResourceLocation logo = new ResourceLocation("rarity/images/hud/staff.png");
    float iconSizeX = 10;
    float iconSizeY = 10;


    private final List<Staff> staffPlayers = new ArrayList<>();
    private final Pattern namePattern = Pattern.compile("^\\w{3,16}$");
    private final Pattern prefixMatches = Pattern.compile(".*(mod|der|adm|help|wne|хелп|адм|поддержка|кура|own|taf|curat|dev|supp|yt|сотруд).*");



    @Override
    public void update(EventUpdate e) {
        staffPlayers.clear();

        for (ScorePlayerTeam team : mc.world.getScoreboard().getTeams().stream().sorted(Comparator.comparing(Team::getName)).toList()) {
            String name = team.getMembershipCollection().toString().replaceAll("[\\[\\]]", "");
            boolean vanish = true;
            for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
                if (info.getGameProfile().getName().equals(name)) {
                    vanish = false;
                }
            }
            if (namePattern.matcher(name).matches() && !name.equals(mc.player.getName().getString())) {
                if (!vanish) {
                    if (prefixMatches.matcher(team.getPrefix().getString().toLowerCase(Locale.ROOT)).matches() || StaffStorage.isStaff(name)) {
                        Staff staff = new Staff(team.getPrefix(), name, false, Status.NONE);
                        staffPlayers.add(staff);
                    }
                }
                if (vanish && !team.getPrefix().getString().isEmpty()) {
                    Staff staff = new Staff(team.getPrefix(), name, true, Status.VANISHED);
                    staffPlayers.add(staff);
                }
            }
        }
    }

    float width;
    float height;

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();

        float posX = dragging.getX();
        float posY = dragging.getY();
        float padding = 5;
        float fontSize = 6.5f;

        ITextComponent name = GradientUtil.gradient("Staff");
        String namemod = "Staff";

        float finalPosY = posY;
        drawStyledRect(posX, finalPosY, width, height, 5);

        DisplayUtils.drawShadow(posX, posY, width, height, 15, ColorUtils.rgba(21,24,40,165));

        Scissor.push();
        try {
            Scissor.setFromComponentCoordinates(posX, posY, width, height);
            Fonts.sfui.drawText(ms, namemod, posX + padding, posY + padding + 1, ColorUtils.rgb(255, 255, 255), fontSize);

            float imagePosX = posX + width - iconSizeX - padding;
            DisplayUtils.drawImage(logo, imagePosX, posY + 4f, iconSizeX, iconSizeY, ColorUtils.rgb(129, 135, 255));

            posY += fontSize + padding * 2;

            float maxWidth = Fonts.sfMedium.getWidth(name, fontSize) + padding * 2;
            float localHeight = fontSize + padding * 2;

            for (StaffListRenderer.Staff f : staffPlayers) {
                ITextComponent prefix = f.getPrefix();
                float prefixWidth = Fonts.sfMedium.getWidth(prefix, fontSize);
                String staff = (prefix.getString().isEmpty() ? "" : " ") + f.getName();
                float nameWidth = Fonts.sfMedium.getWidth(staff, fontSize);

                float localWidth = prefixWidth + nameWidth + Fonts.sfMedium.getWidth(f.getStatus().string, fontSize) + padding * 3;

                Fonts.sfMedium.drawText(ms, prefix, posX + padding, posY, fontSize, 255);
                Fonts.sfMedium.drawText(ms, staff, posX + padding + prefixWidth, posY, -1, fontSize);
                Fonts.sfMedium.drawText(ms, f.getStatus().string, posX + width - padding - Fonts.sfMedium.getWidth(f.getStatus().string, fontSize), posY, f.getStatus().color, fontSize);

                if (localWidth > maxWidth) {
                    maxWidth = localWidth;
                }

                posY += fontSize + padding;
                localHeight += fontSize + padding;
            }

            width = Math.max(maxWidth, 80);
            height = localHeight + 2.5f;
            dragging.setWidth(width);
            dragging.setHeight(height);
        } finally {
            Scissor.pop();
        }
    }
    @AllArgsConstructor
    @Data
    public static class Staff {
        ITextComponent prefix;
        String name;
        boolean isSpec;
        Status status;

        public void updateStatus() {
            for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
                if (info.getGameProfile().getName().equals(name)) {
                    if (info.getGameType() == GameType.SPECTATOR) {
                        return;
                    }
                    status = Status.NONE;
                    return;
                }
            }
            status = Status.VANISHED;
        }
    }

    public enum Status {
        NONE("", -1),
        VANISHED("V", ColorUtils.rgb(254, 68, 68));
        public final String string;
        public final int color;

        Status(String string, int color) {
            this.string = string;
            this.color = color;
        }
    }

    private void drawStyledRect(float x,
                                float y,
                                float width,
                                float height,
                                float radius) {

        DisplayUtils.drawRoundedRect(x - 0.5f, y - 0.5f, width + 1, height + 1, radius + 0.5f, ColorUtils.setAlpha(ColorUtils.rgb(21,24,40), 90));
        DisplayUtils.drawRoundedRect(x, y, width, height, radius, ColorUtils.rgba(21, 24, 40, 90));
    }
}

