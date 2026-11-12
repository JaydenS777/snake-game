package com.comp2042;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.Point;
import java.util.List;
import java.util.Random;

public class GameController {
    private Snake snek;
    private List<Coin> coins;
    private GameView gameView;
    private Game game;
    private Stage stage;
    private Obstacle obstacle;

    private KeyCode key = KeyCode.LEFT;
    private boolean keyPressed = false;
    private boolean pauseKeyDown;
    private final Random random = new Random();
    private final PauseState pauseState = new PauseState();
    private final ResumeCountdown resumeCountdown = new ResumeCountdown();
    private final ShopCountdown shopCountdown = new ShopCountdown();
    private final ShopRefreshRules shopRefreshRules = new ShopRefreshRules();
    private final Timeline resumeCountdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> advanceResumeCountdown()));
    private final Timeline shopCountdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> tickShopCountdown()));
    private Timeline coinSpawnTimeline;
    private boolean coinSpawnerStarted;
    private boolean shopOpen;
    private boolean resumeCountdownRunning;
    private boolean resumeAnimationAfterCountdown;
    private boolean resumeFromShopCountdown;

    private void advanceResumeCountdown() {
        String number = resumeCountdown.advance();
        gameView.showResumeCountdown(number);
        if (!resumeCountdown.isActive()) {
            resumeCountdownRunning = false;
            if (resumeFromShopCountdown && !gameEnded(game, snek)) {
                resumeFromShopCountdown = false;
                resumeAnimationAfterCountdown = false;
                shopOpen = false;
                shopCountdown.reset();
                gameView.updateTimeLabel(shopCountdown.getRemainingSeconds());
                gameView.startTimeProgress();
                animation.play();
                shopCountdownTimeline.playFromStart();
                resumeCoinSpawner();
            } else if (resumeAnimationAfterCountdown && !gameEnded(game, snek)) {
                animation.play();
                if (!shopCountdown.isFinished()) {
                    shopCountdownTimeline.play();
                    gameView.resumeTimeProgress();
                }
                resumeCoinSpawner();
            }
        }
    }

    private void tickShopCountdown() {
        if (gameEnded(game, snek)) {
            shopCountdownTimeline.stop();
            gameView.stopTimeProgress();
            return;
        }

        int remainingSeconds = shopCountdown.tick();
        gameView.updateTimeLabel(remainingSeconds);
        if (shopCountdown.isFinished()) {
            shopCountdownTimeline.stop();
            animation.pause();
            pauseCoinSpawner();
            shopOpen = true;
            shopRefreshRules.reset();
            gameView.showShop(game.getScorePoints());
        }
    }

    private void startCoinSpawner() {
        if (coinSpawnerStarted || gameEnded(game, snek)) {
            return;
        }
        coinSpawnerStarted = true;
        scheduleNextCoinSpawn();
    }

    private void scheduleNextCoinSpawn() {
        if (!coinSpawnerStarted || gameEnded(game, snek)) {
            return;
        }

        double delaySeconds = CoinSpawnRules.intervalForRoll(random.nextDouble());
        coinSpawnTimeline = new Timeline(new KeyFrame(Duration.seconds(delaySeconds), event -> {
            if (!coinSpawnerStarted || gameEnded(game, snek)) {
                stopCoinSpawner();
                return;
            }

            if (CoinSpawnRules.hasRoom(coins.size())) {
                Coin spawnedCoin = createRandomCoin();
                if (spawnedCoin != null) {
                    coins.add(spawnedCoin);
                    gameView.drawGrid(coins, snek, obstacle, gameView.gc, false, 0);
                }
            }
            scheduleNextCoinSpawn();
        }));
        coinSpawnTimeline.playFromStart();
    }

    private Coin createRandomCoin() {
        Point position = Coin.randomFreePosition(game.getWidth(), game.getHeight(), snek,
                obstacle, coins, random);
        if (position == null) {
            return null;
        }

        return CoinSpawnRules.typeForRoll(random.nextDouble()) == CoinSpawnRules.Type.GOLD
                ? new GoldCoin(position)
                : new Coin(position);
    }

    private void pauseCoinSpawner() {
        if (coinSpawnTimeline != null && coinSpawnTimeline.getStatus() == Animation.Status.RUNNING) {
            coinSpawnTimeline.pause();
        }
    }

    private void resumeCoinSpawner() {
        if (coinSpawnerStarted && coinSpawnTimeline != null) {
            coinSpawnTimeline.play();
        }
    }

    private void stopCoinSpawner() {
        coinSpawnerStarted = false;
        if (coinSpawnTimeline != null) {
            coinSpawnTimeline.stop();
            coinSpawnTimeline = null;
        }
    }

    public GameController(Snake snek, List<Coin> coins, Game game, GameView gameView, Obstacle obstacle, Stage stage) {
        this.snek = snek;
        this.coins = coins;
        this.gameView = gameView;
        this.game = game;
        this.stage = stage;

        key = KeyCode.LEFT;

        this.obstacle = obstacle;
        animation.setCycleCount(Animation.INDEFINITE);
        animation.setRate(game.getSpeed());
        shopCountdownTimeline.setCycleCount(Animation.INDEFINITE);
    }

    private final Timeline animation = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
        if (gameEnded(game, snek)) {
            gameView.stopTimeProgress();
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

            if (!snek.getDead()) {
                Coin eatenCoin = snek.ateAnyCoin(coins);
                if (eatenCoin != null) {
                    coins.remove(eatenCoin);
                    game.addPoints(eatenCoin.getPoints());
                    game.incScore();
                    if (game.gameWon()) {
                        gameView.drawGrid(coins, snek, obstacle, gameView.gc, false, 0);
                        gameView.gameWon();
                        gameView.updateScoreLabel();
                        ((Timeline) event.getSource()).stop();
                        shopCountdownTimeline.stop();
                        gameView.stopTimeProgress();
                        stopCoinSpawner();
                        return;
                    }
                }
            }

            keyPressed = false;

            if (!snek.getDead() || snek.hitSelf()) {
                gameView.drawGrid(coins, snek, obstacle, gameView.gc, false, 0);
            }

            gameView.gameWon();
            gameView.gameOver(snek);
            gameView.updateScoreLabel();
            if (gameEnded(game, snek)) {
                shopCountdownTimeline.stop();
                gameView.stopTimeProgress();
                stopCoinSpawner();
            }
        }
    }));

    public void handle(KeyEvent event) {
        if (shopOpen) {
            return;
        }

        if (event.getCode().equals(KeyCode.ESCAPE) || event.getCode().equals(KeyCode.P)) {
            if (!pauseKeyDown) {
                pauseKeyDown = true;
                togglePause();
            }
            return;
        }

        if (pauseState.isPaused()) {
            return;
        }

        if (resumeCountdownRunning) {
            return;
        }

        if (event.getCode().isArrowKey() && !gameEnded(game, snek)) {
            animation.play();
            if (!shopCountdown.isFinished()) {
                if (shopCountdownTimeline.getStatus() == Animation.Status.STOPPED) {
                    gameView.startTimeProgress();
                }
                shopCountdownTimeline.play();
            }
            startCoinSpawner();
        }

        if (!gameEnded(game, snek) && valKey(event.getCode()) && !keyPressed) {
            key = event.getCode();
            keyPressed = true;
        }

        if (event.getCode().equals(KeyCode.TAB)) {
            reset();
            gameView.updateScoreLabel();
        }

        if (event.getCode().equals(KeyCode.PLUS)) {
            Main.getViewManager().applyDisplayMode(true);
            gameView.scaleToFullscreen(true, stage, obstacle, snek, coins);
        }

        if (event.getCode().equals(KeyCode.MINUS)) {
            Main.getViewManager().applyDisplayMode(false);
            gameView.scaleToFullscreen(false, stage, obstacle, snek, coins);
        }

    }

    public void handleKeyReleased(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ESCAPE) || event.getCode().equals(KeyCode.P)) {
            pauseKeyDown = false;
        }
    }

    private void togglePause() {
        if (gameEnded(game, snek)) {
            return;
        }

        if (resumeCountdownRunning) {
            resumeCountdownTimeline.stop();
            resumeCountdownRunning = false;
            pauseState.pause(resumeAnimationAfterCountdown);
            gameView.showResumeCountdown("");
            gameView.setPaused(true);
            return;
        }

        if (pauseState.isPaused()) {
            resumeGame();
            return;
        }

        boolean animationWasRunning = animation.getStatus() == Animation.Status.RUNNING;
        pauseState.pause(animationWasRunning);
        if (animationWasRunning) {
            animation.pause();
            shopCountdownTimeline.pause();
            gameView.pauseTimeProgress();
            pauseCoinSpawner();
        }
        gameView.setPaused(true);
    }

    public void resumeGame() {
        if (!pauseState.isPaused()) {
            return;
        }

        boolean shouldResumeAnimation = pauseState.resume();
        gameView.setPaused(false);
        resumeAnimationAfterCountdown = shouldResumeAnimation;
        resumeCountdown.start();
        resumeCountdownRunning = true;
        gameView.showResumeCountdown(resumeCountdown.currentNumber());
        resumeCountdownTimeline.setCycleCount(3);
        resumeCountdownTimeline.playFromStart();
    }

    public void continueFromShop() {
        if (!shopOpen || gameEnded(game, snek) || resumeCountdownRunning) {
            return;
        }

        gameView.setShopVisible(false);
        resumeFromShopCountdown = true;
        resumeAnimationAfterCountdown = true;
        resumeCountdown.start();
        resumeCountdownRunning = true;
        gameView.showResumeCountdown(resumeCountdown.currentNumber());
        resumeCountdownTimeline.setCycleCount(3);
        resumeCountdownTimeline.playFromStart();
    }

    public void refreshShop() {
        if (!shopOpen || !shopRefreshRules.canRefresh(game.getScorePoints())) {
            gameView.updateShop(game.getScorePoints(), shopRefreshRules.refreshesUsed());
            return;
        }
        if (game.spendPoints(ShopRefreshRules.COST)) {
            shopRefreshRules.recordRefresh(game.getScorePoints() + ShopRefreshRules.COST);
            gameView.updateScoreLabel();
            gameView.updateShop(game.getScorePoints(), shopRefreshRules.refreshesUsed());
        }
    }

    public void exitToMenu() {
        resumeCountdownTimeline.stop();
        resumeCountdownRunning = false;
        resumeFromShopCountdown = false;
        gameView.showResumeCountdown("");
        animation.stop();
        shopCountdownTimeline.stop();
        gameView.stopTimeProgress();
        stopCoinSpawner();
        shopOpen = false;
        gameView.setShopVisible(false);
        pauseState.reset();
        gameView.setPaused(false);

        Main.getViewManager().showMainMenu();
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

        resumeCountdownTimeline.stop();
        resumeCountdownRunning = false;
        resumeAnimationAfterCountdown = false;
        resumeFromShopCountdown = false;
        gameView.showResumeCountdown("");
        shopOpen = false;
        gameView.setShopVisible(false);
        stopCoinSpawner();
        shopCountdownTimeline.stop();
        gameView.stopTimeProgress();
        shopCountdown.reset();
        gameView.updateTimeLabel(shopCountdown.getRemainingSeconds());
        animation.jumpTo(Duration.ZERO);
        animation.stop();
        pauseState.reset();
        gameView.setPaused(false);

        snek = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        obstacle.reset();
        coins.clear();
        coins.add(new Coin(game.getWidth(), game.getHeight(), snek, obstacle));

        animation.setRate(game.getSpeed());
        key = KeyCode.LEFT;
        keyPressed = false;
        game.resetScore();

        gameView.gameWon();
        gameView.gameOver(snek);
        gameView.updateScoreLabel();

        gameView.drawGrid(coins, snek, obstacle, gameView.gc, false, 0);
    }

    public void restart() {
        reset();
        animation.playFromStart();
        gameView.startTimeProgress();
        shopCountdownTimeline.playFromStart();
        startCoinSpawner();
    }

    static boolean gameEnded(Game game, Snake snake) {
        return game.gameWon() || snake.getDead();
    }
}
