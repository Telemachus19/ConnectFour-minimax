package com.connectfourgui;

import com.almasb.fxgl.animation.Animation;
import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.core.util.EmptyRunnable;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.ui.FontType;
import javafx.beans.binding.Bindings;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Arrays;

/**
 * Controls the main menu of the game
 */
public class ConnectFourMainMenu extends GameApplication {
    @Override
    protected void initSettings(GameSettings settings) {
        settings.setMainMenuEnabled(true);
        // Creates a new Scene factory and gets the custom main menu.
        settings.setSceneFactory(new SceneFactory() {
            @Override
            public FXGLMenu newMainMenu() {
                return new MyMainMenu();
            }
        });
    }

    public static class MyMainMenu extends FXGLMenu {
        /**
         * The size of the components
         */
        private static final int SIZE = 150;

        private Animation<?> animation;

        public MyMainMenu() {
            super(MenuType.MAIN_MENU);
            String noAlphaBetaMessage = "Mini Max without alpha beta pruning - Depth ";
            String alphaBetaMessage = "Mini Max with alpha beta pruning - Depth ";
            // Centers the components in the window
            getContentRoot().setTranslateX(FXGL.getAppWidth() / 2.0 - SIZE);
            getContentRoot().setTranslateY(FXGL.getAppHeight() / 2.0 - SIZE);
            Rectangle[] miniMaxBtn = new Rectangle[6];
            for (int i = 0; i < miniMaxBtn.length; i++) {
                if (i == 0) {
                    miniMaxBtn[i] = new Rectangle(0, 0, SIZE * 3, SIZE - 100);
                } else {
                    miniMaxBtn[i] = new Rectangle(0, miniMaxBtn[i - 1].getY() + 50, SIZE * 3, SIZE - 100);
                }
                miniMaxBtn[i].setStrokeWidth(2.5);
                miniMaxBtn[i].strokeProperty().bind(
                        Bindings.when(miniMaxBtn[i].hoverProperty()).then(Color.YELLOW).otherwise(Color.BLACK)
                );
                miniMaxBtn[i].fillProperty().bind(
                        Bindings.when(miniMaxBtn[i].pressedProperty()).then(Color.YELLOW).otherwise(Color.color(0.1, 0.05, 0.0, 0.75))
                );
                // sets the action taken when the button is pressed
                int finalI = i;
                miniMaxBtn[i].setOnMouseClicked(e -> {
                    ConnectFourGUI.alphaBeta = finalI > 2;
                    ConnectFourGUI.depth = 5 + finalI % 3;
                    fireNewGame();
                });
            }

            Text[] btnText = new Text[6];
            for (int i = 0; i < btnText.length; i++) {
                if (i < 3) {
                    btnText[i] = FXGL.getUIFactoryService().newText(noAlphaBetaMessage + (5 + i % 3), Color.WHITE, FontType.GAME, 24.0);
                } else {
                    btnText[i] = FXGL.getUIFactoryService().newText(alphaBetaMessage + (5 + i % 3), Color.WHITE, FontType.GAME, 24.0);
                }
                btnText[i].setTranslateY(miniMaxBtn[i].getY() + miniMaxBtn[i].getHeight() / 2);
                btnText[i].setTranslateX(miniMaxBtn[i].getX());
                btnText[i].setMouseTransparent(true);
            }

            // The exit button in the shape of rectangle
            var exitBtn = new Rectangle(0, miniMaxBtn[5].getY() + 50, SIZE, 50);
            exitBtn.setStrokeWidth(2.5);
            exitBtn.strokeProperty().bind(
                    Bindings.when(exitBtn.hoverProperty()).then(Color.YELLOW).otherwise(Color.BLACK)
            );
            exitBtn.fillProperty().bind(
                    Bindings.when(exitBtn.pressedProperty()).then(Color.YELLOW).otherwise(Color.color(0.1, 0.05, 0.0, 0.75))
            );
            exitBtn.setOnMouseClicked(e -> fireExit());

            Text textExit = FXGL.getUIFactoryService().newText("EXIT", Color.WHITE, FontType.GAME, 24.0);
            textExit.setTranslateX(exitBtn.getX());
            textExit.setTranslateY(exitBtn.getY() + exitBtn.getHeight() / 2);
            textExit.setMouseTransparent(true);
            var children = getContentRoot().getChildren();
            children.addAll(Arrays.asList(miniMaxBtn));
            children.addAll(Arrays.asList(btnText));
            getContentRoot().getChildren().addAll(exitBtn, textExit);

            getContentRoot().setScaleX(0);
            getContentRoot().setScaleY(0);

            animation = FXGL.animationBuilder()
                    .duration(Duration.seconds(0.66))
                    .interpolator(Interpolators.EXPONENTIAL.EASE_OUT())
                    .scale(getContentRoot())
                    .from(new Point2D(0, 0))
                    .to(new Point2D(1, 1))
                    .build();
        }

        @Override
        public void onCreate() {
            animation.setOnFinished(EmptyRunnable.INSTANCE);
            animation.stop();
            animation.start();
        }

        @Override
        protected void onUpdate(double tpf) {
            animation.onUpdate(tpf);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}