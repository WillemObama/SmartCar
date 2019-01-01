package com.lincolnwang.BlueDot;

public class Vector2 {

    private double x;
    private double y;

    public static Vector2 UP = new Vector2(0,1);
    public static Vector2 DOWN = new Vector2(0,-1);
    public static Vector2 LEFT = new Vector2(-0.89,0.42);
    public static Vector2 RIGHT = new Vector2(0.84,0.45);
    public static Vector2 STOP = new Vector2(0,0);

    public Vector2(double x,double y){
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}
