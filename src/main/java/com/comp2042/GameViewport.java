package com.comp2042;

import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;

/** Fits a fixed game layout inside the current window without relaying out the board or HUD. */
final class GameViewport extends Pane {
    private final Region gameLayout;
    private final Group scaledLayout;
    private final Scale scale = new Scale();
    private final double fullscreenWidth;
    private final double fullscreenHeight;

    GameViewport(Region gameLayout, double fullscreenWidth, double fullscreenHeight) {
        this.gameLayout = gameLayout;
        this.fullscreenWidth = fullscreenWidth;
        this.fullscreenHeight = fullscreenHeight;
        setMinSize(0, 0);
        setPrefSize(fullscreenWidth, fullscreenHeight);
        setStyle("-fx-background-color: " + GameView.GAME_BACKGROUND_COLOR + ";");

        scaledLayout = new Group(gameLayout);
        scaledLayout.setAutoSizeChildren(false);
        scaledLayout.setManaged(false);
        scaledLayout.getTransforms().add(scale);
        getChildren().add(scaledLayout);
    }

    @Override
    protected void layoutChildren() {
        gameLayout.resize(fullscreenWidth, fullscreenHeight);
        double factor = Math.min(getWidth() / fullscreenWidth, getHeight() / fullscreenHeight);
        scale.setX(factor);
        scale.setY(factor);
        scaledLayout.setLayoutX((getWidth() - fullscreenWidth * factor) / 2);
        scaledLayout.setLayoutY((getHeight() - fullscreenHeight * factor) / 2);
    }
}
