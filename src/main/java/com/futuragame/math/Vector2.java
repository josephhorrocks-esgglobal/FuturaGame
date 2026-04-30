package com.futuragame.math;

public class Vector2 {
    public double x;
    public double y;

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2 copy() {
        return new Vector2(x, y);
    }

    public Vector2 add(Vector2 other) {
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    public Vector2 subtract(Vector2 other) {
        this.x -= other.x;
        this.y -= other.y;
        return this;
    }

    public Vector2 scale(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }

    public double length() {
        return Math.sqrt((x * x) + (y * y));
    }

    public Vector2 normalize() {
        double len = length();
        if (len > 0.0001) {
            this.x /= len;
            this.y /= len;
        }
        return this;
    }

    public static Vector2 fromAngle(double radians) {
        return new Vector2(Math.cos(radians), Math.sin(radians));
    }
}
