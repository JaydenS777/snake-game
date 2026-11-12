package com.comp2042;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MainMenuController {

    private static final boolean POWER_UP = true;
    private static final char DIFFICULTY = 'N';
    private static final String WINDOWED_MODE = "Windowed";
    private static final String FULLSCREEN_MODE = "Fullscreen";

    private Scene gameScene;
    @FXML
    private ComboBox<String> displayModeChoice;
    @FXML
    private VBox mainMenuPanel;
    @FXML
    private VBox optionsPanel;
    @FXML
    private Label displayModeStatus;

    private boolean appliedFullscreen = true;

    private Snake snek;
    private Obstacle obstacle;
    private List<Coin> coins;
    private GameView gameView;

    private ViewManager viewManager = Main.getViewManager();

    @FXML
    private void initialize() {
        displayModeChoice.getItems().setAll(WINDOWED_MODE, FULLSCREEN_MODE);
        appliedFullscreen = viewManager.isFullscreenMode();
        displayModeChoice.setValue(appliedFullscreen ? FULLSCREEN_MODE : WINDOWED_MODE);
        updateMenuScale();
        updateDisplayModeStatus();
    }

    @FXML
    private void handleOpenOptions() {
        appliedFullscreen = viewManager.isFullscreenMode();
        updateMenuScale();
        updateDisplayModeStatus();
        mainMenuPanel.setVisible(false);
        mainMenuPanel.setManaged(false);
        optionsPanel.setVisible(true);
        optionsPanel.setManaged(true);
        displayModeChoice.setValue(appliedFullscreen ? FULLSCREEN_MODE : WINDOWED_MODE);
    }

    @FXML
    private void handleBackToMenu() {
        showMainMenu();
    }

    @FXML
    private void handleApplyOptions() {
        appliedFullscreen = FULLSCREEN_MODE.equals(displayModeChoice.getValue());
        viewManager.applyDisplayMode(appliedFullscreen);
        updateMenuScale();
        updateDisplayModeStatus();
    }

    private void updateMenuScale() {
        Rectangle2D screen = appliedFullscreen
                ? Screen.getPrimary().getBounds()
                : Screen.getPrimary().getVisualBounds();
        double viewportFraction = appliedFullscreen ? 1.0 : GameDimensions.WINDOWED_SCREEN_FRACTION;
        double scale = DisplayScale.mainMenuScale(
                screen.getWidth() * viewportFraction,
                screen.getHeight() * viewportFraction);
        mainMenuPanel.setScaleX(scale);
        mainMenuPanel.setScaleY(scale);
        optionsPanel.setScaleX(scale);
        optionsPanel.setScaleY(scale);
    }

    private void updateDisplayModeStatus() {
        if (displayModeStatus != null) {
            displayModeStatus.setText(appliedFullscreen
                    ? "Applied: Fullscreen"
                    : "Applied: Windowed (large screen-scaled window)");
        }
    }

    private void showMainMenu() {
        optionsPanel.setVisible(false);
        optionsPanel.setManaged(false);
        mainMenuPanel.setVisible(true);
        mainMenuPanel.setManaged(true);
    }

    @FXML
    private void handleExitGame() {
        Platform.exit();
    }

    public void handleStart() {
        Stage gameStage = viewManager.getPrimaryStage();
        appliedFullscreen = viewManager.isFullscreenMode();
        showMainMenu();
        launchSnake(gameStage);
        gameView.scaleToFullscreen(appliedFullscreen,
                gameStage, obstacle, snek, coins);
    }

    private void launchSnake(Stage gameStage) {
        Rectangle2D bounds = viewManager.isFullscreenMode()
                ? Screen.getPrimary().getBounds()
                : Screen.getPrimary().getVisualBounds();
        GameDimensions.Grid grid = viewManager.isFullscreenMode()
                ? GameDimensions.forFullscreen(bounds.getWidth(), bounds.getHeight())
                : GameDimensions.forWindowed(bounds.getWidth(), bounds.getHeight());
        Game game = new Game(grid.columns(), grid.rows(), POWER_UP, DIFFICULTY, grid.cellSize());
        obstacle = new Obstacle(game.getWidth(), game.getHeight());
        snek = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        coins = new ArrayList<>();
        coins.add(new Coin(game.getWidth(), game.getHeight(), snek, obstacle));

        gameView = new GameView(game);
        GameController controller = new GameController(snek, coins, game, gameView, obstacle, gameStage);

        StackPane gamePane = new StackPane();
        gamePane.setStyle("-fx-background-color: #feffe4;");
        gameScene = viewManager.getMainScene();

        gameScene.setOnKeyPressed(controller::handle);
        gameScene.setOnKeyReleased(controller::handleKeyReleased);

        gameView.makeScene(snek, coins);
        gamePane.getChildren().addAll(gameView.gridCanvas, gameView.gameOverLabel, gameView.gameWonLabel,
                gameView.timeLabel, gameView.scoreLabel, gameView.restartButton, gameView.pauseOverlay,
                gameView.resumeCountdownOverlay, gameView.gameOverOverlay, gameView.shopOverlay);
        gamePane.setAlignment(gameView.gameOverLabel, Pos.TOP_CENTER);
        gamePane.setAlignment(gameView.gameWonLabel, Pos.TOP_CENTER);
        gamePane.setAlignment(gameView.timeLabel, Pos.TOP_RIGHT);
        gamePane.setAlignment(gameView.scoreLabel, Pos.BOTTOM_CENTER);
        gamePane.setAlignment(gameView.restartButton, Pos.CENTER);
        gameView.restartButton.setOnAction(event -> controller.restart());
        gameView.resumeButton.setOnAction(event -> controller.resumeGame());
        gameView.pauseRestartButton.setOnAction(event -> controller.restart());
        gameView.exitToMenuButton.setOnAction(event -> controller.exitToMenu());
        gameView.gameOverRestartButton.setOnAction(event -> controller.restart());
        gameView.gameOverExitButton.setOnAction(event -> controller.exitToMenu());
        gameView.shopContinueButton.setOnAction(event -> controller.continueFromShop());
        viewManager.showGameView(gamePane);
    }

    public ViewManager getViewManager() {
        return viewManager;
    }

    public static int getxValue() {
        return GameDimensions.forFullscreen(Screen.getPrimary().getBounds().getWidth(),
                Screen.getPrimary().getBounds().getHeight()).columns();
    }

    public static int getyValue() {
        return GameDimensions.forFullscreen(Screen.getPrimary().getBounds().getWidth(),
                Screen.getPrimary().getBounds().getHeight()).rows();
    }
}
