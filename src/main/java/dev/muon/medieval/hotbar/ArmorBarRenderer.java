package dev.muon.medieval.hotbar;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.muon.medieval.Medieval;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public class ArmorBarRenderer {
    private enum ArmorIcon {
        NONE("armor_icon_0"),
        LIGHT("armor_icon_1"),
        MEDIUM("armor_icon_2"),
        HEAVY("armor_icon_3"),
        PLATE("armor_icon_4"),
        REINFORCED("armor_icon_5"),
        ENHANCED("armor_icon_6"),
        MASTERWORK("armor_icon_7"),
        LEGENDARY("armor_icon_8"),
        MYTHICAL("armor_icon_9"),
        DIVINE("armor_icon_10");

        private final String texture;

        ArmorIcon(String texture) {
            this.texture = texture;
        }

        public String getTexture() {
            return texture;
        }

        public static ArmorIcon fromArmorValue(int armorValue) {
            int tier = Math.min(10, armorValue / 10);
            return values()[tier];
        }
    }

    public static void render(GuiGraphics graphics, Player player) {
        Position armorPos = HUDPositioning.getArmorAnchor()
                .offset(HUDPositioning.getArmorBarXOffset(), HUDPositioning.getArmorBarYOffset());

        // TODO: Move to config
        int borderWidth = 80;
        int borderHeight = 10;
        int barWidth = 74;
        int barHeight = 4;
        int barXOffset = 3;
        int barYOffset = 3;
        int iconSize = 16;

        int xPos = armorPos.x();
        int yPos = armorPos.y();

        graphics.blit(
                Medieval.loc("textures/gui/armor_border.png"), xPos, yPos, 0, 0, borderWidth, borderHeight, 256, 256
        );

        int armorValue = player.getArmorValue();
        float armorPercent = Math.min(1.0f, armorValue / 100.0f);

        int filledWidth = Math.round((barWidth - iconSize/2) * armorPercent);
        if (filledWidth > 0) {
            graphics.blit(
                    Medieval.loc("textures/gui/armor_bar.png"),
                    xPos + barXOffset + iconSize/2,
                    yPos + barYOffset,
                    0, 0,
                    filledWidth,
                    barHeight,
                    256, 256
            );
        }

        ArmorIcon icon = ArmorIcon.fromArmorValue(armorValue);
        graphics.blit(
                Medieval.loc("textures/gui/" + icon.getTexture() + ".png"),
                xPos + barXOffset,
                yPos + (borderHeight - iconSize)/2 - 2,
                0, 0,
                iconSize, iconSize,
                256, 256
        );
    }
}