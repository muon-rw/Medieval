package dev.muon.medieval.leveling.event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

public class AptitudeChangedEvent extends Event {
    private final Player player;
    private final String aptitudeName;
    private final int oldLevel;
    private final int newLevel;

    public AptitudeChangedEvent(Player player, String aptitudeName, int oldLevel, int newLevel) {
        this.player = player;
        this.aptitudeName = aptitudeName;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    public Player getPlayer() {
        return player;
    }

    public String getAptitudeName() {
        return aptitudeName;
    }

    public int getOldLevel() {
        return oldLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }
}