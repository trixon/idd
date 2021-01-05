/* 
 * Copyright 2021 Patrik Karlström.
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
package se.trixon.idr;

import java.awt.GraphicsEnvironment;
import java.util.logging.Logger;
import se.trixon.almond.util.AlmondUI;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.idr.ui.MainFrame;

/**
 *
 * @author Patrik Karlström
 */
public class Idr {

    private static final Logger LOGGER = Logger.getLogger(Idr.class.getName());

    private final AlmondUI mAlmondUI = AlmondUI.getInstance();
    private MainFrame mMainFrame = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new Idr();
    }

    public Idr() {
        displayGui();
    }

    private void displayGui() {
        if (GraphicsEnvironment.isHeadless()) {
            LOGGER.severe(Dict.Dialog.ERROR_NO_GUI_IN_HEADLESS.toString());
            System.exit(1);

            return;
        }

        SystemHelper.setMacApplicationName("IDR");

        mAlmondUI.initLookAndFeel();

        java.awt.EventQueue.invokeLater(() -> {
            mMainFrame = new MainFrame();
            mMainFrame.setVisible(true);
        });
    }
}
