package com.comp2042;

import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.awt.*;
import java.util.List;

public class GameView {

    public Canvas gridCanvas;
    public GraphicsContext gc;

    public Label gameOverLabel;
    public Label gameWonLabel;
    public Label scoreLabel;
    public Label timeLabel;
    public Button restartButton;
    public StackPane gameOverOverlay;
    public Label finalScoreLabel;
    public Button gameOverRestartButton;
    public Button gameOverExitButton;
    public StackPane pauseOverlay;
    public Button resumeButton;
    public Button pauseRestartButton;
    public Button exitToMenuButton;
    public StackPane resumeCountdownOverlay;
    public Label resumeCountdownLabel;
    public StackPane shopOverlay;
    public Button shopRefreshButton;
    public Button shopContinueButton;
    public Label shopPointsLabel;
    private boolean gameOverVis = false;
    private boolean gameWonVis = false;

    private Game game;
    private double initX;
    private double initY;
    private double fullScale = 1;
    private boolean fullsize = false;
    public GameView(Game game) {
        this.game = game;
    }

    public void setPaused(boolean paused) {
        pauseOverlay.setVisible(paused);
    }

    public void showResumeCountdown(String number) {
        resumeCountdownLabel.setText(number);
        resumeCountdownOverlay.setVisible(!number.isEmpty());
    }

    public void showShop(int points) {
        shopPointsLabel.setText("POINTS: " + points);
        shopOverlay.setVisible(true);
    }

    public void setShopVisible(boolean visible) {
        shopOverlay.setVisible(visible);
    }

    public void makeScene(Scene scene, Snake snek, List<Coin> coins) {
        Scene gameScene = scene;
        gridCanvas = new Canvas(gameScene.getWidth(), gameScene.getHeight());
        initX = scene.getWidth();
        initY = scene.getHeight();
        gc = gridCanvas.getGraphicsContext2D();
        drawGrid(coins, snek, gc);

        gameOverLabel = new Label("GAME OVER");
        gameOverLabel.setId("gameOverLabel");
        String css = "resources/views/styling.css";
        gameOverLabel.getStylesheets().add(css);
        gameOverLabel.setVisible(false);

        gameWonLabel = new Label("YOU WON!");
        gameWonLabel.setId("gameWonLabel");
        gameWonLabel.getStylesheets().add(css);
        gameWonLabel.setVisible(false);

        scoreLabel = new Label("Score: " + game.getScorePoints());
        scoreLabel.setId("scoreLabel");
        scoreLabel.getStylesheets().add(css);
        scoreLabel.setFont(new Font("Arial", 20));
        scoreLabel.setVisible(true);

        timeLabel = new Label(timeText(ShopCountdown.DURATION_SECONDS));
        timeLabel.setId("timeLabel");
        timeLabel.getStylesheets().add(css);
        timeLabel.setVisible(true);

        restartButton = new Button("Restart");
        restartButton.setId("restartButton");
        restartButton.getStylesheets().add(css);
        restartButton.setFont(new Font("Arial", 18));
        restartButton.setPrefSize(140, 48);
        restartButton.setFocusTraversable(false);
        restartButton.setVisible(false);

        pauseOverlay = new StackPane();
        pauseOverlay.setId("pauseOverlay");
        pauseOverlay.setVisible(false);
        pauseOverlay.getStylesheets().add(css);

        Label pauseTitle = new Label("PAUSED");
        pauseTitle.setId("pauseTitle");

        resumeButton = createPauseButton("Resume");
        pauseRestartButton = createPauseButton("Restart Game");
        exitToMenuButton = createPauseButton("Exit to Menu");

        VBox pausePanel = new VBox(14, pauseTitle, resumeButton, pauseRestartButton, exitToMenuButton);
        pausePanel.setId("pausePanel");
        pausePanel.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        pausePanel.setAlignment(javafx.geometry.Pos.CENTER);
        pauseOverlay.getChildren().add(pausePanel);
        pauseOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        resumeCountdownLabel = new Label();
        resumeCountdownLabel.setId("resumeCountdownLabel");
        resumeCountdownOverlay = new StackPane(resumeCountdownLabel);
        resumeCountdownOverlay.getStylesheets().add(css);
        resumeCountdownOverlay.getStyleClass().add("resume-countdown-overlay");
        resumeCountdownOverlay.setVisible(false);

        gameOverOverlay = new StackPane();
        gameOverOverlay.setId("gameOverOverlay");
        gameOverOverlay.getStylesheets().add(css);
        gameOverOverlay.setVisible(false);

        Label gameOverTitle = new Label("GAME OVER");
        gameOverTitle.setId("gameOverTitle");

        finalScoreLabel = new Label(finalScoreText(game.getScorePoints()));
        finalScoreLabel.setId("finalScoreLabel");

        gameOverRestartButton = createOverlayButton("Restart", "game-over-menu-button");
        gameOverExitButton = createOverlayButton("Main Menu", "game-over-menu-button");

        VBox gameOverPanel = new VBox(16, gameOverTitle, finalScoreLabel,
                gameOverRestartButton, gameOverExitButton);
        gameOverPanel.setId("gameOverPanel");
        gameOverPanel.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        gameOverPanel.setAlignment(javafx.geometry.Pos.CENTER);
        gameOverOverlay.getChildren().add(gameOverPanel);
        gameOverOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        shopOverlay = new StackPane();
        shopOverlay.setId("shopOverlay");
        shopOverlay.getStylesheets().add(css);
        shopOverlay.setVisible(false);

        Label shopTitle = new Label("SNAKE SHOP");
        shopTitle.setId("shopTitle");
        Label shopSubtitle = new Label("Spend your points on items");
        shopSubtitle.setId("shopSubtitle");

        shopRefreshButton = new Button("REFRESH");
        shopRefreshButton.getStyleClass().add("shop-refresh-button");
        shopRefreshButton.setPrefSize(108, 42);
        shopRefreshButton.setFocusTraversable(false);
        shopRefreshButton.setDisable(true);

        shopPointsLabel = new Label("POINTS: " + game.getScorePoints());
        shopPointsLabel.setId("shopPointsLabel");
        HBox shopStatusRow = new HBox(12, shopRefreshButton, shopPointsLabel);
        shopStatusRow.setId("shopStatusRow");
        shopStatusRow.setAlignment(javafx.geometry.Pos.CENTER);

        HBox shopItems = new HBox(8, createShopItemCard(1), createShopItemCard(2), createShopItemCard(3));
        shopItems.setId("shopItems");
        shopItems.setAlignment(javafx.geometry.Pos.CENTER);

        shopContinueButton = createOverlayButton("Continue Game", "shop-continue-button");
        shopContinueButton.setPrefSize(316, 48);

        VBox shopPanel = new VBox(12, shopTitle, shopSubtitle, shopStatusRow,
                shopItems, shopContinueButton);
        shopPanel.setId("shopPanel");
        shopPanel.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        shopPanel.setAlignment(javafx.geometry.Pos.CENTER);
        shopOverlay.getChildren().add(shopPanel);
        shopOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }

