package szakdolgozat.istvan.ping_pong;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Pisti on 2017. 03. 22..
 */

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder holder;
    private int width, height;
    private GameLoop gameLoop;
    private GameEngine gameEngine;
    private Boolean multiplayer;
    private Player[] players;
    private GameActivity gameActivity;
    private TextView playerScore, playerScore2, winner;
    private String player1, player2;
    Sound sound;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        holder.addCallback(this);
        this.width = Resources.getSystem().getDisplayMetrics().widthPixels;
        this.height = Resources.getSystem().getDisplayMetrics().heightPixels;
        players = new Player[2];
        setFocusable(true);
        gameActivity = (GameActivity) getContext();
        this.sound = new Sound(getContext());
    }

    public void startSinglePlayer(int difficulty, int goal, String player1, String player2) {
        Difficulty diff;
        switch (difficulty) {
            case 1:
                diff = Difficulty.EASY;
                break;
            case 2:
                diff = Difficulty.MEDIUM;
                break;
            case 3:
                diff = Difficulty.HARD;
                break;
            default:
                diff = Difficulty.MEDIUM;
                break;
        }
        this.player1 = player1;
        this.player2 = player2;
        gameEngine = new GameEngine(width, height, diff, goal, sound);
        this.multiplayer = false;
        gameLoop = new GameLoop(this, gameEngine);
    }

    public void startMultiPlayer(int goal, String player1, String player2) {
        gameEngine = new GameEngine(width, height, goal, sound);
        this.multiplayer = true;
        this.player1 = player1;
        this.player2 = player2;
        gameLoop = new GameLoop(this, gameEngine);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        View root = getRootView();
        playerScore = (TextView) root.findViewById(R.id.playerScore);
        playerScore.setTextColor(gameEngine.getGameState().getPlayer1().getColor());
        playerScore2 = (TextView) root.findViewById(R.id.playerScore2);
        playerScore2.setTextColor(gameEngine.getGameState().getPlayer2().getColor());
        winner = (TextView) root.findViewById(R.id.TV_WINNER);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        setScore();
        drawMap(canvas);
        drawBall(canvas);
        drawPlayers(canvas);
    }

    private void drawBall(Canvas canvas) {
        Paint paint = new Paint();
        Ball ball = gameEngine.getGameState().getBall();
        paint.setColor(ball.getColor());
        canvas.drawCircle((float) ball.getX(), (float) ball.getY(), (int) ball.getDiameter(), paint);
    }

    private void drawPlayers(Canvas canvas) {
        Player player = gameEngine.getGameState().getPlayer1();
        Paint paint = new Paint();
        paint.setColor(player.getColor());
        canvas.drawRect((float) (player.getX() - player.getWidth() / 2), (float) (player.getY() - player.getHeight() / 2), (float) (player.getX() + player.getWidth() / 2), (float) (player.getY() + player.getHeight() / 2), paint);
        player = gameEngine.getGameState().getPlayer2();
        paint.setColor(player.getColor());
        canvas.drawRect((float) (player.getX() - player.getWidth() / 2), (float) (player.getY() - player.getHeight() / 2), (float) (player.getX() + player.getWidth() / 2), (float) (player.getY() + player.getHeight() / 2), paint);

    }

    private void drawMap(Canvas canvas) {
        Paint paint = new Paint();
        canvas.drawColor(Color.parseColor("#3a7a2d"));
        paint.setAlpha(50);
        paint.setColor(Color.WHITE);
        canvas.drawLine(0, height / 2, width, height / 2, paint);
        paint.setColor(Color.BLACK);
        canvas.drawLine(0, height - (height * 1 / 4), width, height - (height * 1 / 4), paint);
        canvas.drawLine(0, height - (height * 1 / 12), width, height - (height * 1 / 12), paint);
        canvas.drawLine(0, height * 1 / 4, width, height * 1 / 4, paint);
        canvas.drawLine(0, height * 1 / 12, width, height * 1 / 12, paint);
    }

    private void setScore() {
        gameActivity.runOnUiThread(new Runnable() {
            public void run() {
                playerScore.setText(String.valueOf(gameEngine.getGameState().getPlayer1().getScore()));
                playerScore2.setText(String.valueOf(gameEngine.getGameState().getPlayer2().getScore()));
            }

        });
    }

    private void announceWinner(final String name, final int color) {
        gameActivity.runOnUiThread(new Runnable() {
            public void run() {
                winner.setTextColor(color);
                winner.setText(name + " won!");
            }
        });
    }

    private void clearWinner() {
        gameActivity.runOnUiThread(new Runnable() {
            public void run() {
                winner.setText("");
            }
        });
    }

    public void endGame() {
        SQLiteHelper helper = new SQLiteHelper(getContext());
        int score1 = gameEngine.getGameState().getPlayer1().getScore();
        int score2 = gameEngine.getGameState().getPlayer2().getScore();
        if (score1 > score2) {
            announceWinner(player1, gameEngine.getGameState().getPlayer1().getColor());
            helper.insertMatch(player1, player2, score1, score2);
        } else {
            announceWinner(player2, gameEngine.getGameState().getPlayer2().getColor());
            helper.insertMatch(player1, player2, score1, score2);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.width = w;
        this.height = h;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        gameLoop.setRunning(false);
        while (retry) {
            try {
                gameLoop.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        gameLoop.setRunning(true);
        gameLoop.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int pointerIndex = event.getActionIndex();

        // not specific to a pointer action
        int maskedAction = event.getActionMasked();

        switch (maskedAction) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                if (event.getY(pointerIndex) > height / 2 && pointerIndex < 2 && gameEngine.pointInPlayer(gameEngine.getGameState().getPlayer1(), event.getX(pointerIndex), event.getY(pointerIndex))) {
                    players[pointerIndex] = gameEngine.getGameState().getPlayer1();
                    players[pointerIndex].setMoving(true);
                    gameEngine.movePlayer1(event.getX(pointerIndex), event.getY(pointerIndex), players[pointerIndex]);
                } else if (multiplayer && pointerIndex < 2 && gameEngine.pointInPlayer(gameEngine.getGameState().getPlayer2(), event.getX(pointerIndex), event.getY(pointerIndex))) {
                    players[pointerIndex] = gameEngine.getGameState().getPlayer2();
                    players[pointerIndex].setMoving(true);
                    gameEngine.movePlayer2(event.getX(pointerIndex), event.getY(pointerIndex), players[pointerIndex]);
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: { // a pointer was moved
                for (int i = 0; i < event.getPointerCount(); i++) {
                    if (event.getY(pointerIndex) > height / 2 && pointerIndex < 2 && gameEngine.pointInPlayer(gameEngine.getGameState().getPlayer1(), event.getX(pointerIndex), event.getY(pointerIndex))) {
                        players[pointerIndex] = gameEngine.getGameState().getPlayer1();
                        players[pointerIndex].setMoving(true);
                        gameEngine.movePlayer1(event.getX(pointerIndex), event.getY(pointerIndex), players[pointerIndex]);
                    } else if (multiplayer && pointerIndex < 2 && gameEngine.pointInPlayer(gameEngine.getGameState().getPlayer2(), event.getX(pointerIndex), event.getY(pointerIndex))) {
                        players[pointerIndex] = gameEngine.getGameState().getPlayer2();
                        players[pointerIndex].setMoving(true);
                        gameEngine.movePlayer2(event.getX(pointerIndex), event.getY(pointerIndex), players[pointerIndex]);
                    }
                    if (event.getY(i) > height / 2 && gameEngine.getGameState().getPlayer1().isMoving()) {
                        gameEngine.movePlayer1(event.getX(i), event.getY(i), gameEngine.getGameState().getPlayer1());
                    } else if (multiplayer && gameEngine.getGameState().getPlayer2().isMoving()) {
                        gameEngine.movePlayer2(event.getX(i), event.getY(i), gameEngine.getGameState().getPlayer2());
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (pointerIndex < 2 && players[pointerIndex] != null)
                    players[pointerIndex].setMoving(false);
                break;
            }
        }
        invalidate();

        return true;
    }

    public void restart() {
        clearWinner();
        gameEngine.restart();
        if (gameLoop.isRunning()) {
            continueGame();
        } else {
            gameLoop = new GameLoop(this, gameEngine);
            gameLoop.setRunning(true);
            gameLoop.start();
        }


    }

    public void pauseGame() {
        gameLoop.pause();
    }

    public void continueGame() {
        gameLoop.unPause();
    }

    public void stopGame() {
        gameLoop.stopLoop();
    }

    public boolean isGameEnded() {
        return gameEngine.isGameEnded();
    }

    public void muteGame() {
        sound.mute();
    }

    public void unMuteGame() {
        sound.unMute();
    }
}

