package com.comp2042;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.geometry.Rectangle2D;
import javafx.stage.Stage;
import javafx.stage.Screen;

import java.io.IOException;

public class ViewManager {

    private final Stage primaryStage;
    private Parent root;
    private Parent mainMenuRoot;
    private Scene mainMenuScene;
    private boolean fullscreenMode = true;

    private Image snakeLogo = new Image("resources/views/res/SnakeLogoWhite.png");

    public ViewManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void initalizeMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/resources/views/MainMenu.fxml"));
            root = loader.load();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        mainMenuRoot = root;
        mainMenuScene = new Scene(mainMenuRoot);
        primaryStage.setScene(mainMenuScene);
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(snakeLogo);
        primaryStage.setTitle("Snake Game");
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        primaryStage.setFullScreen(fullscreenMode);
        primaryStage.show();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setWindowedStageSize() {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        if (primaryStage.isFullScreen()) {
            primaryStage.setFullScreen(false);
        }
        primaryStage.setWidth(bounds.getWidth() * GameDimensions.WINDOWED_SCREEN_FRACTION);
        primaryStage.setHeight(bounds.getHeight() * GameDimensions.WINDOWED_SCREEN_FRACTION);
        primaryStage.centerOnScreen();
    }

    public void applyDisplayMode(boolean fullScreen) {
        fullscreenMode = fullScreen;
        if (primaryStage.isFullScreen() != fullScreen) {
            primaryStage.setFullScreen(fullScreen);
        }
        if (!fullScreen) {
            setWindowedStageSize();
        }
    }

    public boolean isFullscreenMode() {
        return fullscreenMode;
    }

    public Scene getMainScene() {
        return mainMenuScene;
    }

    public void showGameView(Parent gameRoot) {
        mainMenuScene.setRoot(gameRoot);
        applyDisplayMode(fullscreenMode);
        if (!primaryStage.isShowing()) {
            primaryStage.show();
        }
    }

    public void showMainMenu() {
        if (mainMenuScene != null) {
            mainMenuScene.setRoot(mainMenuRoot);
            mainMenuScene.setOnKeyPressed(null);
            mainMenuScene.setOnKeyReleased(null);
            if (primaryStage.getScene() != mainMenuScene) {
                primaryStage.setScene(mainMenuScene);
            }
            applyDisplayMode(fullscreenMode);
        }
        if (!primaryStage.isShowing()) {
            primaryStage.show();
        }
    }
}
