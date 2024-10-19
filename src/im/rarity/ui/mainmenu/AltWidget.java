package im.rarity.ui.mainmenu;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.RandomStringUtils;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AltWidget extends Screen {

    final List<Account> accounts = new ArrayList<>();
    private float scroll;
    private float scrollSpeed = 0.0f;
    private String altName = "";
    private WidgetState currentState = WidgetState.VIEWING;

    private static final ResourceLocation BACKGROUND = new ResourceLocation("rarity/images/backmenu.png");
    private static final ResourceLocation STEVE_HEAD = new ResourceLocation("minecraft", "textures/entity/steve.png");
    private static final ResourceLocation ALEX_HEAD = new ResourceLocation("minecraft", "textures/entity/alex.png");

    private final Minecraft mc;

    public AltWidget() {
        super(new StringTextComponent("Добавьте новый аккаунт!"));
        this.mc = Minecraft.getInstance();
        loadAccounts(); // Load accounts from file during initialization
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackgroundTexture(matrixStack); // Render background
        renderInputField(matrixStack, mouseX, mouseY); // Render account input field
        renderAccountList(matrixStack, mouseX, mouseY); // Render account list
        renderButtons(matrixStack, mouseX, mouseY); // Render "Random Nick" and "Clear" buttons
    }

    private void renderBackgroundTexture(MatrixStack matrixStack) {
        mc.getTextureManager().bindTexture(BACKGROUND);
        blit(matrixStack, 0, 0, 0, 0, this.width, this.height, this.width, this.height);
    }

    private void renderInputField(MatrixStack matrixStack, int mouseX, int mouseY) {
        float x = this.width / 2f - 130f;
        float y = this.height / 2f - 150f;
        drawGradientRoundedRect(matrixStack, x, y, 260f, 30f, 5f, new Color(60, 60, 60, 180).getRGB(), new Color(80, 80, 80, 180).getRGB());
        drawBorder(matrixStack, x, y, 260f, 30f, 1f, Color.LIGHT_GRAY.getRGB());
        String inputText = currentState == WidgetState.TYPING || !altName.isEmpty() ? altName : "Введите имя аккаунта...";
        drawStringWithShadow(matrixStack, inputText, x + 10f, y + 10f, new Color(200, 200, 200).getRGB());
    }

    private void renderAccountList(MatrixStack matrixStack, int mouseX, int mouseY) {
        float x = this.width / 2f - 130f;
        float y = this.height / 2f - 100f;
        drawStringWithShadow(matrixStack, "Аккаунты:", x + 10, y - 20f, Color.WHITE.getRGB());
        drawStringWithShadow(matrixStack, "Выбери аккаунт из списка!", x + 10, y - 5f, new Color(180, 180, 180).getRGB());
        drawGradientRoundedRect(matrixStack, x, y, 260f, 200f, 5f, new Color(60, 60, 60, 180).getRGB(), new Color(80, 80, 80, 180).getRGB());
        drawBorder(matrixStack, x, y, 260f, 200f, 1f, Color.LIGHT_GRAY.getRGB());

        if (accounts.isEmpty()) {
            drawCenteredString(matrixStack, this.font, "Нет доступных аккаунтов", (int) (x + 130f), (int) (y + 100f), Color.WHITE.getRGB());
        }

        float iter = scroll;
        for (Account account : accounts) {
            float scrollY = y + iter * 40f;
            int accountColor = isHovered(mouseX, mouseY, x + 5f, scrollY, 250f, 40f) ? new Color(111, 131, 151).getRGB() : new Color(101, 101, 101).getRGB();
            drawGradientRoundedRect(matrixStack, x + 5f, scrollY, 250f, 40f, 5f, accountColor, new Color(121, 121, 121).getRGB());
            drawBorder(matrixStack, x + 5f, scrollY, 250f, 40f, 1f, new Color(140, 140, 140, 255).getRGB());

            ResourceLocation skin = account.isAlex() ? ALEX_HEAD : STEVE_HEAD;
            mc.getTextureManager().bindTexture(skin);
            blit(matrixStack, (int) (x + 10f), (int) (scrollY + 6f), 8, 8, 8, 8, 64, 64);
            drawStringWithShadow(matrixStack, account.getAccountName(), x + 30f, scrollY + 12f, Color.WHITE.getRGB());
            iter++;
        }
        drawStringWithShadow(matrixStack, "Ваш ник - " + (altName.isEmpty() ? "Не задан" : altName), x, y + 210f, new Color(255, 255, 255).getRGB());
    }

    private void renderButtons(MatrixStack matrixStack, int mouseX, int mouseY) {
        float x = this.width / 2f - 130f;
        float y = this.height / 2f + 130f;
        float buttonWidth = 125f;
        float buttonHeight = 25f;
        float spacing = 10f;

        // Render "Random Nick" button
        renderButton(matrixStack, x, y, buttonWidth, buttonHeight, "Рандом ник", mouseX, mouseY, () -> {
            altName = RandomStringUtils.randomAlphabetic(8);
            currentState = WidgetState.TYPING;
            saveAccount(); // Automatically save the new random account
            updateScreen();
        });

        // Render "Clear" button
        renderButton(matrixStack, x + buttonWidth + spacing, y, buttonWidth, buttonHeight, "Очистить", mouseX, mouseY, () -> {
            altName = "";
            currentState = WidgetState.VIEWING;
            updateScreen();
        });
    }

    private void renderButton(MatrixStack matrixStack, float x, float y, float width, float height, String label, int mouseX, int mouseY, Runnable onClick) {
        boolean hovered = isHovered(mouseX, mouseY, x, y, width, height);
        int buttonColor = hovered ? new Color(111, 131, 151).getRGB() : new Color(101, 101, 101).getRGB();
        drawGradientRoundedRect(matrixStack, x, y, width, height, 5f, buttonColor, new Color(121, 121, 121).getRGB());
        drawBorder(matrixStack, x, y, width, height, 1f, Color.LIGHT_GRAY.getRGB());
        drawStringWithShadow(matrixStack, label, x + 10f, y + 7f, Color.WHITE.getRGB());

        if (hovered && isMouseClicked(mouseX, mouseY)) {
            onClick.run();
        }
    }

    private boolean isHovered(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    private boolean isMouseClicked(int mouseX, int mouseY) {
        // Placeholder for mouse click detection
        return false;
    }

    private void drawStringWithShadow(MatrixStack matrixStack, String text, float x, float y, int color) {
        this.font.drawStringWithShadow(matrixStack, text, x, y, color);
    }

    private void drawGradientRoundedRect(MatrixStack matrixStack, float x, float y, float width, float height, float radius, int colorStart, int colorEnd) {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        float alphaStart = (colorStart >> 24 & 255) / 255.0F;
        float redStart = (colorStart >> 16 & 255) / 255.0F;
        float greenStart = (colorStart >> 8 & 255) / 255.0F;
        float blueStart = (colorStart & 255) / 255.0F;
        float alphaEnd = (colorEnd >> 24 & 255) / 255.0F;
        float redEnd = (colorEnd >> 16 & 255) / 255.0F;
        float greenEnd = (colorEnd >> 8 & 255) / 255.0F;
        float blueEnd = (colorEnd & 255) / 255.0F;
        buffer.pos(x, y + height, 0.0D).color(redStart, greenStart, blueStart, alphaStart).endVertex();
        buffer.pos(x + width, y + height, 0.0D).color(redEnd, greenEnd, blueEnd, alphaEnd).endVertex();
        buffer.pos(x + width, y, 0.0D).color(redEnd, greenEnd, blueEnd, alphaEnd).endVertex();
        buffer.pos(x, y, 0.0D).color(redStart, greenStart, blueStart, alphaStart).endVertex();
        Tessellator.getInstance().draw();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    private void drawBorder(MatrixStack matrixStack, float x, float y, float width, float height, float thickness, int color) {
        fill(matrixStack, (int) x, (int) y, (int) (x + width), (int) (y + thickness), color);
        fill(matrixStack, (int) x, (int) (y + height - thickness), (int) (x + width), (int) (y + height), color);
        fill(matrixStack, (int) x, (int) y, (int) (x + thickness), (int) (y + height), color);
        fill(matrixStack, (int) (x + width - thickness), (int) y, (int) (x + width), (int) (y + height), color);
    }

    public void updateScroll(int mouseX, int mouseY, float delta) {
        // Implement scroll logic with limits
        scroll += delta * scrollSpeed;
        scroll = Math.max(0, Math.min(scroll, accounts.size() - 5));
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY) {
    }

    public void onChar(char codePoint) {
        if (currentState == WidgetState.TYPING) {
            altName += codePoint;
            updateScreen();
        }
    }

    public void onKey(int keyCode) {
        if (keyCode == 259 && !altName.isEmpty()) { // Backspace key code
            altName = altName.substring(0, altName.length() - 1);
            updateScreen();
        }
    }

    public void click(int x, int y, int button) {
        float buttonWidth = 125f;
        float buttonHeight = 25f;
        float xBase = this.width / 2f - 130f;
        float yBase = this.height / 2f + 130f;
        float spacing = 10f;

        if (isHovered(x, y, xBase, yBase, buttonWidth, buttonHeight)) {
            altName = RandomStringUtils.randomAlphabetic(8); // Рандом ник
            currentState = WidgetState.TYPING;
            saveAccount(); // Automatically save the new random account
            updateScreen();
        } else if (isHovered(x, y, xBase + buttonWidth + spacing, yBase, buttonWidth, buttonHeight)) {
            altName = ""; // Очистить ник
            currentState = WidgetState.VIEWING;
            updateScreen();
        }
    }

    private void updateScreen() {
        this.minecraft.displayGuiScreen(this); // Перерисовка экрана, чтобы обновить изменения
    }

    private void saveAccount() {
        if (!altName.isEmpty()) {
            accounts.add(new Account(altName));
            saveAccounts(); // Save accounts to file after adding a new account
            altName = "";
            currentState = WidgetState.VIEWING;
            updateScreen();
        }
    }

    private void saveAccounts() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("accounts.dat"))) {
            out.writeObject(accounts);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadAccounts() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("accounts.dat"))) {
            accounts.addAll((List<Account>) in.readObject());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private enum WidgetState {
        TYPING,
        VIEWING
    }

    public static class Account implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String accountName;
        private final UUID id;
        private final long creationTime;

        public Account(String accountName) {
            this.accountName = accountName;
            this.id = UUID.randomUUID();
            this.creationTime = System.currentTimeMillis();
        }

        public String getAccountName() {
            return accountName;
        }

        public boolean isAlex() {
            return accountName.hashCode() % 2 == 0;
        }

        public long getCreationTime() {
            return creationTime;
        }

        public UUID getId() {
            return id;
        }
    }
}
