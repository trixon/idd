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
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.PomInfo;
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

    public static final String APP_TITLE = "idf";
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private final AlmondFx mAlmondFX = AlmondFx.getInstance();
    private Client mClient;
    private ImageView mImageView;
    private final Options mOptions = Options.getInstance();
    private Image mPreviousImage;
    private BorderPane mRoot;
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
        mStage.setTitle(APP_TITLE);
        mStage.show();

        connect();
    }

    @Override
    public void stop() throws Exception {
        disconnect();
    }

    private void connect() {
        try {
            mClient.disconnect();
        } catch (Exception e) {
            //nvm
        }

        mClient = new Client();
        mClient.addClientListener(new ClientListener() {
            @Override
            public void onClientConnect() {
                mStage.setTitle(String.format("%s (%s:%d)", APP_TITLE, mOptions.getHost(), mOptions.getPort()));
                LOGGER.info(String.format("onClientConnect (%s:%d)", mOptions.getHost(), mOptions.getPort()));
            }

            @Override
            public void onClientDisconnect() {
                mStage.setTitle(APP_TITLE);
                LOGGER.info("onClientDisconnect");
            }

            @Override
            public void onClientReceive(FrameImageCarrier frameImageCarrier) {
                if (frameImageCarrier.hasValidMd5()) {
                    mPreviousImage = mImageView.getImage();
                    mImageView.setImage(frameImageCarrier.getRotatedImageFx());
                } else {
                    LOGGER.warning("Invalid image checksum");
                }

                System.gc();
            }

            @Override
            public void onClientRegister() {
                LOGGER.info("onClientRegister");
            }
        });

        try {
            mClient.setHost(mOptions.getHost());
            mClient.setPort(mOptions.getPort());
            mClient.connect();
            mClient.register();
            mClient.send("random");
        } catch (SocketException e) {
            displayError("IDD Error", e.getMessage(), e);
            LOGGER.log(Level.SEVERE, null, e);
        } catch (IOException e) {
            displayError("IDD Error", e.getMessage(), e);
            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    // <editor-fold defaultstate="collapsed" desc=" createUI">
    private void createUI() {
        mImageView = new ImageView();
        mImageView.setPickOnBounds(true);
        mImageView.setPreserveRatio(true);
        mImageView.setSmooth(true);
        mImageView.setCache(true);

        mRoot = new BorderPane(mImageView);
        updateBackground();
        mImageView.fitWidthProperty().bind(mRoot.widthProperty());
        mImageView.fitHeightProperty().bind(mRoot.heightProperty());

        Scene scene = new Scene(mRoot);
        createUIContextMenu(scene);
        mStage.setScene(scene);

        mOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            switch (evt.getKey()) {
                case Options.KEY_BACKGROUND:
                    updateBackground();
                    break;

                default:
            }
        });
    }

    private void createUIContextMenu(Scene scene) {
        MenuItem menuItemQuit = new MenuItem(Dict.QUIT.toString());
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

        MenuItem menuItemOptions = new MenuItem(Dict.OPTIONS.toString());
        menuItemOptions.setAccelerator(KeyCombination.keyCombination("o"));
        menuItemOptions.setOnAction((ActionEvent event) -> {
            displayOptions();
        });

        MenuItem menuItemAbout = new MenuItem(Dict.ABOUT.toString());
        menuItemAbout.setOnAction((ActionEvent event) -> {
            displayAbout();
        });

        MenuItem menuItemConnect = new MenuItem(Dict.CONNECT.toString());
        menuItemConnect.setAccelerator(KeyCombination.keyCombination("c"));
        menuItemConnect.setOnAction((ActionEvent event) -> {
            connect();
        });

        MenuItem menuItemDisconnect = new MenuItem(Dict.DISCONNECT.toString());
        menuItemDisconnect.setAccelerator(KeyCombination.keyCombination("d"));
        menuItemDisconnect.setOnAction((ActionEvent event) -> {
            disconnect();
        });

        CheckMenuItem fullScreenCheckMenuItem = new CheckMenuItem(Dict.FULL_SCREEN.toString());
        fullScreenCheckMenuItem.setAccelerator(KeyCombination.keyCombination("F"));
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
                menuItemOptions,
                new SeparatorMenuItem(),
                menuItemAbout,
                new SeparatorMenuItem(),
                menuItemConnect,
                menuItemDisconnect,
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

                    case C:
                        menuItemConnect.getOnAction().handle(null);
                        break;

                    case CONTEXT_MENU:
                        Bounds b = mRoot.localToScreen(mRoot.getBoundsInLocal());
                        contextMenu.show(mStage, b.getMinX(), b.getMinY());
                        break;
                    case D:
                        menuItemDisconnect.getOnAction().handle(null);
                        break;

                    case F:
                        fullScreenCheckMenuItem.getOnAction().handle(null);
                        fullScreenCheckMenuItem.setSelected(mStage.isFullScreen());
                        break;

                    case O:
                        menuItemOptions.getOnAction().handle(null);
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
    }//</editor-fold>

    private void disconnect() {
        try {
            mClient.disconnect();
        } catch (Exception e) {
            displayError("IDD Error", e.getMessage(), e);
            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    private void displayAbout() {
        PomInfo pomInfo = new PomInfo(Main.class, "se.trixon.idd", "idl");

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.initOwner(mStage);
        alert.setTitle(String.format(Dict.ABOUT_S.toString(), APP_TITLE));
        alert.setHeaderText(String.format("%s v%s", APP_TITLE, pomInfo.getVersion()));
        alert.setContentText("A basic implementation of an\n"
                + "image displayer daemon frame.\n\n"
                + "Licensed under the Apache License, Version 2.0\n"
                + "Copyright 2018 Patrik Karlsson");

        FxHelper.showAndWait(alert, mStage);
    }

    private void displayError(String header, String content, Exception ex) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(mStage);
            alert.setTitle(Dict.Dialog.ERROR.toString());
            alert.setHeaderText(header);
            alert.setContentText(content);

            FxHelper.showAndWait(alert, mStage);
        });
    }

    private void displayOptions() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.initOwner(mStage);
        alert.setTitle(Dict.OPTIONS.toString());
        alert.setGraphic(null);
        alert.setHeaderText(null);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 10, 10));

        TextField hostTextField = new TextField(mOptions.getHost());
        hostTextField.setPromptText(Dict.HOST.toString());

        Spinner portSpinner = new Spinner(1024, 65536, mOptions.getPort());
        portSpinner.setEditable(true);

        ColorPicker colorPicker = new ColorPicker(Color.web(mOptions.getBackground()));

        gridPane.add(new Label(Dict.HOST.toString()), 0, 0);
        gridPane.add(hostTextField, 1, 0);
        gridPane.add(new Label(Dict.PORT.toString()), 0, 1);
        gridPane.add(portSpinner, 1, 1);
        gridPane.add(new Label(Dict.BACKGROUND_COLOR.toString()), 0, 2);
        gridPane.add(colorPicker, 1, 2);

        alert.getDialogPane().setContent(gridPane);

        Optional<ButtonType> result = FxHelper.showAndWait(alert, mStage);
        if (result.get() == ButtonType.OK) {
            final String host = hostTextField.getText();
            final int port = (Integer) portSpinner.getValue();
            boolean reconnect = !mOptions.getHost().equalsIgnoreCase(host) || mOptions.getPort() != port;

            mOptions.setHost(host);
            mOptions.setPort(port);
            mOptions.setBackground(colorPicker.getValue());

            if (reconnect) {
                connect();
            }
        }
    }

    private void send(String string) {
        if (mClient.isConnected()) {
            try {
                mClient.send(string);
            } catch (IOException ex) {
                displayError("IDD Error", ex.getMessage(), ex);
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }

    private void updateBackground() {
        mRoot.setStyle(String.format("-fx-background-color: %s", mOptions.getBackground()));
    }
}
