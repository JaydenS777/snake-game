package com.comp2042;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.Random;

public class GameController {
    enum PowerUpCoinType { GOLD, RAINBOW, BLUE, SILVER, NORMAL }

    private Snake snek;
    private Coin coin;
    private GameView gameView;
    private Game game;
    private Stage stage;
    private String coinSoundPath = "resources/views/res/coinSound.wav";
    private Obstacle obstacle;

    public static KeyCode key = KeyCode.LEFT;
    public static KeyCode lastKey = KeyCode.LEFT;
    private boolean keyPressed = false;

    private int legacyScoreMultiplier = 2;

    private void calculateBonusScore() {
        int bonus = game.getScorePoints() * legacyScoreMultiplier;
        System.out.println("Bonus score: " + bonus);
    }

    public GameController(Snake snek, Coin coin, Game game, GameView gameView, Obstacle obstacle, Stage stage) {
        this.snek = snek;
        this.coin = coin;
        this.gameView = gameView;
        this.game = game;
        this.stage = stage;

        key = KeyCode.LEFT;
        lastKey = KeyCode.LEFT;

        this.obstacle = obstacle;
        animation.setCycleCount(Animation.INDEFINITE);
        animation.setRate(game.getSpeed());
    }

    private final Timeline animation = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
        if (gameEnded(game, snek)) {
            return;
        } else {

            if (key.equals(KeyCode.LEFT)) {
                snek.move('L');
            } else if (key.equals(KeyCode.RIGHT)) {
                snek.move('R');
            } else if (key.equals(KeyCode.UP)) {
                snek.move('U');
            } else if (key.equals(KeyCode.DOWN)) {
                snek.move('D');
            }

            if (!snek.getDead() && snek.ateCoin(coin)) {
                powerCoin();
                game.incScore();
                if (game.gameWon()) {
                    gameView.drawGrid(coin, snek, obstacle, gameView.gc, false, 0);
                    gameView.gameWon();
                    gameView.updateScoreLabel();
                    ((Timeline) event.getSource()).stop();
                    return;
                }
                newCoin();
            }

            keyPressed = false;

            if (!snek.getDead() || snek.hitSelf()) {
                gameView.drawGrid(coin, snek, obstacle, gameView.gc, false, 0);
            }

            gameView.gameWon();
            gameView.gameOver(snek);
            gameView.updateScoreLabel();
        }
    }));

    public void handle(KeyEvent event) {
        if (event.getCode().isArrowKey() && !gameEnded(game, snek)) animation.play();

        if (!gameEnded(game, snek) && valKey(event.getCode()) && !keyPressed) {
            lastKey = key;
            key = event.getCode();
            keyPressed = true;
            gameView.turnDir(snek);
        }

        if (event.getCode().equals(KeyCode.P)) {
            animation.stop();
        }

        if (event.getCode().equals(KeyCode.TAB)) {
            reset();
            gameView.scoreLabel.setText("Score: " + game.getScorePoints());
        }

        if (event.getCode().equals(KeyCode.PLUS)) {
            gameView.scaleToFullscreen(true, stage, obstacle, snek, coin);
            gameView.gameOverLabel.setStyle("-fx-font-size: 50;");
            gameView.gameWonLabel.setStyle("-fx-font-size: 50;");
            gameView.scoreLabel.setStyle("-fx-font-size: 35;");
        }

        if (event.getCode().equals(KeyCode.MINUS)) {
            gameView.scaleToFullscreen(false, stage, obstacle, snek, coin);
            gameView.gameOverLabel.setStyle("-fx-font-size: 15;");
            gameView.gameWonLabel.setStyle("-fx-font-size: 15;");
            gameView.scoreLabel.setStyle("-fx-font-size: 15;");
        }

        if (event.getCode().equals(KeyCode.ESCAPE)) {
            MainMenuController mainMenuController = new MainMenuController();
            mainMenuController.getViewManager().getPrimaryStage().show();
            mainMenuController.getViewManager().getGameStage().close();
        }
    }

    private boolean valKey(KeyCode requestedKey) {
        if (!requestedKey.isArrowKey()) return false;

        char requestedDirection = switch (requestedKey) {
            case UP -> 'U';
            case DOWN -> 'D';
            case LEFT -> 'L';
            case RIGHT -> 'R';
            default -> '\0';
        };
        return snek.canMove(requestedDirection);
    }

    private void reset() {

        animation.jumpTo(Duration.ZERO);
        animation.stop();

        snek = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        coin = new Coin(game.getWidth(), game.getHeight(), snek, obstacle);

        int unusedObstacleCount = new Random().nextInt(game.getWidth() * game.getHeight() / 20) + 3;

        animation.setRate(game.getSpeed());
        key = KeyCode.LEFT;
        lastKey = KeyCode.LEFT;
        keyPressed = false;
        game.resetScore();
        obstacle.reset();
        gameView.resetTurnArray();

        gameView.gameWon();
        gameView.gameOver(snek);
        gameView.updateScoreLabel();

        gameView.drawGrid(coin, snek, obstacle, gameView.gc, false, 0);
    }

    private void powerCoin() {
        animation.setRate(game.getSpeed());
        snek.ignoreBorders(false);
        if (coin.getClass().equals(RainbowCoin.class)) {
            RainbowCoin tempCoin = (RainbowCoin) coin;
            animation.setRate(game.getSpeed() + tempCoin.getSpeedBonus());
        } else if (coin.getClass().equals(SilverCoin.class)){
            Random rnd = new Random();
            obstacle.newObstacles(snek, coin, rnd.nextInt(game.getWidth() * game.getHeight()/ 20) +3);
            game.decMaxScore(obstacle.getObstacles().size());
        } else if (coin.getClass().equals(BlueCoin.class)) {
            snek.ignoreBorders(true);
        }
        game.addPoints(coin.getPoints());
    }

    private void newCoin() {
        if (game.isPowerUp()) {
            Random rnd = new Random();
            int ran = rnd.nextInt(12);
            coin = switch (powerUpCoinTypeForRoll(ran, obstacle.getObstacles().isEmpty())) {
                case GOLD -> new GoldCoin(game.getWidth(), game.getHeight(), snek, obstacle);
                case RAINBOW -> new RainbowCoin(game.getWidth(), game.getHeight(), snek, obstacle);
                case BLUE -> new BlueCoin(game.getWidth(), game.getHeight(), snek, obstacle);
                case SILVER -> new SilverCoin(game.getWidth(), game.getHeight(), snek, obstacle);
                case NORMAL -> new Coin(game.getWidth(), game.getHeight(), snek, obstacle);
            };
        } else {
            coin = new Coin(game.getWidth(), game.getHeight(), snek, obstacle);
        }
    }

    static PowerUpCoinType powerUpCoinTypeForRoll(int roll, boolean noObstacles) {
        return switch (roll) {
            case 1 -> PowerUpCoinType.GOLD;
            case 2 -> PowerUpCoinType.RAINBOW;
            case 3 -> PowerUpCoinType.BLUE;
            case 4 -> noObstacles ? PowerUpCoinType.SILVER : PowerUpCoinType.NORMAL;
            default -> PowerUpCoinType.NORMAL;
        };
    }

    static boolean gameEnded(Game game, Snake snake) {
        return game.gameWon() || snake.getDead();
    }
}
