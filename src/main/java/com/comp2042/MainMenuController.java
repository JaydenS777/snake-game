package com.comp2042;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MainMenuController {

    private static final int X_VALUE = 20;
    private static final int Y_VALUE = 20;
    private static final boolean POWER_UP = true;
    private static final char DIFFICULTY = 'N';

    private Scene gameScene;

    private ViewManager viewManager = Main.getViewManager();

    public void handleStart() {
        viewManager.initGameStage();

        Stage gameStage = viewManager.getGameStage();
        launchSnake(gameStage);
        gameStage.setScene(gameScene);
        viewManager.getPrimaryStage().close();
        gameStage.show();
    }

    private void launchSnake(Stage gameStage) {
        Game game = new Game(X_VALUE, Y_VALUE, POWER_UP, DIFFICULTY);
        Obstacle obstacle = new Obstacle(game.getWidth(), game.getHeight());
        Snake snek = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        List<Coin> coins = new ArrayList<>();
        coins.add(new Coin(game.getWidth(), game.getHeight(), snek, obstacle));

        GameView gameView = new GameView(game);
        GameController controller = new GameController(snek, coins, game, gameView, obstacle, gameStage);

        StackPane gamePane = new StackPane();
        gameScene = new Scene(gamePane, game.getWidth() * game.getMultiplier(), game.getHeight() * game.getMultiplier());

        gameScene.setOnKeyPressed(controller::handle);
        gameScene.setOnKeyReleased(controller::handleKeyReleased);

        gameView.makeScene(gameScene, snek, coins);
        gamePane.getChildren().addAll(gameView.gridCanvas, gameView.gameOverLabel, gameView.gameWonLabel,
                gameView.timeLabel, gameView.scoreLabel, gameView.restartButton, gameView.pauseOverlay,
                gameView.resumeCountdownOverlay, gameView.gameOverOverlay, gameView.shopOverlay);
        gamePane.setPadding(new Insets(20));
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
    }

    public ViewManager getViewManager() {
        return viewManager;
    }

    public static int getxValue() {
        return X_VALUE;
    }

    public static int getyValue() {
        return Y_VALUE;
    }
}
