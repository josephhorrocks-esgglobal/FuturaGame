package com.futura.game.world;

public enum MapType {
    MAP_1("Map_1"),
    CITY_MAP("City_Map");

    private final String displayName;

    MapType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
