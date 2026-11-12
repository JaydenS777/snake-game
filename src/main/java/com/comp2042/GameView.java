package com.comp2042;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.util.List;
import java.util.Locale;

public class GameView {
    static final String GAME_BACKGROUND_COLOR = "#07210b";
    private static final double TIMER_PROGRESS_FULL_WIDTH = 320;
    private static final double TIMER_PROGRESS_HEIGHT = 12;

    public Canvas gridCanvas;
    public GraphicsContext gc;

    public Label gameOverLabel;
    public Label gameWonLabel;
    public Label scoreLabel;
    public HBox scoreHud;
    public Label timeLabel;
    public VBox timeHud;
    public Rectangle timeProgressFill;
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
    public Button shopBackButton;
    public Button shopContinueButton;
    public Label shopPointsLabel;
    public Label shopRefreshCountLabel;
    private boolean gameOverVis = false;
    private boolean gameWonVis = false;

    private Game game;
    private double initX;
    private double initY;
    private double fullScale = 1;
    private VBox pausePanel;
    private VBox gameOverPanel;
    private VBox shopPanel;
    private StackPane gamePlayfield;
    private Snake currentSnake;
    private List<Coin> currentCoins;
    private Obstacle currentObstacle;
    private Timeline timeProgressTimeline;
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
        updateShop(points, 0);
        setShopVisible(true);
    }

    public void setShopVisible(boolean visible) {
        shopOverlay.setVisible(visible);
        shopOverlay.setManaged(visible);
    }

    public void updateShop(int points, int refreshesUsed) {
        shopPointsLabel.setText(shopPointsText(points));
        shopRefreshCountLabel.setText(refreshesUsed + "/" + ShopRefreshRules.MAX_REFRESHES);
        shopRefreshButton.setDisable(refreshesUsed >= ShopRefreshRules.MAX_REFRESHES
                || points < ShopRefreshRules.COST);
    }

    public void makeScene(Snake snek, List<Coin> coins) {
        currentSnake = snek;
        currentCoins = coins;
        initX = game.getWidth() * game.getMultiplier();
        initY = game.getHeight() * game.getMultiplier();
        gridCanvas = new Canvas(initX, initY);
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

        scoreLabel = new Label(shopPointsText(game.getScorePoints()));
        scoreLabel.setId("scoreLabel");
        scoreLabel.getStylesheets().add(css);
        scoreLabel.setFont(new Font("Arial", 20));
        scoreLabel.setVisible(true);

        ImageView scoreCoinIcon = new ImageView(new Image("resources/views/res/shop-coin.png"));
        scoreCoinIcon.setFitWidth(48);
        scoreCoinIcon.setFitHeight(48);
        scoreCoinIcon.setPreserveRatio(true);
        scoreCoinIcon.setSmooth(false);
        StackPane scoreCoin = new StackPane(scoreCoinIcon);
        scoreCoin.setMinSize(48, 48);
        scoreCoin.setPrefSize(48, 48);
        scoreHud = new HBox(8, scoreCoin, scoreLabel);
        scoreHud.setId("scoreHud");
        scoreHud.setAlignment(javafx.geometry.Pos.CENTER);
        scoreHud.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        scoreHud.getStylesheets().add(css);

        timeLabel = new Label(timeText(ShopCountdown.DURATION_SECONDS));
        timeLabel.setId("timeLabel");
        timeLabel.getStylesheets().add(css);
        timeLabel.setVisible(true);
        timeHud = createTimerHud(css);

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

        pausePanel = new VBox(14, pauseTitle, resumeButton, pauseRestartButton, exitToMenuButton);
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

        gameOverPanel = new VBox(16, gameOverTitle, finalScoreLabel,
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
        shopOverlay.setManaged(false);

        shopBackButton = new Button("←");
        shopBackButton.getStyleClass().add("shop-back-button");
        shopBackButton.setFocusTraversable(false);

        Label shopTitle = new Label("Shop ✦");
        shopTitle.setId("shopTitle");
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        shopPointsLabel = new Label(shopPointsText(game.getScorePoints()));
        shopPointsLabel.setId("shopPointsLabel");
        ImageView shopCoinIcon = createShopCoinIcon(52);
        HBox shopCurrency = new HBox(14, shopCoinIcon, shopPointsLabel);
        shopCurrency.setId("shopCurrency");
        shopCurrency.setAlignment(Pos.CENTER);
        HBox shopHeader = new HBox(28, shopBackButton, shopTitle, headerSpacer, shopCurrency);
        shopHeader.setId("shopHeader");
        shopHeader.setAlignment(Pos.CENTER_LEFT);

        Button skillsTab = new Button("⚡  Skills");
        skillsTab.getStyleClass().add("shop-skills-tab");
        skillsTab.setFocusTraversable(false);
        VBox shopSidebar = new VBox(skillsTab);
        shopSidebar.setId("shopSidebar");
        shopSidebar.setPrefWidth(270);
        shopSidebar.setMinWidth(220);
        shopSidebar.setMaxWidth(320);

        Label chooseSkill = new Label("◆  Choose a Skill  ◆");
        chooseSkill.setId("chooseSkillTitle");
        Pane emptySkillOptions = new Pane();
        VBox.setVgrow(emptySkillOptions, Priority.ALWAYS);

        shopRefreshButton = new Button();
        shopRefreshButton.getStyleClass().add("shop-refresh-button");
        shopRefreshButton.setFocusTraversable(false);
        Label refreshSymbol = new Label("⟳");
        refreshSymbol.getStyleClass().add("shop-refresh-symbol");
        Label refreshText = new Label("Refresh");
        refreshText.getStyleClass().add("shop-footer-text");
        ImageView refreshCoinIcon = createShopCoinIcon(48);
        Label refreshCost = new Label(Integer.toString(ShopRefreshRules.COST));
        refreshCost.getStyleClass().add("shop-footer-text");
        HBox refreshContent = new HBox(12, refreshSymbol, refreshText, refreshCoinIcon, refreshCost);
        refreshContent.setAlignment(Pos.CENTER);
        shopRefreshButton.setGraphic(refreshContent);

        shopRefreshCountLabel = new Label("0/" + ShopRefreshRules.MAX_REFRESHES);
        shopRefreshCountLabel.setId("shopRefreshCountLabel");
        VBox refreshGroup = new VBox(3, shopRefreshButton, shopRefreshCountLabel);
        refreshGroup.setAlignment(Pos.CENTER);

        shopContinueButton = new Button("▶  Continue");
        shopContinueButton.getStyleClass().add("shop-continue-button");
        shopContinueButton.setFocusTraversable(false);
        HBox shopFooter = new HBox(54, refreshGroup, shopContinueButton);
        shopFooter.setId("shopFooter");
        shopFooter.setAlignment(Pos.CENTER);

        shopPanel = new VBox(20, chooseSkill, emptySkillOptions, shopFooter);
        shopPanel.setId("shopPanel");
        shopPanel.setAlignment(Pos.TOP_CENTER);
        shopPanel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(shopPanel, Priority.ALWAYS);

        HBox shopBody = new HBox(20, shopSidebar, shopPanel);
        shopBody.setId("shopBody");
        VBox.setVgrow(shopBody, Priority.ALWAYS);
        HBox.setHgrow(shopPanel, Priority.ALWAYS);

        VBox shopScreen = new VBox(24, shopHeader, shopBody);
        shopScreen.setId("shopScreen");
        shopScreen.getStylesheets().add(css);
        shopScreen.setPadding(new Insets(22, 26, 26, 26));
        shopScreen.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        shopOverlay.getChildren().add(shopScreen);
        shopOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }

    private VBox createTimerHud(String css) {
        Label hourglass = new Label("⌛");
        hourglass.getStyleClass().add("timer-hourglass");
        HBox timeRow = new HBox(14, hourglass, timeLabel);
        timeRow.setAlignment(Pos.CENTER_LEFT);

        Rectangle track = new Rectangle(TIMER_PROGRESS_FULL_WIDTH, TIMER_PROGRESS_HEIGHT);
        track.setArcWidth(10);
        track.setArcHeight(10);
        track.setFill(Color.web("#16351c"));
        timeProgressFill = new Rectangle(TIMER_PROGRESS_FULL_WIDTH, TIMER_PROGRESS_HEIGHT);
        timeProgressFill.setArcWidth(10);
        timeProgressFill.setArcHeight(10);
        timeProgressFill.setFill(Color.web("#b9ed3b"));
        StackPane progressBar = new StackPane(track, timeProgressFill);
        progressBar.setAlignment(Pos.CENTER_LEFT);
        progressBar.setMinHeight(TIMER_PROGRESS_HEIGHT);
        progressBar.setPrefHeight(TIMER_PROGRESS_HEIGHT);
        progressBar.setMaxWidth(TIMER_PROGRESS_FULL_WIDTH);

        VBox hud = new VBox(12, timeRow, progressBar);
        hud.setId("timeHud");
        hud.getStylesheets().add(css);
        hud.setAlignment(Pos.CENTER_LEFT);
        hud.setMaxWidth(Double.MAX_VALUE);
        return hud;
    }

    public StackPane createGameLayout() {
        String css = "resources/views/styling.css";
        Label heading = new Label("SNAKE");
        heading.getStyleClass().add("sidebar-heading");

        VBox coinPanel = new VBox(10, scoreHud);
        coinPanel.setId("sidebarCoinPanel");
        coinPanel.setAlignment(Pos.CENTER_LEFT);

        Label skillsHeading = new Label("Owned Skills");
        skillsHeading.getStyleClass().add("sidebar-section-heading");
        Pane emptySkills = new Pane();
        VBox.setVgrow(emptySkills, Priority.ALWAYS);
        VBox skillsPanel = new VBox(14, skillsHeading, emptySkills);
        skillsPanel.setId("sidebarSkillsPanel");
        skillsPanel.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(skillsPanel, Priority.ALWAYS);

        VBox sidebar = new VBox(22, heading, timeHud, coinPanel, skillsPanel);
        sidebar.setId("gameSidebar");
        sidebar.setAlignment(Pos.TOP_LEFT);
        sidebar.setMinWidth(280);
        sidebar.setPrefWidth(380);
        sidebar.setMaxWidth(540);
        sidebar.setFillWidth(true);
        sidebar.getStylesheets().add(css);

        gamePlayfield = new StackPane();
        gamePlayfield.setId("gamePlayfield");
        gamePlayfield.setMinSize(0, 0);
        gamePlayfield.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        gamePlayfield.getStylesheets().add(css);
        gamePlayfield.getChildren().addAll(gridCanvas, gameOverLabel, gameWonLabel, restartButton,
                pauseOverlay, resumeCountdownOverlay, gameOverOverlay);
        StackPane.setAlignment(gameOverLabel, Pos.TOP_CENTER);
        StackPane.setMargin(gameOverLabel, new Insets(24, 0, 0, 0));
        StackPane.setAlignment(gameWonLabel, Pos.TOP_CENTER);
        StackPane.setMargin(gameWonLabel, new Insets(24, 0, 0, 0));
        StackPane.setAlignment(restartButton, Pos.CENTER);
        StackPane.setAlignment(pauseOverlay, Pos.CENTER);
        StackPane.setAlignment(resumeCountdownOverlay, Pos.CENTER);
        StackPane.setAlignment(gameOverOverlay, Pos.CENTER);
        StackPane.setAlignment(shopOverlay, Pos.CENTER);

        HBox gameLayout = new HBox(20, sidebar, gamePlayfield);
        gameLayout.setId("gameLayout");
        gameLayout.setPadding(new Insets(20));
        sidebar.prefWidthProperty().bind(gameLayout.widthProperty().multiply(0.26));
        HBox.setHgrow(gamePlayfield, Priority.ALWAYS);

        gamePlayfield.widthProperty().addListener((observable, oldValue, newValue) -> fitCanvasToPlayfield());
        gamePlayfield.heightProperty().addListener((observable, oldValue, newValue) -> fitCanvasToPlayfield());
        StackPane root = new StackPane(gameLayout, shopOverlay);
        root.setId("gameRoot");
        root.getStylesheets().add(css);
        return root;
    }

    private ImageView createShopCoinIcon(double size) {
        ImageView icon = new ImageView(new Image("resources/views/res/shop-coin.png"));
        icon.setFitWidth(size);
        icon.setFitHeight(size);
        icon.setPreserveRatio(true);
        icon.setSmooth(false);
        return icon;
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
        if (primaryStage.isFullScreen() != fullScreen) {
            primaryStage.setFullScreen(fullScreen);
        }
        currentSnake = snek;
        currentCoins = coins;
        currentObstacle = obstacle;
        fitCanvasToPlayfield();
    }

    private void fitCanvasToPlayfield() {
        if (gamePlayfield == null || gridCanvas == null || gamePlayfield.getWidth() <= 0
                || gamePlayfield.getHeight() <= 0 || currentSnake == null || currentCoins == null) {
            return;
        }
        Insets insets = gamePlayfield.getInsets();
        double availableWidth = gamePlayfield.getWidth() - insets.getLeft() - insets.getRight();
        double availableHeight = gamePlayfield.getHeight() - insets.getTop() - insets.getBottom();
        fullScale = Math.min(availableWidth / initX, availableHeight / initY);
        if (!Double.isFinite(fullScale) || fullScale <= 0) {
            return;
        }
        gridCanvas.setWidth(initX * fullScale);
        gridCanvas.setHeight(initY * fullScale);
        updateDisplayTextScale();
        if (currentObstacle != null) {
            drawGrid(currentCoins, currentSnake, currentObstacle, gc, false, 0);
        } else {
            drawGrid(currentCoins, currentSnake, gc);
        }
    }

    private void updateDisplayTextScale() {
        double textScale = Math.max(1.0, DisplayScale.uiScale(fullScale));
        gameOverLabel.setFont(new Font("Arial", 25 * textScale));
        gameWonLabel.setFont(new Font("Arial", 25 * textScale));
        restartButton.setFont(new Font("Arial", 18 * textScale));
        restartButton.setPrefSize(140 * textScale, 48 * textScale);
        double scoreScale = Math.min(1.5, textScale);
        scoreLabel.setStyle("-fx-font-size: " + (27 * scoreScale) + "px;");
        scoreHud.setScaleX(scoreScale);
        scoreHud.setScaleY(scoreScale);
        resumeCountdownLabel.setStyle("-fx-font-size: " + (84 * textScale) + "px;");
        timeLabel.setStyle("-fx-font-size: " + (32 * textScale) + "px;");
        pausePanel.setScaleX(textScale);
        pausePanel.setScaleY(textScale);
        gameOverPanel.setScaleX(textScale);
        gameOverPanel.setScaleY(textScale);
    }

    public void drawGrid(List<Coin> coins, Snake snek, Obstacle obstacle, GraphicsContext gc, boolean a, int b) {
        drawGameBackground(gc);

        drawCoins(coins, gc);

        drawSnake(snek);

        gc.setFill(obstacle.getColor());
        for (Point point : obstacle.getObstacles()) {
            gc.fillRect(point.x * game.getMultiplier() * fullScale, point.y * game.getMultiplier() * fullScale, game.getMultiplier() * fullScale, game.getMultiplier() * fullScale);
        }
    }

    private void drawGrid(List<Coin> coins, Snake snek, GraphicsContext gc) {
        drawGameBackground(gc);

        drawCoins(coins, gc);

        drawSnake(snek);
    }

    private void drawSnake(Snake snek) {
        gc.setImageSmoothing(false);
        List<Point> body = snek.getBody();
        for (int index = 0; index < body.size(); index++) {
            drawBody(snek, body, snek.getHead(), body.get(index), index);
        }
        drawBodySeams(body, snek.getHead());
        drawHead(snek);
        gc.setImageSmoothing(true);
    }

    private void drawBodySeams(List<Point> body, Point head) {
        double cellSize = game.getMultiplier() * fullScale;
        gc.setStroke(Color.web("#24581d"));
        gc.setLineWidth(Math.max(1.5, 2.0 * fullScale));
        for (int index = 0; index < body.size(); index++) {
            Point from = body.get(index);
            Point to = index + 1 < body.size() ? body.get(index + 1) : head;
            int direction = SnakeGeometry.directionBetween(from, to, game.getWidth(), game.getHeight());
            drawBodySeam(from, direction, cellSize);
        }
    }

    private void drawBodySeam(Point from, int direction, double cellSize) {
        double centerX = (from.x + 0.5) * cellSize;
        double centerY = (from.y + 0.5) * cellSize;
        double halfSpan = cellSize * 0.34;
        switch (direction) {
            case SnakeGeometry.RIGHT -> drawVerticalSeam((from.x + 1) * cellSize, centerY, halfSpan);
            case SnakeGeometry.LEFT -> drawVerticalSeam(from.x * cellSize, centerY, halfSpan);
            case SnakeGeometry.DOWN -> drawHorizontalSeam(centerX, (from.y + 1) * cellSize, halfSpan);
            case SnakeGeometry.UP -> drawHorizontalSeam(centerX, from.y * cellSize, halfSpan);
            default -> throw new IllegalArgumentException("Unknown snake segment direction.");
        }
        if (direction == SnakeGeometry.RIGHT && from.x == game.getWidth() - 1) {
            drawVerticalSeam(0, centerY, halfSpan);
        } else if (direction == SnakeGeometry.LEFT && from.x == 0) {
            drawVerticalSeam(gridCanvas.getWidth(), centerY, halfSpan);
        } else if (direction == SnakeGeometry.DOWN && from.y == game.getHeight() - 1) {
            drawHorizontalSeam(centerX, 0, halfSpan);
        } else if (direction == SnakeGeometry.UP && from.y == 0) {
            drawHorizontalSeam(centerX, gridCanvas.getHeight(), halfSpan);
        }
    }

    private void drawVerticalSeam(double x, double centerY, double halfSpan) {
        gc.strokeLine(x, centerY - halfSpan, x, centerY + halfSpan);
    }

    private void drawHorizontalSeam(double centerX, double y, double halfSpan) {
        gc.strokeLine(centerX - halfSpan, y, centerX + halfSpan, y);
    }

    private void drawGameBackground(GraphicsContext graphics) {
        graphics.setFill(Color.web(GAME_BACKGROUND_COLOR));
        graphics.fillRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());
    }

    private void drawCoins(List<Coin> coins, GraphicsContext gc) {
        gc.setImageSmoothing(false);
        for (Coin coin : coins) {
            Point position = coin.getPos();
            gc.drawImage(coin.getImage(), (position.x * game.getMultiplier()) * fullScale,
                    (position.y * game.getMultiplier()) * fullScale,
                    game.getMultiplier() * fullScale - 3 * fullScale,
                    game.getMultiplier() * fullScale - 3 * fullScale);
        }
        gc.setImageSmoothing(true);
    }

    private void drawHead(Snake snek) {
        double cellSize = game.getMultiplier() * fullScale;
        double drawSize = cellSize * 1.2;
        Point head = snek.getHead();
        drawSprite(snek.getSpriteSheet(), SnakeSpriteSheet.HEAD, snek.getSnakeDir(),
                head.x * cellSize + cellSize / 2, head.y * cellSize + cellSize / 2, drawSize);
    }

    private void drawBody(Snake snek, List<Point> body, Point head, Point point, int index) {
        int rotation;
        SnakeSpriteSheet.SpriteRegion sprite;

        if (index == 0) {
            Point nextSegment = body.size() > 1 ? body.get(1) : head;
            int directionToHead = SnakeGeometry.directionBetween(point, nextSegment,
                    game.getWidth(), game.getHeight());
            rotation = SnakeGeometry.tailRotation(directionToHead);
            sprite = SnakeSpriteSheet.TAIL;
        } else {
            Point previousSegment = body.get(index - 1);
            int directionFromTailToHead = SnakeGeometry.directionBetween(previousSegment, point,
                    game.getWidth(), game.getHeight());
            // Keep the tile's highlights and segment joins oriented with the actual path.
            rotation = SnakeGeometry.bodyRotation(directionFromTailToHead);
            sprite = SnakeSpriteSheet.BODY;
        }

        double cellSize = game.getMultiplier() * fullScale;
        double drawSize = cellSize * 1.2;
        drawSprite(snek.getSpriteSheet(), sprite, rotation,
                point.x * cellSize + cellSize / 2, point.y * cellSize + cellSize / 2, drawSize);
    }

    private void drawSprite(Image sheet, SnakeSpriteSheet.SpriteRegion sprite, int rotation,
                            double centerX, double centerY, double drawSize) {
        gc.save();
        gc.translate(centerX, centerY);
        gc.rotate(rotation);
        gc.drawImage(sheet,
                sprite.x(), sprite.y(), sprite.width(), sprite.height(),
                -drawSize / 2, -drawSize / 2, drawSize, drawSize);
        gc.restore();
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
        scoreLabel.setText(shopPointsText(game.getScorePoints()));
    }

    public void updateTimeLabel(int remainingSeconds) {
        timeLabel.setText(timeText(remainingSeconds));
        if (remainingSeconds <= 0) {
            stopTimeProgress();
            timeProgressFill.setWidth(0.0);
        } else if (remainingSeconds >= ShopCountdown.DURATION_SECONDS) {
            stopTimeProgress();
            timeProgressFill.setWidth(TIMER_PROGRESS_FULL_WIDTH);
        }
    }

    public void startTimeProgress() {
        stopTimeProgress();
        timeProgressFill.setWidth(TIMER_PROGRESS_FULL_WIDTH);
        timeProgressTimeline = new Timeline(new KeyFrame(
                Duration.seconds(ShopCountdown.DURATION_SECONDS),
                new KeyValue(timeProgressFill.widthProperty(), 0.0, Interpolator.LINEAR)));
        timeProgressTimeline.playFromStart();
    }

    public void pauseTimeProgress() {
        if (timeProgressTimeline != null
                && timeProgressTimeline.getStatus() == javafx.animation.Animation.Status.RUNNING) {
            timeProgressTimeline.pause();
        }
    }

    public void resumeTimeProgress() {
        if (timeProgressTimeline != null
                && timeProgressTimeline.getStatus() == javafx.animation.Animation.Status.PAUSED) {
            timeProgressTimeline.play();
        }
    }

    public void stopTimeProgress() {
        if (timeProgressTimeline != null) {
            timeProgressTimeline.stop();
            timeProgressTimeline = null;
        }
    }

    static String timeText(int remainingSeconds) {
        int seconds = Math.max(0, remainingSeconds);
        return String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60);
    }

    static double timeProgress(int remainingSeconds) {
        return Math.max(0.0, Math.min(1.0,
                remainingSeconds / (double) ShopCountdown.DURATION_SECONDS));
    }

    static String shopPointsText(int points) {
        return String.format(Locale.ROOT, "%,d", points);
    }

}
