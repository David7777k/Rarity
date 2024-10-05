package im.rarity.functions.impl.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SChatPacket;
import im.rarity.events.EventPacket;
import im.rarity.events.EventUpdate;
import im.rarity.functions.api.Category;
import im.rarity.functions.api.Function;
import im.rarity.functions.api.FunctionRegister;
import im.rarity.utils.math.StopWatch;

import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@FunctionRegister(
        name = "CasinoBOT", type = Category.Player, description = "55")
public class CasinoBOT extends Function {
    private final StopWatch timer = new StopWatch();
    private PlayerEntity currentPlayer;
    private int wallet;
    private final Random randomizer = new Random();

    public CasinoBOT() {
        // Конструктор без параметров
        super("EnhancedStats", Category.Render);
    }

    @Subscribe
    public void onGameTick(EventUpdate event) {
        if (timer.isReached(20000L)) {
            ClientPlayerEntity player = Minecraft.player;
            String playerName = player.getScoreboardName();
            player.sendChatMessage("!Привет! Я - ваш личный  Казино-Бот. Проверьте свою удачу! /pay " + playerName + " количество. В случае успеха, я удвою вашу сумму. Минимальная сумма для игры - 5000.");
            timer.reset();
        }
    }

    @Subscribe
    public void onIncomingPacket(EventPacket event) {
        IPacket packet = event.getPacket();
        if (packet instanceof SChatPacket chatPacket) {
            String messageContent = chatPacket.getChatComponent().getString().toLowerCase(Locale.ROOT);
            if (messageContent.contains("получено от игрока ")) {
                String[] messageWords = messageContent.split(" ");
                String sender = messageWords[messageWords.length - 1];

                Pattern pattern = Pattern.compile("\\$(\\d{1,3}(,\\d{3})*)");
                Matcher matcher = pattern.matcher(messageContent);
                int sum = 0;

                if (matcher.find()) {
                    String extractedAmount = matcher.group(1).replace(",", "");
                    sum = Integer.parseInt(extractedAmount);
                }

                if (sum < 5000) {
                    Minecraft.player.sendChatMessage("/m " + sender + " Минимальная сумма для участия - 5000 монет.");
                    return;
                }

                boolean isWinner = randomizer.nextDouble() < 0.35; // 35% вероятность выигрыша

                if (isWinner) {
                    Minecraft.player.sendChatMessage("/m " + sender + " Поздравляю, вы выиграли!");
                    int reward = sum * 2;
                    Minecraft.player.sendChatMessage("/pay " + sender + " " + reward);
                    Minecraft.player.sendChatMessage("/pay " + sender + " " + reward);
                } else {
                    Minecraft.player.sendChatMessage("/m " + sender + " К сожалению, вы проиграли.");
                }
            }
        }
    }
}