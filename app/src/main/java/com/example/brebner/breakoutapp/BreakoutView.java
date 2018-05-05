package com.example.brebner.breakoutapp;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class BreakoutView extends SurfaceView implements Runnable {

    private static final String TAG = "BreakoutView";

    public final int BRICKS_COL = 12;
    public final int BRICKS_ROW = 3;
    public final int NLIVES = 4;
    public final int BACKGROUND = Color.argb(255, 26, 128, 182);
    public final int BALL_COLOUR = Color.argb(255, 255, 255, 255);
    public final int PADDLE_COLOUR = Color.argb(255, 255, 255, 255);
    public final int BRICK_COLOUR = Color.argb(255,  128, 0, 128);
    public final int HUD_COLOUR = Color.argb(255,  255, 200, 200);
    public final boolean NOISY = false;

    public final long LOSECOUNTTIME = 10;  // how many seconds to show the you lost message
    public final long WONCOUNTIME = 10;  // how many seconds to show the you won message

    private long loseCount = 0;
    private long wonCount = 0;

    // This is our thread
    Thread gameThread = null;

    // This is new. We need a SurfaceHolder
    // When we use Paint and Canvas in a thread
    // We will see it in action in the draw method soon.
    SurfaceHolder ourHolder;

    // A boolean which we will set and unset
    // when the game is running- or not.
    volatile boolean playing;

    // Game is paused at the start
    boolean paused = true;

    // A Canvas and a Paint object
    Canvas canvas;
    Paint paint;

    // This variable tracks the game frame rate
    long fps;


    // screen size
    int screenX;
    int screenY;

    Paddle paddle;
    Ball ball;

    // Up to 200 bricks
    Brick[] bricks = new Brick[200];
    int numBricks = 0;

    // for sounds
    // For sound FX
    SoundPool.Builder spb;
    SoundPool soundPool;
    int beep1ID = -1;
    int beep2ID = -1;
    int beep3ID = -1;
    int loseLifeID = -1;
    int explodeID = -1;

    // The score
    int score = 0;

    // Lives
    int lives = NLIVES;

    Display display;

    // When the we initialize (call new()) on gameView
    // This special constructor method runs
    public BreakoutView(Context context, Display display) {
        // The next line of code asks the
        // SurfaceView class to set up our object.
        // How kind.
        super(context);

        // Initialize ourHolder and paint objects
        ourHolder = getHolder();
        paint = new Paint();
        this.display = display;
        if (display != null) {
            Point size = new Point();
            display.getSize(size);
            screenX = size.x;
            screenY = size.y;
        }
        else {
            screenX = 100;
            screenY = 100;
        }

        paddle = new Paddle(screenX, screenY);
        ball = new Ball(screenX, screenY);

        loadSoundsNew(context);
        createBricksAndRestart();
    }

    // just to provide a default constructor
    public BreakoutView(Context context) {
        this(context, null);
    }

    private void loadSoundsNew(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            spb = new SoundPool.Builder();
            spb.setMaxStreams(4);
            AudioAttributes aa = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
            spb.setAudioAttributes(aa);
            soundPool = spb.build();
        }
        else {
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        }
        try {
            // Create objects of the 2 required classes
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;
            // Load our fx in memory ready for use
            descriptor = assetManager.openFd("beep1.ogg");
            beep1ID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("beep2.ogg");
            beep2ID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("beep3.ogg");
            beep3ID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("loseLife.ogg");
            loseLifeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("explode.ogg");
            explodeID = soundPool.load(descriptor, 0);
            Log.d(TAG, "loadSounds: OK");
        }
        catch(IOException e) {
            // Print an error message to the console
            Log.e(TAG, "loadSounds: FAILED to load", e);
        }
    }

    public void createBricksAndRestart() {
        int brickWidth = screenX / BRICKS_COL;
        int brickHeight = screenY / 12;
        // Build a wall of bricks
        numBricks = 0;
        for (int column = 0; column < BRICKS_COL; column ++ ) {
            for (int row = 0; row < BRICKS_ROW; row ++ ) {
                bricks[numBricks] = new Brick(row, column, brickWidth, brickHeight);
                numBricks ++;
            }
        }
        // now the ball
        ball.reset();
        paddle.reset();
        // reset score and lives
        score = 0;
        lives = NLIVES;
    }

    @Override
    public void run() {
        while (playing) {
            long startTime = System.currentTimeMillis();

            if (! paused) {
                update();
            }

            draw();

            long timeThisFrame = System.currentTimeMillis() - startTime;
            if (timeThisFrame > 0) {
                fps = 1000 / timeThisFrame;
            }
        }
    }

    public void update() {
        paddle.update(fps);
        // Check for ball colliding with a brick
        for (int i = 0; i < numBricks; i++) {
            if (bricks[i].getVisibility()) {
                if (RectF.intersects(bricks[i].getRect(),ball.getRect())) {
                    bricks[i].setInvisible();
                    ball.reverseYVel();
                    score = score + 10;
                    if (NOISY) {
                        soundPool.play(explodeID, 1, 1, 0, 0, 1);
                    }
                }
            }
        }
        // Check for ball colliding with paddle
        RectF pRect = paddle.getRect();
        RectF bRect = ball.getRect();
        if (RectF.intersects(pRect, bRect)) {
            float diff = pRect.right - bRect.right;
            ball.setDiff(diff);
            ball.setRandomXVel();
            ball.reverseYVel();
            ball.clearObstacleY(paddle.getRect().top - 2);
            if (NOISY) {
                soundPool.play(beep1ID, 1, 1, 0, 0, 1);
            }
        }
        // Bounce the ball back when it hits the bottom of screen
        // And deduct a life
        if (ball.getRect().bottom > screenY) {
            ball.reverseYVel();
            ball.clearObstacleY(screenY - 2);
            // Lose a life
            lives --;
            if (NOISY) {
                soundPool.play(loseLifeID, 1, 1, 0, 0, 1);
            }
            if(lives == 0){
                paused = true;
                loseCount = LOSECOUNTTIME *  fps;
                createBricksAndRestart();
            }
        }
        // Bounce the ball back when it hits the top of screen
        if (ball.getRect().top < 0) {
            ball.reverseYVel();
            ball.clearObstacleY(12);
            if (NOISY) {
                soundPool.play(beep2ID, 1, 1, 0, 0, 1);
            }
        }
        // If the ball hits left wall bounce
        if (ball.getRect().left < 0) {
            ball.reverseXVel();
            ball.clearObstacleX(2);
            if (NOISY) {
                soundPool.play(beep3ID, 1, 1, 0, 0, 1);
            }
        }
        // If the ball hits right wall bounce
        if (ball.getRect().right > screenX - 10) {
            ball.reverseXVel();
            ball.clearObstacleX(screenX - 22);
            if (NOISY) {
                soundPool.play(beep3ID, 1, 1, 0, 0, 1);
            }
        }
        // Pause if cleared screen
        if(score == numBricks * 10){
            paused = true;
            wonCount = WONCOUNTIME *  fps;
            createBricksAndRestart();
        }
        ball.update(fps);
    }

    public void draw() {
        if (ourHolder.getSurface().isValid()) {
            canvas = ourHolder.lockCanvas();
            // background
            canvas.drawColor(BACKGROUND);

            if ((loseCount > 0) || (wonCount > 0)) {
                if (loseCount > 0) {
                    paint.setColor(HUD_COLOUR);
                    paint.setTextSize(60);
                    canvas.drawText("YOU LOST", this.screenX / 2, this.screenY / 2, paint);
                    loseCount--;
                }
                if (wonCount > 0) {
                    paint.setColor(HUD_COLOUR);
                    paint.setTextSize(60);
                    canvas.drawText("YOU WON", this.screenX / 2, this.screenY / 2, paint);
                    wonCount--;
                }
            }
            else {
                paint.setColor(PADDLE_COLOUR);
                canvas.drawRect(paddle.getRect(), paint);
                paint.setColor(BALL_COLOUR);
                canvas.drawRect(ball.getRect(), paint);
                // draw bricks
                paint.setColor(BRICK_COLOUR);
                for (int i = 0; i < numBricks; i++) {
                    if (bricks[i].getVisibility()) {
                        canvas.drawRect(bricks[i].getRect(), paint);
                    }
                }
                // Draw the HUD
                // Choose the brush color for drawing
                paint.setColor(HUD_COLOUR);
                // Draw the score
                paint.setTextSize(40);
                canvas.drawText("Score: " + score + "   Lives: " + lives, 10, 50, paint);
                // Has the player cleared the screen?
                if (score == numBricks * 10) {
                    paint.setTextSize(90);
                    canvas.drawText("YOU HAVE WON!", 10, screenY / 2, paint);
                }
                // Has the player lost?
                if (lives <= 0) {
                    paint.setTextSize(90);
                    canvas.drawText("YOU HAVE LOST!", 10, screenY / 2, paint);
                }
            }
            // and done ...
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void pause() {
        playing = false;
        try {
            gameThread.join();
        }
        catch (InterruptedException e) {
            Log.e(TAG, "pause: error joining thread", e);
        }
    }

    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                paused = false;
                if (motionEvent.getX() > screenX / 2) {
                    paddle.setPaddleMoving(paddle.RIGHT);
                }
                else {
                    paddle.setPaddleMoving(paddle.LEFT);
                }
                break;

            case MotionEvent.ACTION_UP:
                paddle.setPaddleMoving(paddle.STOPPED);
                break;
                
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onTouchEvent: ACTION_MOVE");
                break;
                
            case MotionEvent.ACTION_BUTTON_PRESS:
            case MotionEvent.ACTION_BUTTON_RELEASE:
                this.performClick();
                break;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        Log.d(TAG, "performClick!");
        return super.performClick();
    }
}
