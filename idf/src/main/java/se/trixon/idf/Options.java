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

/**
 *
 * @author Patrik Karlsson
 */
public class Options {

    public static final String KEY_BACKGROUND_B = "background_b";
    public static final String KEY_BACKGROUND_G = "background_g";
    public static final String KEY_BACKGROUND_O = "background_o";
    public static final String KEY_BACKGROUND_R = "background_r";
    public static final String KEY_HOST = "host";
    public static final String KEY_PORT = "port";
    private static final String DEFAULT_HOST = "localhost";
    private final Color DEFAULT_BACKGROUND = Color.BROWN;
    private final int DEFAULT_PORT = 5705;
    private final Preferences mPreferences = Preferences.userNodeForPackage(Options.class);

    public static Options getInstance() {
        return Holder.INSTANCE;
    }

    private Options() {
    }

    public Color getBackground() {
        double r = mPreferences.getDouble(KEY_BACKGROUND_R, DEFAULT_BACKGROUND.getRed());
        double g = mPreferences.getDouble(KEY_BACKGROUND_G, DEFAULT_BACKGROUND.getGreen());
        double b = mPreferences.getDouble(KEY_BACKGROUND_B, DEFAULT_BACKGROUND.getBlue());
        double o = mPreferences.getDouble(KEY_BACKGROUND_O, DEFAULT_BACKGROUND.getOpacity());

        return new Color(r, g, b, o);
    }

    public String getHost() {
        return mPreferences.get(KEY_HOST, DEFAULT_HOST);
    }

    public int getPort() {
        return mPreferences.getInt(KEY_PORT, DEFAULT_PORT);
    }

    public void setBackground(Color c) {
        mPreferences.putDouble(KEY_BACKGROUND_R, c.getRed());
        mPreferences.putDouble(KEY_BACKGROUND_G, c.getGreen());
        mPreferences.putDouble(KEY_BACKGROUND_B, c.getBlue());
        mPreferences.putDouble(KEY_BACKGROUND_O, c.getOpacity());
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
