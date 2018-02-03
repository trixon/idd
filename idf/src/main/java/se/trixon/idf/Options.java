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

import java.util.prefs.Preferences;
import javafx.scene.paint.Color;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlsson
 */
public class Options {

    public static final String KEY_BACKGROUND = "background";
    public static final String KEY_HOST = "host";
    public static final String KEY_PORT = "port";
    private static final String DEFAULT_HOST = "localhost";
    private final Color DEFAULT_BACKGROUND = Color.BLACK.brighter();
    private final int DEFAULT_PORT = 5705;
    private final Preferences mPreferences = Preferences.userNodeForPackage(Options.class);

    public static Options getInstance() {
        return Holder.INSTANCE;
    }

    private Options() {
    }

    public String getBackground() {
        return mPreferences.get(KEY_BACKGROUND, FxHelper.colorToString(DEFAULT_BACKGROUND));
    }

    public String getHost() {
        return mPreferences.get(KEY_HOST, DEFAULT_HOST);
    }

    public int getPort() {
        return mPreferences.getInt(KEY_PORT, DEFAULT_PORT);
    }

    public void setBackground(Color c) {
        mPreferences.put(KEY_BACKGROUND, FxHelper.colorToString(c));
    }

    public void setHost(String value) {
        mPreferences.put(KEY_HOST, value);
    }

    public void setPort(int value) {
        mPreferences.putInt(KEY_PORT, value);
    }

    private static class Holder {

        private static final Options INSTANCE = new Options();
    }
}
