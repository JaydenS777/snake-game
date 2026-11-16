package com.comp2042;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class GameView {
    static final String GAME_BACKGROUND_COLOR = "#07210b";
    private static final double TIMER_PROGRESS_FULL_WIDTH = 320;
    private static final double TIMER_PROGRESS_HEIGHT = 12;

    public Canvas gridCanvas;
    public GraphicsContext gc;
    private Canvas snakeCanvas;
    private GraphicsContext snakeGraphics;

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
    private Label chooseSkill;
    private final List<ShopSkillCardView> shopSkillCardViews = new ArrayList<>();
    private Consumer<ShopSkill> shopSkillPurchaseHandler = skill -> { };
    private GridPane ownedSkillsGrid;
    private ScrollPane ownedSkillsScroll;
    private ImageView ownedReviveImage;
    private ReviveAnimation reviveAnimation;
    private DoubleBinding ownedSkillCardWidth;
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
    private final Map<ShopSkill, Image> shopSkillImages = new EnumMap<>(ShopSkill.class);
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

    public void showShop(int points, int refreshesUsed, List<ShopSkill> offers,
                         Map<ShopSkill, Integer> ownedSkills, int bodySegments, int purchasesThisVisit) {
        updateShop(points, refreshesUsed, offers, ownedSkills, bodySegments, purchasesThisVisit);
        setShopVisible(true);
    }

    public void setShopVisible(boolean visible) {
        shopOverlay.setVisible(visible);
        shopOverlay.setManaged(visible);
    }

    public void updateShop(int points, int refreshesUsed, List<ShopSkill> offers,
                           Map<ShopSkill, Integer> ownedSkills, int bodySegments, int purchasesThisVisit) {
        shopPointsLabel.setText(shopPointsText(points));
        shopRefreshCountLabel.setText(refreshesUsed + "/" + ShopRefreshRules.MAX_REFRESHES);
        shopRefreshButton.setDisable(refreshesUsed >= ShopRefreshRules.MAX_REFRESHES
                || points < ShopRefreshRules.COST);
        int purchaseLimit = SkillInventory.MAX_PURCHASES_PER_VISIT;
        chooseSkill.setText("◆  Choose a Skill  ◆    " + purchasesThisVisit + "/" + purchaseLimit);
        for (int i = 0; i < shopSkillCardViews.size(); i++) {
            ShopSkillCardView card = shopSkillCardViews.get(i);
            boolean hasOffer = i < offers.size();
            card.root.setManaged(hasOffer);
            card.root.setVisible(hasOffer);
            if (hasOffer) {
                ShopSkill offer = offers.get(i);
                card.update(offer, points, ownedSkills.getOrDefault(offer, 0),
                        bodySegments, purchasesThisVisit >= purchaseLimit);
            }
        }
    }

    public void setShopSkillPurchaseHandler(Consumer<ShopSkill> handler) {
        shopSkillPurchaseHandler = handler == null ? skill -> { } : handler;
    }

    public void updateOwnedSkills(Map<ShopSkill, Integer> ownedSkills) {
        if (ownedSkillsGrid == null) {
            return;
        }
        ownedSkillsGrid.getChildren().clear();
        ownedReviveImage = null;
        int cardIndex = 0;
        for (ShopSkill skill : ShopSkill.values()) {
            int ownedCount = ownedSkills.getOrDefault(skill, 0);
            if (ownedCount == 0) {
                continue;
            }
            ImageView cardImage = new ImageView(shopSkillImages.get(skill));
            cardImage.setPreserveRatio(true);
            cardImage.setSmooth(true);
            cardImage.fitWidthProperty().bind(ownedSkillCardWidth);
            cardImage.setAccessibleText(skill.title());
            Tooltip.install(cardImage, new Tooltip(skill.title() + " — " + skill.description()));
            if (skill == ShopSkill.REVIVE) {
                ownedReviveImage = cardImage;
            }
            if (skill == ShopSkill.REVIVE || skill == ShopSkill.RED_COIN_UPGRADE
                    || skill == ShopSkill.GOLD_COIN_UPGRADE) {
                Label count = new Label("×" + ownedCount);
                count.getStyleClass().add("owned-skill-count");
                count.setMouseTransparent(true);
                StackPane tile = new StackPane(cardImage, count);
                StackPane.setAlignment(count, Pos.TOP_RIGHT);
                ownedSkillsGrid.add(tile, cardIndex % 2, cardIndex / 2);
            } else {
                ownedSkillsGrid.add(cardImage, cardIndex % 2, cardIndex / 2);
            }
            cardIndex++;
        }
    }

    public void makeScene(Snake snek, List<Coin> coins) {
        for (ShopSkill skill : ShopSkill.values()) {
            shopSkillImages.put(skill, new Image("resources/views/res/" + skill.imageResource()));
        }
        currentSnake = snek;
        currentCoins = coins;
        initX = game.getWidth() * game.getMultiplier();
        initY = game.getHeight() * game.getMultiplier();
        gridCanvas = new Canvas(initX, initY);
        gc = gridCanvas.getGraphicsContext2D();
        snakeCanvas = new Canvas(initX, initY);
        snakeCanvas.setMouseTransparent(true);
        snakeGraphics = snakeCanvas.getGraphicsContext2D();
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

        chooseSkill = new Label("◆  Choose a Skill  ◆");
        chooseSkill.setId("chooseSkillTitle");
        HBox shopCards = new HBox(18);
        shopCards.setId("shopCards");
        shopCards.setAlignment(Pos.CENTER);
        for (int i = 0; i < ShopSkill.values().length; i++) {
            ShopSkillCardView cardView = new ShopSkillCardView();
            shopSkillCardViews.add(cardView);
            shopCards.getChildren().add(cardView.root);
            HBox.setHgrow(cardView.root, Priority.ALWAYS);
        }
        VBox.setVgrow(shopCards, Priority.ALWAYS);

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

        shopContinueButton = new Button("▶  Continue");
        shopContinueButton.getStyleClass().add("shop-continue-button");
        shopContinueButton.setFocusTraversable(false);
        GridPane shopFooter = new GridPane();
        shopFooter.setId("shopFooter");
        shopFooter.setAlignment(Pos.CENTER);
        shopFooter.setVgap(3);
        shopFooter.setMaxWidth(Region.USE_PREF_SIZE);
        ColumnConstraints refreshColumn = new ColumnConstraints();
        refreshColumn.setPercentWidth(50);
        ColumnConstraints continueColumn = new ColumnConstraints();
        continueColumn.setPercentWidth(50);
        shopFooter.getColumnConstraints().addAll(refreshColumn, continueColumn);
        for (Button button : List.of(shopRefreshButton, shopContinueButton)) {
            button.setMinWidth(0);
            button.setMaxWidth(Double.MAX_VALUE);
        }
        shopFooter.addRow(0, shopRefreshButton, shopContinueButton);
        shopFooter.add(shopRefreshCountLabel, 0, 1);
        GridPane.setHalignment(shopRefreshCountLabel, javafx.geometry.HPos.CENTER);

        shopPanel = new VBox(20, chooseSkill, shopCards, shopFooter);
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
        ownedSkillsGrid = new GridPane();
        ownedSkillsGrid.setHgap(10);
        ownedSkillsGrid.setVgap(10);
        ownedSkillsScroll = new ScrollPane(ownedSkillsGrid);
        ownedSkillsScroll.setId("ownedSkillsScroll");
        ownedSkillsScroll.setFitToWidth(true);
        ownedSkillsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        ownedSkillsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        ownedSkillsScroll.setPannable(true);
        ownedSkillsScroll.setMaxWidth(Double.MAX_VALUE);
        ownedSkillCardWidth = Bindings.createDoubleBinding(
                () -> Math.max(1, Math.floor((ownedSkillsScroll.getViewportBounds().getWidth()
                        - ownedSkillsGrid.getHgap()) / 2)),
                ownedSkillsScroll.viewportBoundsProperty(), ownedSkillsGrid.hgapProperty());
        VBox.setVgrow(ownedSkillsScroll, Priority.ALWAYS);
        VBox skillsPanel = new VBox(14, skillsHeading, ownedSkillsScroll);
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
        gamePlayfield.getChildren().addAll(gridCanvas, snakeCanvas, gameOverLabel, gameWonLabel, restartButton);
        StackPane.setAlignment(gameOverLabel, Pos.TOP_CENTER);
        StackPane.setMargin(gameOverLabel, new Insets(24, 0, 0, 0));
        StackPane.setAlignment(gameWonLabel, Pos.TOP_CENTER);
        StackPane.setMargin(gameWonLabel, new Insets(24, 0, 0, 0));
        StackPane.setAlignment(restartButton, Pos.CENTER);

        HBox gameLayout = new HBox(20, sidebar, gamePlayfield);
        gameLayout.setId("gameLayout");
        gameLayout.setPadding(new Insets(20));
        sidebar.prefWidthProperty().bind(gameLayout.widthProperty().multiply(0.26));
        HBox.setHgrow(gamePlayfield, Priority.ALWAYS);

        gamePlayfield.widthProperty().addListener((observable, oldValue, newValue) -> fitCanvasToPlayfield());
        gamePlayfield.heightProperty().addListener((observable, oldValue, newValue) -> fitCanvasToPlayfield());
        StackPane root = new StackPane(gameLayout, pauseOverlay, gameOverOverlay,
                resumeCountdownOverlay, shopOverlay);
        root.setId("gameRoot");
        root.getStylesheets().add(css);
        configureWindowOverlays(root);
        reviveAnimation = new ReviveAnimation(root);
        return root;
    }

    void playReviveAnimation(Runnable onFinished) {
        if (ownedReviveImage != null) {
            ownedSkillsScroll.applyCss();
            ownedSkillsScroll.layout();
            double viewport = ownedSkillsScroll.getViewportBounds().getHeight();
            double scrollable = ownedSkillsGrid.getHeight() - viewport;
            if (scrollable > 0) {
                double cardY = ownedReviveImage.getParent().getBoundsInParent().getCenterY();
                ownedSkillsScroll.setVvalue(Math.clamp((cardY - viewport / 2) / scrollable, 0, 1));
                ownedSkillsScroll.layout();
            }
        }
        reviveAnimation.play(ownedReviveImage, shopSkillImages.get(ShopSkill.REVIVE), onFinished);
    }

    void stopReviveAnimation() {
        reviveAnimation.stop();
    }

    void setSnakeOpacity(double opacity) {
        snakeCanvas.setOpacity(opacity);
    }

    private void configureWindowOverlays(StackPane root) {
        for (StackPane overlay : List.of(pauseOverlay, gameOverOverlay, resumeCountdownOverlay)) {
            overlay.setMinSize(0, 0);
            overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            overlay.setPickOnBounds(true);
            overlay.managedProperty().bind(overlay.visibleProperty());
        }

        DoubleBinding overlayScale = Bindings.createDoubleBinding(
                () -> Math.clamp(Math.min(root.getWidth() / 780, root.getHeight() / 460), 0.8, 2.8),
                root.widthProperty(), root.heightProperty());
        pausePanel.scaleXProperty().bind(overlayScale);
        pausePanel.scaleYProperty().bind(overlayScale);
        gameOverPanel.scaleXProperty().bind(overlayScale);
        gameOverPanel.scaleYProperty().bind(overlayScale);
        resumeCountdownLabel.scaleXProperty().bind(overlayScale);
        resumeCountdownLabel.scaleYProperty().bind(overlayScale);
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

    private final class ShopSkillCardView {
        private static final double CARD_ASPECT_RATIO = 1402.0 / 1122.0;

        private final StackPane root = new StackPane();
        private final ImageView cardImage = new ImageView();
        private final Tooltip tooltip = new Tooltip("Click the card to buy");
        private ShopSkill skill;

        private ShopSkillCardView() {
            root.getStyleClass().add("shop-skill-card-image");
            root.setAlignment(Pos.CENTER);
            root.setMinWidth(0);
            root.setPrefWidth(300);
            root.setMaxWidth(Double.MAX_VALUE);
            root.prefHeightProperty().bind(root.widthProperty().multiply(CARD_ASPECT_RATIO));
            root.setMaxHeight(Double.MAX_VALUE);
            root.setFocusTraversable(true);
            root.setCursor(javafx.scene.Cursor.HAND);
            root.setPickOnBounds(true);
            Tooltip.install(root, tooltip);

            cardImage.setPreserveRatio(true);
            cardImage.setSmooth(true);
            cardImage.fitWidthProperty().bind(root.widthProperty());
            cardImage.fitHeightProperty().bind(root.heightProperty());

            root.getChildren().add(cardImage);
            root.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    purchaseCurrentSkill();
                }
            });
            root.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER
                        || event.getCode() == javafx.scene.input.KeyCode.SPACE) {
                    purchaseCurrentSkill();
                    event.consume();
                }
            });
        }

        private void purchaseCurrentSkill() {
            if (skill != null && !root.isDisabled()) {
                shopSkillPurchaseHandler.accept(skill);
            }
        }

        private void update(ShopSkill newSkill, int balance, int ownedCount, int bodySegments, boolean limitReached) {
            skill = newSkill;
            cardImage.setImage(shopSkillImages.get(newSkill));
            boolean enoughSegments = newSkill != ShopSkill.LENGTH_DECREASE || bodySegments >= 5;
            root.setDisable(limitReached || balance < newSkill.cost() || !enoughSegments);
            root.setAccessibleText(newSkill.title() + ", " + newSkill.description()
                    + " Cost: " + newSkill.cost() + ". Owned: " + ownedCount);
            tooltip.setText("Click the card to buy — " + newSkill.title()
                    + " — " + newSkill.cost() + " coins");
        }
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
        snakeCanvas.setWidth(gridCanvas.getWidth());
        snakeCanvas.setHeight(gridCanvas.getHeight());
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
        timeLabel.setStyle("-fx-font-size: " + (32 * textScale) + "px;");
    }

    public void drawGrid(List<Coin> coins, Snake snek, Obstacle obstacle, GraphicsContext gc, boolean a, int b) {
        currentSnake = snek;
        currentCoins = coins;
        currentObstacle = obstacle;
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
        snakeGraphics.clearRect(0, 0, snakeCanvas.getWidth(), snakeCanvas.getHeight());
        snakeGraphics.setImageSmoothing(false);
        List<Point> body = snek.getBody();
        for (int index = 0; index < body.size(); index++) {
            drawBody(snek, body, snek.getHead(), body.get(index), index);
        }
        drawBodySeams(body, snek.getHead());
        drawHead(snek);
    }

    private void drawBodySeams(List<Point> body, Point head) {
        double cellSize = game.getMultiplier() * fullScale;
        snakeGraphics.setStroke(Color.web("#24581d"));
        snakeGraphics.setLineWidth(Math.max(1.5, 2.0 * fullScale));
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
        snakeGraphics.strokeLine(x, centerY - halfSpan, x, centerY + halfSpan);
    }

    private void drawHorizontalSeam(double centerX, double y, double halfSpan) {
        snakeGraphics.strokeLine(centerX - halfSpan, y, centerX + halfSpan, y);
    }

    private void drawGameBackground(GraphicsContext graphics) {
        graphics.setFill(Color.web(GAME_BACKGROUND_COLOR));
        graphics.fillRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());
    }

    private void drawCoins(List<Coin> coins, GraphicsContext gc) {
        gc.setImageSmoothing(false);
        for (Coin coin : coins) {
            Point position = coin.getPos();
            coin.draw(gc, (position.x * game.getMultiplier()) * fullScale,
                    (position.y * game.getMultiplier()) * fullScale,
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
        snakeGraphics.save();
        snakeGraphics.translate(centerX, centerY);
        snakeGraphics.rotate(rotation);
        snakeGraphics.drawImage(sheet,
                sprite.x(), sprite.y(), sprite.width(), sprite.height(),
                -drawSize / 2, -drawSize / 2, drawSize, drawSize);
        snakeGraphics.restore();
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
