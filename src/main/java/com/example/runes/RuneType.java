package com.example.runes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public enum RuneType {
    RED("red", "§cКрасная руна", TextColor.color(255, 60, 60)),
    BLUE("blue", "§bГолубая руна", TextColor.color(60, 180, 255)),
    GREEN("green", "§aЗелёная руна", TextColor.color(60, 200, 60)),
    ORANGE("orange", "§6Оранжевая руна", TextColor.color(255, 140, 0)),
    PURPLE("purple", "§5Фиолетовая руна", TextColor.color(160, 60, 255));

    private final String id;
    private final String displayName;
    private final TextColor color;

    RuneType(String id, String displayName, TextColor color) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public TextColor getColor() { return color; }

    public static RuneType fromString(String s) {
        for (RuneType r : values()) {
            if (r.id.equalsIgnoreCase(s)) return r;
        }
        return null;
    }
}
