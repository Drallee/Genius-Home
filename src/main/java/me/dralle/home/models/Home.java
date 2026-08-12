package me.dralle.home.models;

import org.bukkit.Location;
import java.util.UUID;

public class Home {
    private UUID targetUUID;
    private String homeName;
    private String iconType;
    private String skullMeta;
    private String sound;
    private Location location;

    public Home(UUID targetUUID, String homeName, String iconType, String skullMeta, String sound, Location location) {
        this.targetUUID = targetUUID;
        this.homeName = homeName;
        this.iconType = iconType;
        this.skullMeta = skullMeta;
        this.sound = sound;
        this.location = location;
    }

    public void setHomeName(String homeName) {
        this.homeName = homeName;
    }

    public void setIconType(String iconType) {
        this.iconType = iconType;
    }

    public void setSkullMeta(String skullMeta) {
        this.skullMeta = skullMeta;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public UUID getTargetUUID() {
        return targetUUID;
    }
    public String getHomeName() {
        return homeName;
    }
    public String getIconType() {
        return iconType;
    }
    public String getSkullMeta() {
        return skullMeta;
    }
    public String getSound() {
        return sound;
    }
    public Location getLocation() {
        return location;
    }
}
