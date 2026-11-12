package dev.stevecount;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.*;

public class Settings {
    static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    public String username = "";
    public String preset = "Compact";
    public String icon = "Steve";
    public float scale = 1, opacity = .65f;
    public float x = 0, y = 0;
    public int offsetX = 8, offsetY = 8;
    public boolean background = true, border = true, liveLabel = true, abbreviated = false;
    public int textColor = 0xFFFFFFFF, backgroundColor = 0x101820;
    public boolean animations = true, sound = false, toast = true, autoConnect = false;
    public String milestones = "10,25,50,100,250,500,1000";
    static Path configDirectory = Path.of("config");
    private static Path path() { return configDirectory.resolve("stevecount.json"); }
    public void validate() {
        scale = Float.isFinite(scale) ? Math.clamp(scale, .5f, 3f) : 1;
        opacity = Float.isFinite(opacity) ? Math.clamp(opacity, 0, 1) : .65f;
        x = Float.isFinite(x) ? Math.clamp(x, 0, 1) : 0;
        y = Float.isFinite(y) ? Math.clamp(y, 0, 1) : 0;
        if (!java.util.List.of("Minimal","Compact","Streamer","Expanded").contains(preset)) preset="Compact";
        if (!java.util.List.of("Steve","Alex","Player","None").contains(icon)) icon="Steve";
        if (username == null) username="";
        if (milestones == null) milestones="";
    }
    public static Settings load() {
        try { var s = JSON.fromJson(Files.readString(path()), Settings.class); if (s != null) { s.validate(); return s; } }
        catch (Exception ignored) { }
        return new Settings();
    }
    public void save() {
        validate();
        try { Files.createDirectories(path().getParent()); var tmp = path().resolveSibling("stevecount.json.tmp");
            Files.writeString(tmp, JSON.toJson(this)); Files.move(tmp, path(), StandardCopyOption.REPLACE_EXISTING); }
        catch (Exception ex) { SteveCount.message = "Could not save settings: " + ex.getClass().getSimpleName(); }
    }
}

