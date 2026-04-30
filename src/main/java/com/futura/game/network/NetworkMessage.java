package com.futura.game.network;

public record NetworkMessage(Type type, double[] data) {

    public enum Type {
        POS,     // data: [x, y, rotation]
        SHOT,    // data: [muzzleX, muzzleY, rotation]
        HIT,     // remote's projectile hit me — I must apply damage to myself
        HEALTH,  // data: [health] — my health after taking a hit
        DEAD,    // I died — sender lost
        RESTART, // request round reset
        MAP      // data: [mapOrdinal] host-selected map
    }

    public static NetworkMessage parse(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        try {
            if (line.startsWith("POS:")) {
                String[] p = line.substring(4).split(",");
                return new NetworkMessage(Type.POS,
                        new double[]{Double.parseDouble(p[0]), Double.parseDouble(p[1]), Double.parseDouble(p[2])});
            }
            if (line.startsWith("SHOT:")) {
                String[] p = line.substring(5).split(",");
                return new NetworkMessage(Type.SHOT,
                        new double[]{Double.parseDouble(p[0]), Double.parseDouble(p[1]), Double.parseDouble(p[2])});
            }
            if (line.equals("HIT")) {
                return new NetworkMessage(Type.HIT, new double[0]);
            }
            if (line.startsWith("HEALTH:")) {
                return new NetworkMessage(Type.HEALTH, new double[]{Double.parseDouble(line.substring(7))});
            }
            if (line.equals("DEAD")) {
                return new NetworkMessage(Type.DEAD, new double[0]);
            }
            if (line.equals("RESTART")) {
                return new NetworkMessage(Type.RESTART, new double[0]);
            }
            if (line.startsWith("MAP:")) {
                return new NetworkMessage(Type.MAP, new double[]{Double.parseDouble(line.substring(4))});
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }
}
