/*
 * Copyright 2017 Patrik Karlsson.
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

import java.io.InputStream;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import se.trixon.almond.util.Xlog;
import se.trixon.idl.client.Client;
import se.trixon.idl.client.ConnectionListener;
import se.trixon.idl.shared.IddHelper;
import se.trixon.idl.shared.ImageServerEvent;
import se.trixon.idl.shared.ImageServerEventRelay;
import se.trixon.idl.shared.ProcessEvent;

public class MainApp extends Application implements ImageServerEventRelay, ConnectionListener {

    private FXMLController controller;
    private Client mClient;

    @Override
    public void onConnectionConnect() {
        Xlog.timedOut("onConnectionConnect");
    }

    @Override
    public void onConnectionDisconnect() {
        Xlog.timedOut("onConnectionDisconnect");
    }

    @Override
    public void onExecutorEvent(String command, String... strings) {
    }

    @Override
    public void onReceiveStreamEvent(InputStream inputStream) {
        Image image = new Image(inputStream);
//        controller.getImage().
        controller.loadImage(image);
    }

    @Override
    public void onProcessEvent(ProcessEvent processEvent, Object object) {
    }

    @Override
    public void onServerEvent(ImageServerEvent imageServerEvent) {
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Scene.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");

        stage.setTitle("IDD Frame");
        stage.setScene(scene);
        stage.setAlwaysOnTop(true);
        stage.show();
        controller.init(stage);

//        try {
        mClient = new Client();
        mClient.addConnectionListener(this);
        mClient.connect();
//        mManager.addImageServerEventRelay(this);
//        } catch (java.rmi.ConnectException e) {
//            System.err.println(e.getMessage());
//        }
    }

    @Override
    public void stop() throws Exception {
        mClient.disconnect();
        IddHelper.exit();
    }

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
}
