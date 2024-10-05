package im.rarity.events;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventDisplay {
    MatrixStack matrixStack;
    float partialTicks;
    Type type;

    public EventDisplay(MatrixStack matrixStack, float partialTicks) {
        this.matrixStack = matrixStack;
        this.partialTicks = partialTicks;
    }

    public MatrixStack getMatrix() {
        return null;
    }

    public double getMouseX() {
        return 0;
    }

    public double getMouseY() {
        return 0;
    }

    public enum Type {
        PRE, POST, HIGH
    }
}
