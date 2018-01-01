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

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import se.trixon.almond.util.Dict;

public class FXMLController implements Initializable {

    @FXML
    private BorderPane borderPane;

    @FXML
    private CheckMenuItem checkMenuItemFullScreen;
    @FXML
    private CheckMenuItem checkMenuItemMenubar;
    @FXML
    private ImageView imageView;

    private Stage mStage;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Menu menuFile;
    @FXML
    private Menu menuHelp;
    @FXML
    private MenuItem menuItemAbout;
    @FXML
    private MenuItem menuItemQuit;
    @FXML
    private Menu menuView;
    @FXML
    private VBox vbox;

    public FXMLController() {
    }

    public void init(Stage stage) {
        mStage = stage;
        //mStage.setFullScreenExitKeyCombination(KeyCombination.keyCombination("F11"));

        menuFile.setText(Dict.FILE_MENU.toString());
        menuItemQuit.setText(Dict.QUIT.toString());

        menuView.setText(Dict.VIEW.toString());
        checkMenuItemFullScreen.setText(Dict.FULL_SCREEN.toString());
        checkMenuItemMenubar.setText(Dict.MENU.toString());

        menuHelp.setText(Dict.HELP.toString());
        menuItemAbout.setText(Dict.ABOUT.toString());

        Image image = new Image(getClass().getResourceAsStream("midsummer.jpg"));
        imageView.setImage(image);
        imageView.fitWidthProperty().bind(borderPane.widthProperty());
        imageView.fitHeightProperty().bind(borderPane.heightProperty());

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    public void loadImage(Image image) {
        imageView.setImage(image);
    }

    public Image getImage() {
        return imageView.getImage();
    }

    @FXML
    private void onActionAbout(ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(String.format(Dict.ABOUT_S.toString(), mStage.getTitle()));
        alert.setHeaderText(String.format("%s %s", mStage.getTitle(), "v0.0.1"));
        alert.setContentText("A basic implementation of an\n"
                + "image displayer daemon frame.\n\n"
                + "Licensed under the Apache License, Version 2.0\n"
                + "Copyright 2017 Patrik Karlsson");

        alert.showAndWait();
    }

    @FXML
    private void onActionFullscreen(ActionEvent event) {
        mStage.setFullScreen(!mStage.isFullScreen());
    }

    @FXML
    private void onActionMenubar(ActionEvent event) {
        menuBar.setVisible(!menuBar.isVisible());
    }

    @FXML
    private void onActionQuit(ActionEvent event) {
        mStage.fireEvent(new WindowEvent(mStage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
