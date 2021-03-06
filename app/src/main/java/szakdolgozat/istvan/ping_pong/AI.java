package szakdolgozat.istvan.ping_pong;

import java.util.Random;

/**
 * Created by Pisti on 2017. 09. 19..
 */

public class AI {
    private double easy_speed;
    private double medium_speed;
    private double hard_speed;
    private Difficulty difficulty;
    private double screenWidth, maxY, hitY;
    private boolean willHit, ballInArea;
    private Direction hitDirection;

    public AI(Difficulty difficulty, double screenWidth, double maxY) {
        this.difficulty = difficulty;
        this.screenWidth = screenWidth;
        this.easy_speed = screenWidth / 110;
        this.medium_speed = screenWidth / 70;
        this.hard_speed = screenWidth / 45;
        this.maxY = maxY;
        this.ballInArea = false;
        setHitY();
        setWillHit();

    }

    private void setHitY() {
        switch (difficulty) {
            case EASY:
                this.hitY = maxY - (easy_speed / 2);
                break;
            case MEDIUM:
                this.hitY = maxY - (medium_speed / 2);
                break;
            case HARD:
                this.hitY = maxY - (hard_speed / 2);
                break;
        }
    }

    public Point getDestination(GameState gamesState) {
        Ball ball = gamesState.getBall();
        Player opponent = gamesState.getPlayer1();
        Player ai = gamesState.getPlayer2();

        if (ballInArea && ball.getY() > maxY) {
            ballInArea = false;
            setWillHit();
            if (difficulty == Difficulty.HARD)
                generateHitDirection();
        } else if (!ballInArea && ball.getY() < maxY) {
            ballInArea = true;
            if (difficulty == Difficulty.HARD)
                generateHitDirection();
        }

        switch (difficulty) {
            case EASY:
                return getEasyDestination(ai, ball);
            case MEDIUM:
                return getMediumDestination(ai, ball);
            case HARD:
                return getHardDestination(ai, ball);
            default:
                return getMediumDestination(ai, ball);
        }
    }

    private Point getEasyDestination(Player ai, Ball ball) {
        Point point = new Point();
        if (ball.getX() > ai.getX() + easy_speed)
            point.setX(ai.getX() + easy_speed);
        else if (ball.getX() < ai.getX() - easy_speed)
            point.setX(ai.getX() - easy_speed);
        else
            point.setX(ball.getX());
        return point;
    }

    private Point getMediumDestination(Player ai, Ball ball) {
        Point point = new Point();
        if (ball.getX() > (ai.getX() + medium_speed))
            point.setX(ai.getX() + medium_speed);
        else if (ball.getX() < (ai.getX() - medium_speed))
            point.setX(ai.getX() - medium_speed);
        else
            point.setX(ball.getX());

        if ((ball.getY() > ((hitY + ai.getHeight() / 2) + medium_speed)) || (ball.getY() < ai.getY()))
            point.setY(ai.getY() - medium_speed / 2);
        else if (isBallInRange(ball, ai, medium_speed) && ball.getY() < hitY)
            if (willHit) {
                if (ball.getY() - ball.getDiameter() / 2 - 0.5 > medium_speed)
                    point.setY(ai.getY() + medium_speed);
                else
                    point.setY(ball.getY() - ball.getDiameter() / 2 - 0.5);

            }

        return point;
    }

    private boolean isBallInRange(Ball ball, Player ai, double speed) {
        if ((ball.getX() < ai.getX() + speed) && (ball.getX() > ai.getX() - speed))
            if ((ball.getY() - ball.getDiameter() / 2 < ai.getY() + ai.getHeight() / 2 + speed) && (ball.getY() - ball.getDiameter() / 2 > ai.getY() + ai.getHeight() / 2 - speed))
                return true;
        return false;
    }

    private Point getHardDestination(Player ai, Ball ball) {
        Point point = new Point();
        if (ball.getX() > (ai.getX() + hard_speed))
            point.setX(ai.getX() + hard_speed);
        else if (ball.getX() < (ai.getX() - hard_speed))
            point.setX(ai.getX() - hard_speed);
        else
            point.setX(ball.getX());

        if ((ball.getY() > ((hitY + ai.getHeight() / 2) + hard_speed)) || (ball.getY() < ai.getY()))
            point.setY(ai.getY() - hard_speed / 2);
        else if (isBallInRange(ball, ai, hard_speed))
            if (willHit) {
                if (ball.getY() - ball.getDiameter() / 2 - 0.5 > hard_speed)
                    point.setY(ai.getY() + hard_speed);
                else
                    point.setY(ball.getY() - ball.getDiameter() / 2 - 0.5);
            }

        return point;
    }

    public void setWillHit() {
        Random random = new Random();
        willHit = random.nextBoolean();
    }

    private void generateHitDirection() {
        Random random = new Random();
        int i = random.nextInt(3);
        switch (i) {
            case 0:
                hitDirection = Direction.LEFT;
                break;
            case 1:
                hitDirection = Direction.CENTER;
                break;
            case 2:
                hitDirection = Direction.RIGHT;
                break;
            default:
                hitDirection = Direction.CENTER;
        }
    }

    private Point calcBallNextPosition(Ball a) {
        Ball ball = new Ball(a.getDiameter(), a.getX(), a.getY());
        ball.nextPosition();
        if (ball.getX() >= screenWidth) {
            ball.setX(screenWidth - (ball.getDiameter() / 2) - 0.2);
            ball.reverseX();
        }

        if (ball.getX() <= 0) {
            ball.setX((ball.getDiameter() / 2) + 0.2);
            ball.reverseX();
        }
        Point target = new Point(ball.getX(), ball.getY());
        return target;
    }

    public boolean isWillHit() {
        return willHit;
    }

    public double getSpeed() {
        switch (difficulty) {
            case EASY:
                return easy_speed;
            case MEDIUM:
                return medium_speed;
            case HARD:
                return hard_speed;
        }
        return medium_speed;
    }
}
