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
package se.trixon.idd;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.almond.util.Dict;
import se.trixon.idl.IddHelper;

/**
 *
 * @author Patrik Karlström
 */
public class Config {

    private static final Logger LOGGER = Logger.getLogger(Config.class.getName());

    private File mCacheDirectory;
    private int mCacheHeight;
    private int mCacheWidth;
    private boolean mConfigLoaded;
    private Configuration mConfiguration;
    private File mDbFile;
    private String mDbMode;
    private File mImageDirectory;
    private int mImageDirectoryLevel;
    private String[] mImageFormats;
    private int mPort;
    private boolean mVerbose;

    public static Config getInstance() {
        return Holder.INSTANCE;
    }

    private Config() {
    }

    public File getCacheDirectory() {
        return mCacheDirectory;
    }

    public int getCacheHeight() {
        return mCacheHeight;
    }

    public int getCacheWidth() {
        return mCacheWidth;
    }

    public Configuration getConfiguration() {
        return mConfiguration;
    }

    public File getDbFile() {
        return mDbFile;
    }

    public String getDbMode() {
        return mDbMode;
    }

    public File getImageDirectory() {
        return mImageDirectory;
    }

    public int getImageDirectoryLevel() {
        return mImageDirectoryLevel;
    }

    public String[] getImageFormats() {
        return mImageFormats;
    }

    public int getPort() {
        return mPort;
    }

    public boolean isVerbose() {
        return mVerbose;
    }

    public boolean load(String filename) {
        File configFile = null;
        File configUser = new File(System.getProperty("user.home"), ".config/idd.conf");
        File configSys = new File("/etc/idd.conf");

        if (filename != null) {
            configFile = new File(filename);
        } else if (configUser.isFile()) {
            configFile = configUser;
        } else if (configSys.isFile()) {
            configFile = configSys;
        }

        if (configFile != null) {
            readConfiguration(configFile);
        } else {
            LOGGER.severe("No valid configuration file found");
        }

        return mConfigLoaded;
    }

    public void setVerbose(boolean verbose) {
        mVerbose = verbose;
    }

    private void readConfiguration(File file) {
        if (file.isFile()) {
            try {
                Configurations configurations = new Configurations();
                mConfiguration = configurations.properties(file);
                mPort = mConfiguration.getInt("port", IddHelper.DEFAULT_PORT);
                mCacheWidth = mConfiguration.getInt("cache_width", 2048);
                mCacheHeight = mConfiguration.getInt("cache_height", 2048);
                mDbFile = new File(mConfiguration.getString("db_file", "idd.db"));
                mDbMode = mConfiguration.getString("db_mode", "");
                mImageDirectory = new File(mConfiguration.getString("image_directory", SystemUtils.USER_HOME));
                mImageDirectoryLevel = mImageDirectory.toPath().getNameCount();
                mImageFormats = StringUtils.split(mConfiguration.getString("image_format", "jpeg").toLowerCase(), " ");
                String cachePath = mConfiguration.getString("cache_directory");
                if (cachePath != null) {
                    mCacheDirectory = new File(cachePath);
                }
                LOGGER.log(Level.INFO, "Loaded configuration from {0}", file.getAbsolutePath());

                mConfigLoaded = true;
            } catch (ConfigurationException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        } else {
            LOGGER.severe(String.format(Dict.Dialog.MESSAGE_FILE_NOT_FOUND.toString(), file.getAbsolutePath()));
        }
    }

    private static class Holder {

        private static final Config INSTANCE = new Config();
    }
}
