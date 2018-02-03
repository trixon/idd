/*
 * Copyright 2018 Patrik Karlsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.trixon.idf;

import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.AlmondFx;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.idl.FrameImageCarrier;
import se.trixon.idl.client.Client;
import se.trixon.idl.client.ClientListener;

/**
 *
 * @author Patrik Karlsson
 */
public class Main extends Application {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private final AlmondFx mAlmondFX = AlmondFx.getInstance();
    private Client mClient;
    private ImageView mImageView;
    private final Options mOptions = Options.getInstance();
    private Image mPreviousImage;
    private Stage mStage;

    /**
     * The main() method is ignored in correctly deployed JavaFX application. main() serves only as
     * fallback in case the application can not be launched through deployment artifacts, e.g., in
     * IDEs with limited FX support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        mStage = stage;
        mAlmondFX.addStageWatcher(stage, Main.class);

        createUI();
        stage.setTitle("IDD Frame");
        stage.show();

        postInit();
    }

    @Override
    public void stop() throws Exception {
        mClient.disconnect();
    }

    private void createContextMenu(Scene scene) {
        MenuItem menuItemQuit = new MenuItem(Dict.QUIT.toString());
        //menuItemQuit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
        menuItemQuit.setAccelerator(KeyCombination.keyCombination("q"));
        menuItemQuit.setOnAction((ActionEvent event) -> {
            mStage.fireEvent(new WindowEvent(mStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        MenuItem menuItemNavRandom = new MenuItem(Dict.RANDOM.toString());
        menuItemNavRandom.setAccelerator(KeyCombination.keyCombination("r"));
        menuItemNavRandom.setOnAction((ActionEvent event) -> {
            send("random");
        });

        MenuItem menuItemNavPrev = new MenuItem(Dict.PREVIOUS.toString());
        menuItemNavPrev.setAccelerator(KeyCombination.keyCombination("p"));
        menuItemNavPrev.setOnAction((ActionEvent event) -> {
            Image image = mImageView.getImage();
            mImageView.setImage(mPreviousImage);
            mPreviousImage = image;
        });

        MenuItem menuItemAbout = new MenuItem(Dict.ABOUT.toString());
        menuItemAbout.setOnAction((ActionEvent event) -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(String.format(Dict.ABOUT_S.toString(), mStage.getTitle()));
            alert.setHeaderText(String.format("%s %s", mStage.getTitle(), "v0.0.3"));
            alert.setContentText("A basic implementation of an\n"
                    + "image displayer daemon frame.\n\n"
                    + "Licensed under the Apache License, Version 2.0\n"
                    + "Copyright 2018 Patrik Karlsson");

            FxHelper.showAndWait(alert, mStage);
        });

        CheckMenuItem fullScreenCheckMenuItem = new CheckMenuItem(Dict.FULL_SCREEN.toString());
        fullScreenCheckMenuItem.setAccelerator(KeyCombination.keyCombination("F"));
//        fullScreenCheckMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F11, KeyCombination.ALT_ANY));
        fullScreenCheckMenuItem.setSelected(FxHelper.isFullScreen(Main.class));
        fullScreenCheckMenuItem.setOnAction((ActionEvent event) -> {
            mStage.setFullScreen(!mStage.isFullScreen());
        });

        CheckMenuItem alwaysOnTopCheckMenuItem = new CheckMenuItem(Dict.ALWAYS_ON_TOP.toString());
        alwaysOnTopCheckMenuItem.setAccelerator(KeyCombination.keyCombination("a"));
        alwaysOnTopCheckMenuItem.setSelected(FxHelper.isAlwaysOnTop(Main.class));
        alwaysOnTopCheckMenuItem.setOnAction((ActionEvent event) -> {
            mStage.setAlwaysOnTop(!mStage.isAlwaysOnTop());
        });

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(
                menuItemNavRandom,
                menuItemNavPrev,
                new SeparatorMenuItem(),
                alwaysOnTopCheckMenuItem,
                fullScreenCheckMenuItem,
                new SeparatorMenuItem(),
                menuItemAbout,
                new SeparatorMenuItem(),
                menuItemQuit
        );

        scene.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                fullScreenCheckMenuItem.getOnAction().handle(null);
                fullScreenCheckMenuItem.setSelected(mStage.isFullScreen());
            }
        });

        scene.setOnMousePressed((MouseEvent event) -> {
            if (event.isSecondaryButtonDown()) {
                contextMenu.show(mImageView, event.getScreenX(), event.getScreenY());
            } else if (event.isPrimaryButtonDown()) {
                contextMenu.hide();
            }
        });

        scene.addEventFilter(KeyEvent.KEY_PRESSED, (evt) -> {
            contextMenu.hide();
            if (null != evt.getCode()) {
                switch (evt.getCode()) {
                    case A:
                        alwaysOnTopCheckMenuItem.getOnAction().handle(null);
                        alwaysOnTopCheckMenuItem.setSelected(mStage.isAlwaysOnTop());
                        break;

                    case F:
                        fullScreenCheckMenuItem.getOnAction().handle(null);
                        fullScreenCheckMenuItem.setSelected(mStage.isFullScreen());
                        break;

                    case P:
                        menuItemNavPrev.getOnAction().handle(null);

                        break;
                    case Q:
                        menuItemQuit.getOnAction().handle(null);
                        break;

                    case SPACE:
                    case R:
                        menuItemNavRandom.getOnAction().handle(null);
                        break;

                    default:
                        break;
                }
            }
        });
    }

    private void createUI() {
        mImageView = new ImageView();
        mImageView.setPickOnBounds(true);
        mImageView.setPreserveRatio(true);
        mImageView.setSmooth(true);
        mImageView.setCache(true);

        BorderPane root = new BorderPane(mImageView);
        root.setStyle(String.format("-fx-background-color: %s", mOptions.getBackground()));

        mImageView.fitWidthProperty().bind(root.widthProperty());
        mImageView.fitHeightProperty().bind(root.heightProperty());

        Scene scene = new Scene(root);
        createContextMenu(scene);
        mStage.setScene(scene);
    }

    private void postInit() {
        try {
            mClient = new Client(mOptions.getHost(), mOptions.getPort());
            mClient.addClientListener(new ClientListener() {
                @Override
                public void onClientConnect() {
                    LOGGER.info("onClientConnect");
                }

                @Override
                public void onClientDisconnect() {
                    LOGGER.info("onClientDisconnect");
                }

                @Override
                public void onClientReceive(FrameImageCarrier frameImageCarrier) {
                    frameImageCarrier.hasValidMd5();
                    mPreviousImage = mImageView.getImage();
                    mImageView.setImage(frameImageCarrier.getRotatedImageFx());
                }

                @Override
                public void onClientRegister() {
                    LOGGER.info("onClientRegister");
                }
            });

            mClient.connect();
            mClient.register();
            mClient.send("random");
        } catch (SocketException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void send(String string) {
        try {
            mClient.send(string);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}
