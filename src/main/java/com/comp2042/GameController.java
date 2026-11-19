package com.comp2042;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.Point;
import java.util.List;
import java.util.Random;

public class GameController {
    private static final double INVINCIBILITY_SECONDS = 3;
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
    private final SkillInventory skillInventory = new SkillInventory();
    private List<ShopSkill> shopOffers = List.of();
    private final Timeline resumeCountdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> advanceResumeCountdown()));
    private final Timeline shopCountdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> tickShopCountdown()));
    private Timeline coinSpawnTimeline;
    private boolean coinSpawnerStarted;
    private boolean shopOpen;
    private boolean resumeCountdownRunning;
    private boolean resumeAnimationAfterCountdown;
    private boolean resumeFromShopCountdown;
    private boolean reviving;
    private final DoubleProperty invincibilityElapsed = new SimpleDoubleProperty();
    private final Timeline invincibilityTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(invincibilityElapsed, 0)),
            new KeyFrame(Duration.seconds(INVINCIBILITY_SECONDS), new KeyValue(invincibilityElapsed, INVINCIBILITY_SECONDS)));

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
                resumeInvincibility();
            } else if (resumeAnimationAfterCountdown && !gameEnded(game, snek)) {
                animation.play();
                if (!shopCountdown.isFinished()) {
                    shopCountdownTimeline.play();
                    gameView.resumeTimeProgress();
                }
                resumeCoinSpawner();
                resumeInvincibility();
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
            invincibilityTimeline.pause();
            snek.halveLengthForShop();
            gameView.drawGrid(coins, snek, obstacle, gameView.gc, false, 0);
            shopOpen = true;
            shopRefreshRules.reset();
            skillInventory.startShopVisit();
            shopOffers = ShopOfferRules.reroll(random, skillInventory.isBlueCoinUnlocked());
            gameView.showShop(game.getScorePoints(), shopRefreshRules.refreshesUsed(),
                    shopOffers, skillInventory.counts(), snek.getBody().size(), skillInventory.purchasesThisVisit());
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

        return switch (CoinSpawnRules.typeForRoll(random.nextDouble(), skillInventory.isBlueCoinUnlocked())) {
            case RED -> new Coin(position);
            case GOLD -> new GoldCoin(position);
            case BLUE -> new BlueCoin(position);
        };
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
        invincibilityElapsed.addListener((observable, previous, elapsed) ->
                gameView.setSnakeOpacity(invincibilityOpacity(elapsed.doubleValue())));
        invincibilityTimeline.setOnFinished(event -> endInvincibility());
    }

    void initializeOwnedSkills() {
        gameView.updateOwnedSkills(skillInventory.counts());
    }

    static double invincibilityOpacity(double elapsed) {
        if (elapsed >= INVINCIBILITY_SECONDS) {
            return 1;
        }
        double wave = (1 - Math.cos(2 * Math.PI * 5 * elapsed / INVINCIBILITY_SECONDS)) / 2;
        double settle = 1 - Math.clamp((INVINCIBILITY_SECONDS - elapsed) / 0.17, 0, 1);
        return 0.26 + 0.74 * Math.max(wave, settle);
    }

    private void resumeInvincibility() {
        if (invincibilityTimeline.getStatus() == Animation.Status.PAUSED) {
            invincibilityTimeline.play();
        }
    }

    private void endInvincibility() {
        gameView.setSnakeOpacity(1);
        snek.exitInvincibility();
        if (snek.getDead()) {
            handleDeath();
        }
    }

    private void handleDeath() {
        animation.pause();
        shopCountdownTimeline.pause();
        gameView.pauseTimeProgress();
        pauseCoinSpawner();
        gameView.drawGrid(coins, snek, obstacle, gameView.gc, false, 0);
        gameView.gameOver(snek);
        gameView.updateScoreLabel();
        if (!skillInventory.consumeRevive()) {
            animation.stop();
            shopCountdownTimeline.stop();
            gameView.stopTimeProgress();
            stopCoinSpawner();
            return;
        }

        reviving = true;
        gameView.playReviveAnimation(this::finishRevival);
    }

    private void finishRevival() {
        key = switch (snek.reviveFromTailHalf()) {
            case 'U' -> KeyCode.UP;
            case 'D' -> KeyCode.DOWN;
            case 'R' -> KeyCode.RIGHT;
            default -> KeyCode.LEFT;
        };
        keyPressed = false;
        snek.enterInvincibility();
        gameView.setSnakeOpacity(invincibilityOpacity(0));
        initializeOwnedSkills();
        gameView.drawGrid(coins, snek, obstacle, gameView.gc, false, 0);
        gameView.gameOver(snek);
        reviving = false;
        invincibilityTimeline.playFromStart();
        animation.playFromStart();
        shopCountdownTimeline.play();
        gameView.resumeTimeProgress();
        resumeCoinSpawner();
    }

    private void clearRevival() {
        gameView.stopReviveAnimation();
        invincibilityTimeline.stop();
        reviving = false;
        gameView.setSnakeOpacity(1);
    }

    private final Timeline animation = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
        if (gameEnded(game, snek)) {
            gameView.stopTimeProgress();
            return;
        }
        snek.move(switch (key) {
            case RIGHT -> 'R';
            case UP -> 'U';
            case DOWN -> 'D';
            default -> 'L';
        });
        if (snek.getDead()) {
            handleDeath();
            return;
        }

        Coin eatenCoin = snek.ateAnyCoin(coins);
        if (eatenCoin != null) {
            coins.remove(eatenCoin);
            game.addPoints(eatenCoin.getPoints() + skillInventory.bonusPointsFor(eatenCoin));
            if (eatenCoin.growsSnake()) {
                game.incScore();
            }
        }
        keyPressed = false;
        gameView.drawGrid(coins, snek, obstacle, gameView.gc, false, 0);
        gameView.gameWon();
        gameView.gameOver(snek);
        gameView.updateScoreLabel();
        if (game.gameWon()) {
            ((Timeline) event.getSource()).stop();
            shopCountdownTimeline.stop();
            gameView.stopTimeProgress();
            stopCoinSpawner();
            invincibilityTimeline.stop();
            gameView.setSnakeOpacity(1);
        }
    }));

    void filterMovementKey(KeyEvent event) {
        if (event.getCode().isArrowKey() && !shopOpen && !reviving && !pauseState.isPaused()
                && !resumeCountdownRunning && !gameEnded(game, snek)) {
            handle(event);
            event.consume();
        }
    }

    public void handle(KeyEvent event) {
        if (shopOpen || reviving) {
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
            invincibilityTimeline.pause();
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
        if (!shopOpen || resumeFromShopCountdown) {
            return;
        }
        if (!shopRefreshRules.canRefresh(game.getScorePoints())) {
            updateShopView();
            return;
        }
        if (game.spendPoints(ShopRefreshRules.COST)) {
            shopRefreshRules.recordRefresh(game.getScorePoints() + ShopRefreshRules.COST);
            shopOffers = ShopOfferRules.reroll(random, skillInventory.isBlueCoinUnlocked());
            gameView.updateScoreLabel();
            updateShopView();
        }
    }

    public void purchaseShopSkill(ShopSkill skill) {
        if (!shopOpen || resumeFromShopCountdown) {
            return;
        }
        if (!shopOffers.contains(skill) || !skillInventory.purchase(skill, game, snek)) {
            updateShopView();
            return;
        }
        if (skill == ShopSkill.BLUE_COIN) {
            shopOffers = ShopOfferRules.replaceBlueOffer(shopOffers, random);
        }
        gameView.updateScoreLabel();
        gameView.updateOwnedSkills(skillInventory.counts());
        gameView.drawGrid(coins, snek, obstacle, gameView.gc, false, 0);
        updateShopView();
    }

    private void updateShopView() {
        gameView.updateShop(game.getScorePoints(), shopRefreshRules.refreshesUsed(),
                shopOffers, skillInventory.counts(), snek.getBody().size(), skillInventory.purchasesThisVisit());
    }

    public void exitToMenu() {
        if (reviving) {
            return;
        }
        clearRevival();
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
        clearRevival();
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
        skillInventory.reset();
        shopOffers = List.of();
        gameView.updateOwnedSkills(skillInventory.counts());

        gameView.gameWon();
        gameView.gameOver(snek);
        gameView.updateScoreLabel();

        gameView.drawGrid(coins, snek, obstacle, gameView.gc, false, 0);
    }

    public void restart() {
        if (reviving) {
            return;
        }
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
