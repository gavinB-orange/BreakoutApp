package com.example.brebner.breakoutapp;

import android.graphics.RectF;

import java.util.Random;

public class Ball {

    RectF rect;
    float xVel;
    float yVel;
    float ballWidth = 10;
    float ballHeight = 10;

    int screenX;
    int screenY;

    public Ball(int screenX, int screenY) {
        xVel = 200;
        yVel = -400;

        rect = new RectF();
        this.screenX = screenX;
        this.screenY = screenY;
        ballHeight = screenY / 100;
        if (ballHeight < 10) {
            ballHeight = 10;
        }
        ballWidth = ballHeight;  // square
    }

    public void setDiff(float diff) {
        // depending on pos, change angle
        if (xVel < 100) {
            xVel = 100;
            return;
        }
        if (xVel > 300) {
            xVel = 300;
            return;
        }
        xVel = xVel + diff;
    }

    public RectF getRect() {
        return rect;
    }

    public void update(long fps) {
        rect.left = rect.left + (xVel / fps);
        rect.top = rect.top + (yVel / fps);
        rect.right = rect.left + ballWidth;
        rect.bottom = rect.top - ballHeight;
    }

    public void reverseYVel() {
        yVel = -yVel;
    }

    public void reverseXVel() {
        xVel = -xVel;
    }

    public void setRandomXVel() {
        Random random = new Random();
        int answer = random.nextInt(2);
        if (answer == 0) {
            reverseXVel();
        }
    }

    public void clearObstacleY(float y) {
        rect.bottom = y;
        rect.top = y - ballHeight;
    }

    public void clearObstacleX(float x) {
        rect.left = x;
        rect.right = x + ballWidth;
    }

    public void reset() {
        rect.left = screenX / 2;
        rect.top = screenY - 20;
        rect.right = screenX / 2 + ballWidth;
        rect.bottom = screenY - 20 - ballHeight;
    }

}
