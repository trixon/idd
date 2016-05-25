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
package se.trixon.idd.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import se.trixon.idd.Config;

/**
 *
 * @author Patrik Karlsson
 */
public class Db {

    private Connection mAutoCommitConnection = null;

    private Db() {
    }

    public static Db getInstance() {
        return Holder.INSTANCE;
    }

    public Connection getAutoCommitConnection() throws ClassNotFoundException, SQLException {
        if (mAutoCommitConnection == null) {
            Class.forName("org.h2.Driver");
            System.out.println(Config.getInstance().getDbFile().getAbsolutePath());
            mAutoCommitConnection = DriverManager.getConnection(String.format("jdbc:h2:%s", Config.getInstance().getDbFile().getAbsolutePath()));
        }

        return mAutoCommitConnection;
    }

    private static class Holder {

        private static final Db INSTANCE = new Db();
    }
}
