/*
 * Copyright 2016 Patrik Karlsson.
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
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import se.trixon.util.dictionary.Dict;

/**
 *
 * @author Patrik Karlsson
 */
public class Config {

    private boolean mConfigLoaded;
    private Configuration mConfiguration;
    private File mDbFile;
    private int mPort;
    private boolean mVerbose;

    public static Config getInstance() {
        return ConfigHolder.INSTANCE;
    }

    private Config() {
    }

    public Configuration getConfiguration() {
        return mConfiguration;
    }

    public File getDbFile() {
        return mDbFile;
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
            System.err.println("No valid configuration file found");
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
                mPort = mConfiguration.getInt("port", 1099);
                mDbFile = new File(mConfiguration.getString("db_file", "idd.db"));
                mConfigLoaded = true;
            } catch (ConfigurationException ex) {
                System.err.println(ex.getMessage());
            }
        } else {
            System.err.format(Dict.FILE_NOT_FOUND_MESSAGE.toString(), file.getAbsolutePath());
            System.err.println("");
        }
    }

    private static class ConfigHolder {

        private static final Config INSTANCE = new Config();
    }
}
