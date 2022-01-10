/* 
 * Copyright 2022 Patrik Karlström.
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

import com.healthmarketscience.sqlbuilder.CreateTableQuery;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbConstraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.trixon.idd.Config;

/**
 *
 * @author Patrik Karlström
 */
public class Db {

    private static final Logger LOGGER = Logger.getLogger(Db.class.getName());
    private static final Config mConfig = Config.getInstance();

    private Connection mAutoCommitConnection = null;
    private final String mConnString = String.format("jdbc:h2:%s%s;DEFRAG_ALWAYS=true", mConfig.getDbMode(), mConfig.getDbFile().getAbsolutePath());
    private Connection mConnection = null;
    private DbSchema mSchema;
    private final DbSpec mSpec;
    private boolean mUpdating;

    public static Db getInstance() {
        return Holder.INSTANCE;
    }

    private Db() {
        mSpec = new DbSpec();
        init();
    }

    public void connectionCommit() throws ClassNotFoundException, SQLException {
        getConnection().commit();
    }

    public void connectionOpen() throws ClassNotFoundException, SQLException {
        if (mConnection != null && !mConnection.isClosed()) {
            connectionCommit();
            //mConnection.close();
        }

        Class.forName("org.h2.Driver");
        mConnection = DriverManager.getConnection(mConnString);
        mConnection.setAutoCommit(false);
        //LOGGER.log(Level.INFO, "JDBC Connect: {0}", mConnString);
    }

    public boolean connectionRollback() {
        try {
            getConnection().rollback();
            LOGGER.fine("JDBC Rollback");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "JDBC Rollback failed: {0}", ex.getMessage());
            return false;
        }

        return true;
    }

    public boolean create(DbTable table, DbConstraint... constraints) {
        boolean tableCreated;

        try (Statement statement = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            if (table.getConstraints().isEmpty()) {
                for (DbConstraint constraint : constraints) {
                    table.addConstraint(constraint);
                }
            }

            String sql = new CreateTableQuery(table, true).validate().toString();
            //System.out.println(sql);

            tableCreated = statement.execute(sql);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Table creation failed. {0}", table.getName());
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            tableCreated = false;
        }

        return tableCreated;
    }

    public void drop(DbTable table, boolean cascade) throws ClassNotFoundException, SQLException {
        try (Statement statement = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            String sql = String.format("DROP TABLE IF EXISTS %s %s;", table.getName(), cascade ? "CASCADE" : "");
            //System.out.println(sql);
            statement.execute(sql);
        }
    }

    public void dropAllObjects() throws ClassNotFoundException, SQLException {
        try (Statement statement = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            String sql = "DROP ALL OBJECTS;";
            System.out.println(sql);
            statement.execute(sql);
        }
    }

    public Connection getAutoCommitConnection() {
        try {
            if (mAutoCommitConnection == null || mAutoCommitConnection.isClosed()) {
                Class.forName("org.h2.Driver");
                mAutoCommitConnection = DriverManager.getConnection(mConnString);
            } else {
                if (!mAutoCommitConnection.isValid(2)) {
                    mAutoCommitConnection = null;
                    LOGGER.severe("Database connection lost");
                }
            }
        } catch (ClassNotFoundException | SQLException ex) {
            LOGGER.severe(ex.getMessage());
        }

        return mAutoCommitConnection;
    }

    public String getConnString() {
        return mConnString;
    }

    public Connection getConnection() {
        return mConnection;
    }

    public DbSpec getSpec() {
        return mSpec;
    }

    public boolean isUpdating() {
        return mUpdating;
    }

    public void setUpdating(boolean updating) {
        mUpdating = updating;
    }

    public String update(String path) {
        String resultMessage = null;

        if (isUpdating()) {
            resultMessage = "ACK Update already in progress";
        } else {
            try {
                setUpdating(true);
                connectionOpen();
                DbCreator.getInstance().initDb();
                EnumSet<FileVisitOption> fileVisitOptions = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
                FileVisitor fileVisitor = new FileVisitor();
                Files.walkFileTree(new File(path).toPath(), fileVisitOptions, Integer.MAX_VALUE, fileVisitor);
                connectionCommit();
                resultMessage = "ACK Update done";
            } catch (ClassNotFoundException | SQLException | IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                resultMessage = "ACK Update failed";
            } finally {
                setUpdating(false);
            }
        }

        return resultMessage;
    }

    private void init() {
        mSpec.addDefaultSchema();
    }

    private static class Holder {

        private static final Db INSTANCE = new Db();
    }
}
