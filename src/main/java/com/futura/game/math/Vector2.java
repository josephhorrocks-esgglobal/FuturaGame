package com.futura.game.math;

public record Vector2(double x, double y) {
    public Vector2 add(Vector2 other) {
        return new Vector2(x + other.x, y + other.y);
    }

    public Vector2 subtract(Vector2 other) {
        return new Vector2(x - other.x, y - other.y);
    }

    public Vector2 scale(double factor) {
        return new Vector2(x * factor, y * factor);
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    public Vector2 normalize() {
        double len = length();
        if (len == 0.0) {
            return new Vector2(0.0, 0.0);
        }
        return new Vector2(x / len, y / len);
    }

    public static double distance(Vector2 a, Vector2 b) {
        return a.subtract(b).length();
    }

    public static Vector2 fromAngle(double angleRadians) {
        return new Vector2(Math.cos(angleRadians), Math.sin(angleRadians));
    }
}
