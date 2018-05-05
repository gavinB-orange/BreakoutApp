package com.example.brebner.breakoutapp;

import android.graphics.RectF;

public class Paddle {

    private RectF rect;
    private float height;
    private float length;

    // x is far left, y is top
    private float x;
    private float y;

    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;
    public final float PADDLESPEED = 400;

    private int paddleMoving = STOPPED;

    private int screenX;
    private int screenY;

    public Paddle(int screenX, int screenY) {

        length = screenX / 10;
        height = 20;

        this.screenX = screenX;
        this.screenY = screenY;

        this.reset();

        rect = new RectF(x,y, x + length, y + height);
    }

    public RectF getRect() {
        return rect;
    }

    public void setPaddleMoving(int state) {
        paddleMoving = state;
    }

    public void reset() {
        // start roughly in the center
        x = screenX / 2;
        y = screenY - 20;
    }

    public void update(long fps) {
        if (paddleMoving == LEFT) {
            x = x - PADDLESPEED / fps;
            if (x < 0) {
                x = 0;
            }
        }

        if (paddleMoving == RIGHT) {
            x = x + PADDLESPEED / fps;
            if (x > screenX) {
                x = screenX;
            }
        }

        rect.left = x;
        rect.right = x + length;
    }
}
