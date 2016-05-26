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

import com.healthmarketscience.sqlbuilder.CreateTableQuery;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbConstraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.trixon.idd.Config;

/**
 *
 * @author Patrik Karlsson
 */
public class Db {

    private Connection mAutoCommitConnection = null;
    private final DbSpec mSpec;

    private Db() {
        mSpec = new DbSpec();
        init();
    }

    public static Db getInstance() {
        return Holder.INSTANCE;
    }

    public DbSpec getSpec() {
        return mSpec;
    }

    public Connection getAutoCommitConnection() throws ClassNotFoundException, SQLException {
        if (mAutoCommitConnection == null) {
            Class.forName("org.h2.Driver");
            mAutoCommitConnection = DriverManager.getConnection(String.format("jdbc:h2:%s", Config.getInstance().getDbFile().getAbsolutePath()));
        }

        return mAutoCommitConnection;
    }

    public void drop(DbTable table, boolean cascade) throws ClassNotFoundException, SQLException {
        try (Statement statement = getAutoCommitConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            String sql = String.format("DROP TABLE IF EXISTS %s %s;", table.getName(), cascade ? "CASCADE" : "");
            System.out.println(sql);
            statement.execute(sql);
        }
    }

    public boolean create(DbTable table, DbConstraint... constraints) {
        boolean tableCreated;

        try (Statement statement = getAutoCommitConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            for (DbConstraint constraint : constraints) {
                table.addConstraint(constraint);
            }

            String sql = new CreateTableQuery(table, true).validate().toString();
            System.out.println("Db.create() " + sql);

            tableCreated = statement.execute(sql);
        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Table creation failed. " + table.getName());
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            tableCreated = false;
        }

        return tableCreated;
    }

    private void init() {
        mSpec.addDefaultSchema();
    }

    private static class Holder {

        private static final Db INSTANCE = new Db();
    }
}