    private VBox createShopItemCard(int itemNumber) {
        Label itemTitle = new Label("ITEM " + itemNumber);
        itemTitle.getStyleClass().add("shop-item-title");
        Label itemEffect = new Label("Effect: TBD");
        itemEffect.getStyleClass().add("shop-item-detail");
        Label itemPrice = new Label("Price: --");
        itemPrice.getStyleClass().add("shop-item-detail");

        Button buyButton = new Button("BUY");
        buyButton.getStyleClass().add("shop-buy-button");
        buyButton.setPrefSize(88, 38);
        buyButton.setFocusTraversable(false);
        buyButton.setDisable(true);

        VBox card = new VBox(8, itemTitle, itemEffect, itemPrice, buyButton);
        card.getStyleClass().add("shop-item-card");
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setPrefSize(98, 132);
        card.setMinSize(98, 132);
        return card;
    }

    private Button createPauseButton(String text) {
        return createOverlayButton(text, "pause-menu-button");
    }

    private Button createOverlayButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        button.setPrefSize(220, 48);
        return button;
    }

    public void scaleToFullscreen(boolean fullScreen, Stage primaryStage, Obstacle obstacle, Snake snek, List<Coin> coins) {
        primaryStage.setFullScreen(fullScreen);

        double xScaling = Screen.getPrimary().getVisualBounds().getHeight() / initY;
        double yScaling = Screen.getPrimary().getVisualBounds().getWidth() / initX;

        if (fullScreen && !fullsize) {
            if (xScaling < yScaling) {
                fullScale = xScaling;
            } else {
                fullScale = yScaling;
            }
            gridCanvas.setWidth(gridCanvas.getWidth() * fullScale);
            gridCanvas.setHeight(gridCanvas.getHeight() * fullScale);
            fullsize = true;
        } else if (!fullScreen) {
            fullScale = 1;
            gridCanvas.setWidth(initX);
            gridCanvas.setHeight(initY);
            fullsize = false;
        }

        gameOverLabel.setFont(new Font("Arial", 25 * fullScale));
        gameWonLabel.setFont(new Font("Arial", 25 * fullScale));
        restartButton.setFont(new Font("Arial", 18 * fullScale));
        restartButton.setPrefSize(140 * fullScale, 48 * fullScale);
        drawGrid(coins, snek, obstacle, gc, false, 0);
    }

    public void drawGrid(List<Coin> coins, Snake snek, Obstacle obstacle, GraphicsContext gc, boolean a, int b) {
        gc.setFill(game.getColor());
        gc.fillRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());

        drawCoins(coins, gc);

        List<Point> body = snek.getBody();
        int bodyIndex = 0;
        for (Point point : body) {
            drawBody(snek, body, snek.getHead(), point, bodyIndex);
            bodyIndex++;
        }

        drawHead(snek);

        gc.setFill(obstacle.getColor());
        for (Point point : obstacle.getObstacles()) {
            gc.fillRect(point.x * game.getMultiplier() * fullScale, point.y * game.getMultiplier() * fullScale, game.getMultiplier() * fullScale, game.getMultiplier() * fullScale);
        }
    }

    private void drawGrid(List<Coin> coins, Snake snek, GraphicsContext gc) {
        gc.setFill(game.getColor());
        gc.fillRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());

        drawCoins(coins, gc);

        List<Point> body = snek.getBody();
        int bodyIndex = 0;
        for (Point point : body) {
            drawBody(snek, body, snek.getHead(), point, bodyIndex);
            bodyIndex++;
        }

        drawHead(snek);
    }

    private void drawCoins(List<Coin> coins, GraphicsContext gc) {
        for (Coin coin : coins) {
            Point position = coin.getPos();
            gc.drawImage(coin.getImage(), (position.x * game.getMultiplier()) * fullScale,
                    (position.y * game.getMultiplier()) * fullScale,
                    game.getMultiplier() * fullScale - 3 * fullScale,
                    game.getMultiplier() * fullScale - 3 * fullScale);
        }
    }

    private void drawHead(Snake snek) {
        snek.getImageViewHead().setRotate(snek.getSnakeDir());
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        Image rotatedImage = snek.getImageViewHead().snapshot(params, null);
        gc.drawImage(rotatedImage, (snek.getHead().x * game.getMultiplier()) * fullScale, (snek.getHead().y * game.getMultiplier()) * fullScale, game.getMultiplier() * fullScale, game.getMultiplier() * fullScale);
    }

    private void drawBody(Snake snek, List<Point> body, Point head, Point point, int index) {
        Image rotatedImage;
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        if (index == 0) {
            Point nextSegment = body.size() > 1 ? body.get(1) : head;
            int tailDirection = SnakeGeometry.directionBetween(point, nextSegment,
                    game.getWidth(), game.getHeight());
            snek.getImageViewTail().setRotate(tailDirection);
            rotatedImage = snek.getImageViewTail().snapshot(params, null);
        } else {
            Point previousSegment = body.get(index - 1);
            Point nextSegment = index + 1 < body.size() ? body.get(index + 1) : head;
            int incomingDirection = SnakeGeometry.directionBetween(previousSegment, point,
                    game.getWidth(), game.getHeight());
            int outgoingDirection = SnakeGeometry.directionBetween(point, nextSegment,
                    game.getWidth(), game.getHeight());
            int turnRotation = SnakeGeometry.turnRotation(incomingDirection, outgoingDirection);

            if (turnRotation >= 0) {
                // Preserve the existing corner-sprite rotations, now derived from adjacent cells.
                snek.getImageViewTurn().setRotate(turnRotation);
                rotatedImage = snek.getImageViewTurn().snapshot(params, null);
            } else {
                snek.getImageViewBody().setRotate(incomingDirection == SnakeGeometry.LEFT
                        || incomingDirection == SnakeGeometry.RIGHT ? 270 : 0);
                rotatedImage = snek.getImageViewBody().snapshot(params, null);
            }
        }

        gc.drawImage(rotatedImage, (point.x * game.getMultiplier()) * fullScale,
                (point.y * game.getMultiplier()) * fullScale,
                game.getMultiplier() * fullScale * fullScale,
                game.getMultiplier() * fullScale * fullScale);
    }

    public void gameOver(Snake snek) {
        if (snek.getDead() && !gameOverVis) {
            gameOverVis = true;
        } else if (!snek.getDead() && gameOverVis) {
            gameOverVis = false;
        }
        gameOverLabel.setVisible(false);
        gameOverOverlay.setVisible(shouldShowGameOverOverlay(snek.getDead()));
        if (snek.getDead()) {
            finalScoreLabel.setText(finalScoreText(game.getScorePoints()));
        }
        restartButton.setVisible(shouldShowRestartButton(game.gameWon()));
    }

    public void gameWon() {
        if (game.gameWon() && !gameWonVis) {
            gameWonVis = true;
            gameWonLabel.setVisible(true);
        } else if (!game.gameWon() && gameWonVis) {
            gameWonVis = false;
            gameWonLabel.setVisible(false);
        }
        restartButton.setVisible(shouldShowRestartButton(game.gameWon()));
    }

    static boolean shouldShowGameOverOverlay(boolean gameOver) {
        return gameOver;
    }

    static String finalScoreText(int score) {
        return "Final Score: " + score;
    }

    static boolean shouldShowRestartButton(boolean gameWon) {
        return gameWon;
    }

    public void updateScoreLabel() {
        scoreLabel.setText("Score: " + game.getScorePoints());
    }

    public void updateTimeLabel(int remainingSeconds) {
        timeLabel.setText(timeText(remainingSeconds));
    }

    static String timeText(int remainingSeconds) {
        return "TIME: " + remainingSeconds;
    }

}
